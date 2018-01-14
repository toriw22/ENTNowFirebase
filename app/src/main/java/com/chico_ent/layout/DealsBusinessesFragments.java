package com.chico_ent.layout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by amarj on 7/27/2017.
 */

public class DealsBusinessesFragments extends android.support.v4.app.Fragment {

    public final static String BUSINESSID = "BUSINESSID";
    public final static String TYPE = "TYPE";
    public final static String ITEMID = "ITEMID";

    final String[] week = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    public DealsBusinessesFragments() {

    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static DealsBusinessesFragments newInstance(int sectionNumber) {
        DealsBusinessesFragments fragment = new DealsBusinessesFragments();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    ArrayList<Business> itemsToRender;
    private DatabaseReference mDatabase;
    ArrayAdapter<Business> adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);

    }

    @Override
    public void onResume() {
        super.onResume();
        int sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
        itemsToRender = new ArrayList<>();

        if (sectionNumber < 2) {
            final String itemType;
            if (sectionNumber == 1) {
                itemType = "businesses";
                adapter = new businessesArrayAdapter(getContext(), itemsToRender);
            }
            else {
                itemType = "deals";
                adapter = new dealsArrayAdapter(getContext(), itemsToRender);
            }

            getPoints(new nextTask() {
                @Override
                void continueToNextTask(Object object) {
                    getContent(itemType, (HashMap<String, Integer>) object, new nextTask() {
                        @Override
                        void continueToNextTask(Object object) {
                            if (itemType.equals("deals")) {
                                FirebaseConnection.getFlashDeals(new nextTask() {
                                    @Override
                                    void continueToNextTask(Object object) {
                                        if (object == null)
                                            return;
                                        ArrayList<Business> flashDeals = (ArrayList<Business>) object;
                                        Log.i("Length", "Size:" + flashDeals.size());
                                        itemsToRender.addAll(0, flashDeals);
                                        adapter.notifyDataSetChanged();
                                        addExtraInfoDeals();
                                    }
                                });
                                addExtraInfoDeals();
                            }
                        }
                    });
                }
            });
        }
        else if (sectionNumber == 2) {

            //final ListView listView = (ListView) getView().findViewById(R.id.itemList);
            //listView.setAdapter(adapter);


        }
    }

    private void getContent(final String itemType, final HashMap<String, Integer> points, final nextTask next) {

        final GridView listView = (GridView) this.getView().findViewById(R.id.itemList);
        listView.setAdapter(adapter);

        if (itemType.equals("businesses"))
            listView.setNumColumns(2);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        Query businessesDB = mDatabase.child(itemType).orderByKey();

        businessesDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot imageSnapshot: dataSnapshot.getChildren()) {
                    String name, description, pictureURL, location;
                    String itemID = imageSnapshot.getKey();
                    name = (String) imageSnapshot.child("name").getValue();
                    description = (String) imageSnapshot.child("description").getValue();
                    pictureURL = (String) imageSnapshot.child("pictureURL").getValue();
                    location = (String) imageSnapshot.child("location").getValue();
                    Business newBusiness = new Business();
                    newBusiness.name = name;
                    newBusiness.picture = pictureURL;
                    newBusiness.description = description;
                    newBusiness.location = location;
                    newBusiness.type = itemType;

                    if (itemType.equals("businesses")) {
                        newBusiness.businessID = itemID;
                        if (points != null && points.containsKey(itemID))
                            newBusiness.points = points.get(itemID);
                    }
                    else {
                        newBusiness.businessID = (String) imageSnapshot.child("businessID").getValue();
                        if (imageSnapshot.child("daysAvailable") != null) {
                            DataSnapshot daysAvailable = imageSnapshot.child("daysAvailable");

                            Calendar calendar = Calendar.getInstance();
                            int day = calendar.get(Calendar.DAY_OF_WEEK);
                            day -= 1;
                            String isValid;
                            isValid = (String) daysAvailable.child(week[day]).getValue();

                            if(!isValid.equals("true"))
                                continue;


                            /*
                            newBusiness.time = "Available every";
                            for (int ii = 0; ii != 7; ii ++) {
                                String available = (String) imageSnapshot.child("daysAvailable").child(week[ii]).getValue();
                                if (available.equals("true")) {
                                    newBusiness.time += " " + week[ii] + ",";
                                }
                            }
                            newBusiness.points = (int) ((long) imageSnapshot.child("points").getValue());
                            newBusiness.time = newBusiness.time.substring(0, newBusiness.time.length() - 1);
                            newBusiness.time += ".";
                            */
                        }
                    }

                    newBusiness.itemID = itemID;

                    if (name != null)
                        itemsToRender.add(newBusiness);

                    adapter.notifyDataSetChanged();
                }
                if (next != null)
                    next.continueToNextTask(null);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= itemsToRender.size())
                    return;
                Business theBusiness = itemsToRender.get(position);
                Intent intent = new Intent(getActivity(), SingleItem.class);
                intent.putExtra(TYPE, theBusiness.type);
                intent.putExtra(BUSINESSID, theBusiness.businessID);
                intent.putExtra(ITEMID, theBusiness.itemID);
                getContext().startActivity(intent);
            }
        });
    }

    void getPoints(final nextTask next) {
        mDatabase = FirebaseDatabase.getInstance().getReference();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Query checkIns = mDatabase.child("users").child(uid).child("points");

        checkIns.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> businessIDs = new ArrayList<String>();
                HashMap<String, Integer> points = new HashMap<String, Integer>();

                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot imageSnapshot : dataSnapshot.getChildren()) {
                        String businessID = imageSnapshot.getKey();
                        long noPoints = (long) imageSnapshot.getValue();
                        points.put(businessID, (int) noPoints);
                        businessIDs.add(businessID);
                    }
                }

                /*for (String businessID : businessIDs) {
                    addBusinessToArray(businessID, points.get(businessID));
                }*/
                next.continueToNextTask(points);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    void addExtraInfoDeals() {
        for (final Business item : itemsToRender) {
            if (item.location == null || item.location.equals("")) {
                mDatabase.child("businesses").child(item.businessID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null) {
                            item.location = (String) dataSnapshot.child("name").getValue();
                            //item.location += " - ";
                            //item.location += (String) dataSnapshot.child("location").getValue();
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
    }

    /*void addExtraInfoBusinesses () {
        for (final Business item : itemsToRender) {
            Query search = mDatabase.child("deals").orderByChild("businessID").equalTo(item.businessID);
            search.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataImage) {
                    int length = 0;
                    for (DataSnapshot dataSnapshot : dataImage.getChildren()) {
                        dataSnapshot.child("")
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        mDatabase.child("deals").
    }*/
    /*void addBusinessToArray(String businessID, final int points) {
        mDatabase.child("itemsToRender").child(businessID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Business newBusiness = new Business();
                String name = (String) dataSnapshot.child("name").getValue();
                newBusiness.name = name;
                newBusiness.description = "You earned " + String.valueOf(points) + " points here.";
                itemsToRender.add(newBusiness);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }*/
}
