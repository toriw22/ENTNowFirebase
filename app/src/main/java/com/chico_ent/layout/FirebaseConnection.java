package com.chico_ent.layout;

import android.provider.ContactsContract;
import android.provider.Settings;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by amarj on 8/15/2017.
 */

public final class FirebaseConnection {

    private static DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    final static String[] week = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

    private FirebaseConnection() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public static void getSingleBusiness(String id, final nextTask next) {
        getSingleItem(id, "businesses", next);
    }

    public static void getSingleDeal(String id, final nextTask next) {
        getSingleItem(id, "deals", next);
    }

    public static void getSingleFlashDeal(String id, final nextTask next) {
        getSingleItem(id, "flash-deals", next);
    }

    public static void getBusinesses(final nextTask next) {
        getItems("businesses", next);
    }

    public static void getDeals(final nextTask next) {
        getItems("deals", next);
    }

    public static void getFlashDeals(final nextTask next) {
        getItems("flash-deals", next);
    }

    public static void getDeals(String businessID, final nextTask next) {
        ValueEventListener vel = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataImage) {
                ArrayList<Business> businesses = new ArrayList<>();
                Iterable<DataSnapshot> dataChildren = dataImage.getChildren();
                for (DataSnapshot dataSnapshot : dataChildren) {
                    Business business = businessFromSnapshot("deals", dataSnapshot);
                    if (business != null)
                        businesses.add(business);
                }
                next.continueToNextTask(businesses);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                next.continueToNextTask(null);
            }
        };

        databaseReference.child("deals").orderByChild("businessID").equalTo(businessID).addListenerForSingleValueEvent(vel);
    }

    public static void getFlashDeals(String businessID, final nextTask next) {
        ValueEventListener vel = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataImage) {
                ArrayList<Business> businesses = new ArrayList<>();
                Iterable<DataSnapshot> dataChildren = dataImage.getChildren();
                for (DataSnapshot dataSnapshot : dataChildren) {
                    Business business = businessFromSnapshot("flash-deals", dataSnapshot);
                    if (business != null)
                        businesses.add(business);
                }
                next.continueToNextTask(businesses);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                next.continueToNextTask(null);
            }
        };

        databaseReference.child("flash-deals").orderByChild("businessID").equalTo(businessID).addListenerForSingleValueEvent(vel);
    }

    private static void getItems(final String type, final nextTask next) {
        ValueEventListener vel = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataImage) {
                ArrayList<Business> businesses = new ArrayList<>();
                Iterable<DataSnapshot> dataChildren = dataImage.getChildren();
                for (DataSnapshot dataSnapshot : dataChildren) {
                    Business business = businessFromSnapshot(type, dataSnapshot);
                    if (business != null)
                        businesses.add(business);
                }
                next.continueToNextTask(businesses);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                next.continueToNextTask(null);
            }
        };

        databaseReference.child(type).orderByKey().addListenerForSingleValueEvent(vel);

    }

    public static void getPoints(String id, final nextTask next) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = databaseReference.child("users").child(uid).child("points").child(id);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    int pointsEarned = (int) (long) dataSnapshot.getValue();
                    next.continueToNextTask(pointsEarned);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private static void getSingleItem(String id, final String type, final nextTask next) {
        ValueEventListener vel = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                next.continueToNextTask(businessFromSnapshot(type, dataSnapshot));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                next.continueToNextTask(null);
            }
        };

        databaseReference.child(type).child(id).addListenerForSingleValueEvent(vel);
    }

    private static Business businessFromSnapshot(String type, DataSnapshot dataSnapshot) {
        Business business = new Business();
        if (dataSnapshot == null)
            return null;
        if (type.equals("businesses")) {
            business.businessID = dataSnapshot.getKey();
        }
        else if (dataSnapshot.child("businessID").getValue() != null) {
            business.businessID = (String) dataSnapshot.child("businessID").getValue();
            business.itemID = dataSnapshot.getKey();
        }

        if (dataSnapshot.child("name").getValue() != null)
            business.name = (String) dataSnapshot.child("name").getValue();
        if (dataSnapshot.child("description").getValue() != null)
            business.description = (String) dataSnapshot.child("description").getValue();
        if (dataSnapshot.child("pictureURL").getValue() != null)
            business.picture = (String) dataSnapshot.child("pictureURL").getValue();
        if (dataSnapshot.child("location").getValue() != null)
            business.location = (String) dataSnapshot.child("location").getValue();
        if (dataSnapshot.child("time").getValue() != null)
            business.time = (String) dataSnapshot.child("time").getValue();
        if (dataSnapshot.child("points").getValue() != null)
            business.points = (int) (long) dataSnapshot.child("points").getValue();
        business.type = type;

        if (business.type.equals("deals")) {
            DataSnapshot daysAvailable = dataSnapshot.child("daysAvailable");

            if (daysAvailable.getValue() != null) {
                Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_WEEK);
                day -= 1;
                String isValid;
                isValid = (String) daysAvailable.child(week[day]).getValue();
                if (isValid.equals("false"))
                    return null;
            }
        }

        if (business.type.equals("flash-deals")) {
            DataSnapshot daysAvailable = dataSnapshot.child("daysAvailable");

            if (daysAvailable.getValue() != null) {
                Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_WEEK);
                day -= 1;
                String isValid;
                isValid = (String) daysAvailable.child(week[day]).getValue();
                if (isValid.equals("false"))
                    return null;
            }

            DataSnapshot start = dataSnapshot.child("start");
            DataSnapshot end = dataSnapshot.child("end");
            if (start.getValue() != null && end.getValue() != null) {
                long time = System.currentTimeMillis() / 1000;
                long s = (long) start.getValue();
                long e = (long) end.getValue();
                if (time < s  || time > e)
                    return null;
            }
        }
        return business;
    }
}
