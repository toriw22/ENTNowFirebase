package com.chico_ent.layout;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class CheckInActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private DatabaseReference mDatabase;

    private ZXingScannerView mScannerView;

    static int REQUEST_CODE = 435;

    String bID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Assume thisActivity is the current activity
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.CAMERA);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, REQUEST_CODE);
        }
        else {
            scanCode();
        }

        String type = getIntent().getStringExtra(DealsBusinessesFragments.TYPE);
        if (type != null && type.equals("deals")) {
            String originalItemId = getIntent().getStringExtra(DealsBusinessesFragments.ITEMID);
            Log.i("Path", originalItemId);
            FirebaseConnection.getSingleDeal(originalItemId, new nextTask() {
                @Override
                void continueToNextTask(Object object) {
                    if (object != null) {
                        Business business = (Business) object;
                        bID = business.businessID;
                    }
                }
            });
        }
        else if (type != null && type.equals("flash-deals")) {
            String originalItemId = getIntent().getStringExtra(DealsBusinessesFragments.ITEMID);
            Log.i("Path", originalItemId);
            FirebaseConnection.getSingleFlashDeal(originalItemId, new nextTask() {
                @Override
                void continueToNextTask(Object object) {
                    if (object != null) {
                        Business business = (Business) object;
                        bID = business.businessID;
                    }
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mScannerView == null)
            mScannerView.stopCamera();
    }

    private void scanCode() {
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view.
        this.setContentView(mScannerView);
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();         // Start camera

    }

    private void tryCheckIn(final String itemID) {
        String type = getIntent().getStringExtra(DealsBusinessesFragments.TYPE);
        if (type != null && (type.equals("deals") || type.equals("flash-deals"))) {
            String originalItemId = getIntent().getStringExtra(DealsBusinessesFragments.ITEMID);
            if (bID != null && bID.equals(itemID)) {
                tryCheckIn(originalItemId, type, new nextTask() {
                    @Override
                    void continueToNextTask(Object object) {
                        boolean didSucceed = (boolean) object;
                        if (didSucceed) {
                            makeAlert(R.string.processed_qr_code_deal);
                            tryCheckIn(itemID, "businesses", new nextTask() {
                                @Override
                                void continueToNextTask(Object object) {
                                    return;
                                }
                            });
                        }
                        else
                            makeAlert(R.string.invalid_qr_code);
                    }
                });
            }
            else {
                makeAlert(R.string.invalid_qr_code);
            }
        }
        else {
            tryCheckIn(itemID, "businesses", new nextTask() {
                @Override
                void continueToNextTask(Object returned) {
                    boolean didSucceed = (boolean) returned;
                    if (didSucceed) {
                        makeAlert(R.string.processed_qr_code_check_in);
                    } else
                        makeAlert(R.string.invalid_qr_code);
                }
            });
        }
    }

    private void tryCheckIn(final String itemID, final String itemType, final nextTask next) {

        mDatabase.child(itemType).child(itemID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                if (dataSnapshot.getValue() == null) {
                    next.continueToNextTask(false);
                }
                else {
                    next.continueToNextTask(true);
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    final String uid = user.getUid();
                    String pushType;
                    if (itemType.equals("businesses"))
                        pushType = "checkIns";
                    else
                        pushType = "redemptions";
                    DatabaseReference newCheckIn = mDatabase.child(pushType).push();
                    if (itemType.equals("businesses"))
                        newCheckIn.child("businessID").setValue(itemID);
                    else {
                        newCheckIn.child("dealID").setValue(itemID);
                        String businessID = (String) dataSnapshot.child("businessID").getValue();
                        newCheckIn.child("businessID").setValue(businessID);
                    }
                    newCheckIn.child("user").setValue(uid);
                    newCheckIn.child("timestamp").setValue(System.currentTimeMillis());

                    if (itemType.equals("businesses")) {
                        mDatabase.child("users").child(uid).child("points").child(itemID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() == null) {
                                    DatabaseReference businessPoints = mDatabase.child("users").child(uid).child("points").child(itemID);
                                    businessPoints.setValue(1);
                                } else {
                                    long points = (long) dataSnapshot.getValue();
                                    mDatabase.child("users").child(uid).child("points").child(itemID).setValue(points + 1);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                    else {
                        int pointsToSubtract = 0;
                        if (dataSnapshot.child("points").getValue() != null) {
                            pointsToSubtract = (int) (long) dataSnapshot.child("points").getValue();
                        }
                        if (pointsToSubtract != 0) {
                            final int fpointsToSubtract = pointsToSubtract;
                            mDatabase.child("users").child(uid).child("points").child(itemID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.getValue() == null) {
                                        DatabaseReference businessPoints = mDatabase.child("users").child(uid).child("points").child(itemID);
                                        businessPoints.setValue(1);
                                    } else {
                                        long points = (long) dataSnapshot.getValue();
                                        mDatabase.child("users").child(uid).child("points").child(itemID).setValue(points - fpointsToSubtract);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void tryCheckInDeals(final String itemID, final String businessID, final String itemType, final nextTask next) {

        mDatabase.child(itemType).child(itemID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                if (dataSnapshot.getValue() == null) {
                    next.continueToNextTask(false);
                }
                else {
                    next.continueToNextTask(true);
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    final String uid = user.getUid();
                    String pushType;
                    if (itemType.equals("businesses"))
                        pushType = "checkIns";
                    else
                        pushType = "redemptions";
                    DatabaseReference newCheckIn = mDatabase.child(pushType).push();
                    if (itemType.equals("businesses"))
                        newCheckIn.child("businessID").setValue(itemID);
                    else {
                        newCheckIn.child("dealID").setValue(itemID);
                        String businessID = (String) dataSnapshot.child("businessID").getValue();
                        newCheckIn.child("businessID").setValue(businessID);
                    }
                    newCheckIn.child("user").setValue(uid);
                    newCheckIn.child("timestamp").setValue(System.currentTimeMillis());

                    if (itemType.equals("businesses")) {
                        mDatabase.child("users").child(uid).child("points").child(itemID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() == null) {
                                    DatabaseReference businessPoints = mDatabase.child("users").child(uid).child("points").child(itemID);
                                    businessPoints.setValue(1);
                                } else {
                                    long points = (long) dataSnapshot.getValue();
                                    mDatabase.child("users").child(uid).child("points").child(itemID).setValue(points + 1);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                    else {
                        int pointsToSubtract = 0;
                        if (dataSnapshot.child("points").getValue() != null) {
                            pointsToSubtract = (int) (long) dataSnapshot.child("points").getValue();
                        }
                        if (pointsToSubtract != 0) {
                            final int fpointsToSubtract = pointsToSubtract;
                            mDatabase.child("users").child(uid).child("points").child(itemID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.getValue() == null) {
                                        DatabaseReference businessPoints = mDatabase.child("users").child(uid).child("points").child(itemID);
                                        businessPoints.setValue(1);
                                    } else {
                                        long points = (long) dataSnapshot.getValue();
                                        mDatabase.child("users").child(uid).child("points").child(itemID).setValue(points - fpointsToSubtract);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void makeAlert(int message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CheckInActivity.this);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                CheckInActivity.this.finish();
            }
        });            // Create the AlertDialog object and return it

        builder.setMessage(message);
        AlertDialog newFragment = builder.create();
        newFragment.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            boolean flag = true;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
                for (int i = 0, len = permissions.length; i < len; i++)
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
                        flag = false;
            if (flag)
                scanCode();
        }
    }

    @Override
    public void handleResult(Result result) {
        tryCheckIn(result.getText());
    }

}
