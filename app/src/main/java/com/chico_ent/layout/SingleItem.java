package com.chico_ent.layout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.sccomponents.gauges.ScGauge;
import com.sccomponents.gauges.ScWriter;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.chico_ent.layout.DealsBusinessesFragments.BUSINESSID;
import static com.chico_ent.layout.DealsBusinessesFragments.ITEMID;
import static com.chico_ent.layout.DealsBusinessesFragments.TYPE;

public class SingleItem extends AppCompatActivity {

    Business business, deal;
    ArrayList<String> current_deals;
    private DatabaseReference mDatabase;
    String itemType, itemID, businessID;
    int pointsEarned = 0;
    final boolean[] weekAvailability = {false, false, false, false, false, false, false};
    ArrayList<Business> deals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_item);

        getContent();

        /*Intent intent = getIntent();
        itemType = intent.getStringExtra(TYPE);
        itemID = intent.getStringExtra(ITEMID);
        businessID = intent.getStringExtra(BUSINESSID);

        final String[] week = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        final int[] weekIDs = {R.id.sunday, R.id.monday, R.id.tuesday, R.id.wednesday, R.id.thursday, R.id.friday, R.id.saturday};

        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (itemType != null && itemID != null) {
            mDatabase.child(itemType).child(itemID).addListenerForSingleValueEvent(new ValueEventListener() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    business = new Business();
                    String name, description, pictureURL, location;
                    name = (String) dataSnapshot.child("name").getValue();
                    description = (String) dataSnapshot.child("description").getValue();
                    pictureURL = (String) dataSnapshot.child("pictureURL").getValue();
                    location = (String) dataSnapshot.child("location").getValue();
                    business.name = name;
                    business.picture = pictureURL;
                    business.description = description;
                    business.location = location;
                    business.type = itemType;

                    business.businessID = businessID;

                    business.itemID = itemID;

                    updateUI();

                    if (!itemType.equals("businesses")) {
                        for (int ii = 0; ii != 7; ii ++) {
                            String available = (String) dataSnapshot.child("daysAvailable").child(week[ii]).getValue();
                            if (available.equals("true")) {
                                findViewById(weekIDs[ii]).setBackground(getDrawable(R.color.green));
                                weekAvailability[ii] = true;
                            }
                        }
                        mDatabase.child("businesses").child(businessID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String businessname = (String) dataSnapshot.child("name").getValue();
                                String businesslocation = (String) dataSnapshot.child("location").getValue();
                                if (businesslocation != null && !businesslocation.equals(""))
                                    business.location = businessname + " - " + businesslocation;
                                updateUI();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = mDatabase.child("users").child(uid).child("points").child(businessID);
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() != null) {
                                pointsEarned = (int) (long) dataSnapshot.getValue();
                                updateUI();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                    findViewById(R.id.submitCheckIn).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(SingleItem.this, CheckInActivity.class);
                            if (itemType.equals("deals")) {
                                tryToClaimDeal();
                            }
                            else {
                                startActivity(new Intent(SingleItem.this, CheckInActivity.class));
                            }
                        }
                    });


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            if (itemType.equals("businesses")) {
                Query search = mDatabase.child("deals").orderByChild("businessID").equalTo(itemID);
                search.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        current_deals = new ArrayList<String>();

                        Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.current_deals);

                        int i = 0;
                        for (DataSnapshot child : children) {
                            if (child.child("daysAvailable") != null) {
                                DataSnapshot daysAvailable = child.child("daysAvailable");

                                Calendar calendar = Calendar.getInstance();
                                int day = calendar.get(Calendar.DAY_OF_WEEK);
                                day -= 1;
                                String isValid;
                                isValid = (String) daysAvailable.child(week[day]).getValue();

                                if (!isValid.equals("true"))
                                    continue;
                            }

                            String name = (String) child.child("name").getValue();
                            name += ": ";

                            name += (String) child.child("description").getValue();
                            linearLayout.setVisibility(View.VISIBLE);
                            String id = child.getKey();
                            current_deals.add(id);
                            TextView textView = new TextView(SingleItem.this);
                            textView.setText(name);
                            textView.setTag(i);
                            textView.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT));
                            textView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    int position = (int) v.getTag();
                                    String dealID = current_deals.get(position);
                                    Intent intent = new Intent(SingleItem.this, SingleItem.class);
                                    intent.putExtra(TYPE, "deals");
                                    intent.putExtra(BUSINESSID, itemID);
                                    intent.putExtra(ITEMID, dealID);
                                    SingleItem.this.startActivity(intent);
                                }
                            });
                            linearLayout.addView(textView);
                            i++;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }*/
    }

    void getContent() {
        Intent intent = getIntent();
        itemType = intent.getStringExtra(TYPE);
        itemID = intent.getStringExtra(ITEMID);
        businessID = intent.getStringExtra(BUSINESSID);

        Log.i("This ID:", itemID);

        if (itemType.equals("businesses")) {
            FirebaseConnection.getSingleBusiness(itemID, new nextTask() {
                @Override
                void continueToNextTask(Object object) {
                    business = (Business) object;
                    updateUI();
                }
            });

            FirebaseConnection.getPoints(itemID, new nextTask() {
                @Override
                void continueToNextTask(Object object) {
                    pointsEarned = (int) object;
                    updateUI();
                }
            });

            FirebaseConnection.getDeals(businessID, new nextTask() {
                @Override
                void continueToNextTask(Object object) {
                    deals = (ArrayList<Business>) object;
                    addDeals();
                }
            });
        }
        else if (itemType.equals("deals")) {
            FirebaseConnection.getSingleDeal(itemID, new nextTask() {
                @Override
                void continueToNextTask(Object object) {
                    deal = (Business) object;
                    updateUI();
                }
            });
            FirebaseConnection.getSingleBusiness(businessID, new nextTask() {
                @Override
                void continueToNextTask(Object object) {
                    business = (Business) object;
                    updateUI();
                }
            });
            FirebaseConnection.getPoints(businessID, new nextTask() {
                @Override
                void continueToNextTask(Object object) {
                    pointsEarned = (int) object;
                    updateUI();
                }
            });
        }
        else if (itemType.equals("flash-deals")) {
            FirebaseConnection.getSingleFlashDeal(itemID, new nextTask() {
                @Override
                void continueToNextTask(Object object) {
                    deal = (Business) object;
                    updateUI();
                }
            });
            FirebaseConnection.getSingleBusiness(businessID, new nextTask() {
                @Override
                void continueToNextTask(Object object) {
                    business = (Business) object;
                    updateUI();
                }
            });
            FirebaseConnection.getPoints(businessID, new nextTask() {
                @Override
                void continueToNextTask(Object object) {
                    pointsEarned = (int) object;
                    updateUI();
                }
            });
        }
    }

    void updateUI() {
        if (pointsEarned == 0) {
            ScGauge scGauge = (ScGauge) findViewById(R.id.gauge);
            scGauge.setHighValue(1);
            String stringEarned = "You have not earned any points here yet.";
            ((TextView) findViewById(R.id.pointsEarned)).setText(stringEarned);
        }
        else {
            ScGauge scGauge = (ScGauge) findViewById(R.id.gauge);
            int gaugeMin = Math.min(pointsEarned * 10, 100);
            scGauge.setHighValue(gaugeMin);
            String stringEarned = "You have earned " + String.valueOf(pointsEarned) + " points here.";
            ((TextView) findViewById(R.id.pointsEarned)).setText(stringEarned);
        }

        if (itemType.equals("businesses") && business != null) {
            if (!business.name.equals("")) {
                ((TextView) findViewById(R.id.name)).setText(business.name);
            }

            if (!business.location.equals("")) {
                ((TextView) findViewById(R.id.location)).setText(business.location);
            }

            if (!business.description.equals("")) {
                ((TextView) findViewById(R.id.description)).setText(business.description);
            }

            if (!business.picture.equals("")) {
                ImageLoader imageLoader = ImageLoader.getInstance(); // Get singleton instance
                imageLoader.displayImage(business.picture, (ImageView) findViewById(R.id.background));
            }
        }
        else if ((itemType.equals("deals") || itemType.equals("flash-deals")) && deal != null) {
            if (!deal.name.equals("")) {
                ((TextView) findViewById(R.id.name)).setText(deal.name);
            }

            if (business != null && !business.location.equals("")) {
                String location = business.name + " - " + business.location;
                ((TextView) findViewById(R.id.location)).setText(location);
            }

            if (!deal.description.equals("")) {
                ((TextView) findViewById(R.id.description)).setText(deal.description);
            }
            if (deal.points == 0 && pointsEarned == 0) {
                String stringEarned = "You have not earned any points here yet.";
                stringEarned += "\nThis deal does not cost points.";
                ((TextView) findViewById(R.id.pointsEarned)).setText(stringEarned);
            }
            else {
                ScGauge scGauge = (ScGauge) findViewById(R.id.gauge);
                String stringEarned = "You have earned " + String.valueOf(pointsEarned) + " points here.";
                stringEarned += "\nThis deal costs " + deal.points + " points.";
                if (pointsEarned < deal.points) {
                    int gaugeMin = Math.min(100 * pointsEarned / deal.points, 100);
                    scGauge.setHighValue(gaugeMin);
                    stringEarned += "\nYou need " + (deal.points - pointsEarned) + " more points to claim this deal.";
                }
                ((TextView) findViewById(R.id.pointsEarned)).setText(stringEarned);
            }

            if (!deal.picture.equals("")) {
                ImageLoader imageLoader = ImageLoader.getInstance(); // Get singleton instance
                imageLoader.displayImage(deal.picture, (ImageView) findViewById(R.id.background));
            }
        }

        findViewById(R.id.submitCheckIn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SingleItem.this, CheckInActivity.class);
                if (itemType.equals("deals") || itemType.equals("flash-deals")) {
                    tryToClaimDeal();
                }
                else {
                    startActivity(new Intent(SingleItem.this, CheckInActivity.class));
                }
            }
        });

        if (itemType.equals("deals") || itemType.equals("flash-deals")) {
            ((Button) findViewById(R.id.submitCheckIn)).setText(R.string.claim_deal);

        }
    }

    void addDeals() {


        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.current_deals);

        for (int i = 0; i < deals.size(); i++) {
            View item = LayoutInflater.from(this).inflate(R.layout.deals_list_item, null);
            ((TextView) item.findViewById(R.id.name)).setText(deals.get(i).name);
            String pointsString = "This deal costs " + deals.get(i).points + " points.";
            ((TextView) item.findViewById(R.id.location)).setText(String.valueOf(pointsString));
            ImageLoader imageLoader = ImageLoader.getInstance(); // Get singleton instance
            imageLoader.displayImage(deals.get(i).picture, (ImageView) item.findViewById(R.id.background));
            item.setTag(deals.get(i));


            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Business deal = (Business) v.getTag();
                    Intent intent = new Intent(SingleItem.this, SingleItem.class);
                    intent.putExtra(TYPE, deal.type);
                    intent.putExtra(BUSINESSID, deal.businessID);
                    intent.putExtra(ITEMID, deal.itemID);
                    SingleItem.this.startActivity(intent);
                }
            });

            linearLayout.addView(item);
        }
        linearLayout.setVisibility(View.VISIBLE);
    }

    /*void updateUI() {
        if (pointsEarned != 0) {
            findViewById(R.id.gauge).setVisibility(View.VISIBLE);
            ScGauge scGauge = (ScGauge) findViewById(R.id.gauge);
            scGauge.setHighValue(pointsEarned * 10);
        }
        else {
            findViewById(R.id.gauge).setVisibility(View.GONE);
        }

        if (business.name != null && !business.name.equals(""))
            ((TextView) findViewById(R.id.name)).setText(business.name);
        else
            findViewById(R.id.name).setVisibility(View.GONE);

        if (business.time != null && !business.time.equals(""))
            ((TextView) findViewById(R.id.time)).setText(business.time);
        else
            findViewById(R.id.time).setVisibility(View.GONE);

        if (business.description != null && !business.description.equals(""))
            ((TextView) findViewById(R.id.description)).setText(business.description);
        else
            findViewById(R.id.description).setVisibility(View.GONE);

        if (business.location != null && !business.location.equals(""))
            ((TextView) findViewById(R.id.location)).setText(business.location);
        else
            findViewById(R.id.location).setVisibility(View.GONE);

        if (business.picture != null && !business.picture.equals("")) {
            ImageLoader imageLoader = ImageLoader.getInstance(); // Get singleton instance
            imageLoader.displayImage(business.picture, (ImageView) findViewById(R.id.background));
        }
        else
            findViewById(R.id.background).setVisibility(View.GONE);
        
        if (itemType.equals("businesses")) {
            findViewById(R.id.dealInfo).setVisibility(View.GONE);
        }
        else {
            findViewById(R.id.dealInfo).setVisibility(View.VISIBLE);
            if (business.points == 1) {
                String displayPoints = "This deal costs " + business.points + " point.";
                ((TextView) findViewById(R.id.pointsCost)).setText(displayPoints);
            }
            if (business.points != 0) {
                String displayPoints = "This deal costs " + business.points + " points.";
                ((TextView) findViewById(R.id.pointsCost)).setText(displayPoints);
            }
            else {
                String displayPoints = "This deal does not cost any points.";
                ((TextView) findViewById(R.id.pointsCost)).setText(displayPoints);
            }
            if (!dealIsValid()) {
                ((Button) findViewById(R.id.submitCheckIn)).setText(R.string.not_enough_points);
                findViewById(R.id.submitCheckIn).setEnabled(false);
            }
            else {
                ((Button) findViewById(R.id.submitCheckIn)).setText(R.string.claim_deal);
                findViewById(R.id.submitCheckIn).setEnabled(true);
            }
        }
        if (pointsEarned != 1) {
            String displayPoints = "You have earned " + pointsEarned + " points at " + business.name;
            displayPoints += ".";
            ((TextView) findViewById(R.id.pointsEarned)).setText(displayPoints);
        }
        else {
            String displayPoints = "You have earned " + pointsEarned + " point at " + business.name;
            displayPoints += ".";
            ((TextView) findViewById(R.id.pointsEarned)).setText(displayPoints);
        }
    }*/

    void tryToClaimDeal() {
        if (dealIsValid()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setPositiveButton(R.string.proceed, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Intent intent = new Intent(SingleItem.this, CheckInActivity.class);
                    intent.putExtra(TYPE, itemType);
                    intent.putExtra(BUSINESSID, businessID);
                    intent.putExtra(ITEMID, itemID);
                    SingleItem.this.startActivity(intent);
                    }
            });            // Create the AlertDialog object and return it
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //
                }
            });

            String message = "Claiming this deal costs " + business.points + " points. ";
            message += "You currently have " + pointsEarned + ". Continue?";

            builder.setMessage(message);
            AlertDialog newFragment = builder.create();
            newFragment.show();
        }
        else {
            String displayText = getString(R.string.not_enough_points);
            Toast.makeText(this, displayText, Toast.LENGTH_SHORT).show();
        }
    }

    boolean dealIsValid() {
        if (pointsEarned >= business.points) {
            Date date = new Date();
            int today = date.getDay();
            return true;
            //return weekAvailability[today];
        }
        else
            return false;
    }
}
