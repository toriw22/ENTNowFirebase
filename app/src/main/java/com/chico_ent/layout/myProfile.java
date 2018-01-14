package com.chico_ent.layout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.widget.ProfilePictureView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class myProfile extends AppCompatActivity {

    ArrayList<Business> businesses;
    HashMap<String, String> points;
    dealsArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        boolean loggedInFB = !(null == AccessToken.getCurrentAccessToken());

        if (loggedInFB)
            facebook();
        else
            google();

        getContent();
    }

    void facebook() {
        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        String first = "", last = "", id = "", birthday = "";
                        try {
                            id = (String) object.getString("id");
                            first = (String) object.getString("first_name");
                            last = (String) object.getString("last_name");
                            birthday = (String) object.getString("birthday");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String name = first + " " + last;
                        ((TextView) findViewById(R.id.name)).setText(name);

                        ProfilePictureView profilePictureView;
                        profilePictureView = (ProfilePictureView) findViewById(R.id.profile_picture);
                        profilePictureView.setProfileId(id);

                        DatabaseReference mDatabaseReference;
                        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        mDatabaseReference.child("users").child(uid).child("birthday").setValue(birthday);

                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,first_name,last_name,birthday");
        request.setParameters(parameters);
        request.executeAsync();

    }

    void google() {
        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        ((TextView) findViewById(R.id.name)).setText(me.getEmail());
    }

    void getContent() {
        businesses = new ArrayList<>();
        points = new HashMap<>();

        adapter = new dealsArrayAdapter(this, businesses);
        ListView listView = (ListView) findViewById(R.id.points_list);
        listView.setAdapter(adapter);

        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        String uid = me.getUid();
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();

        db.child("users").child(uid).child("points").orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {
                    String businessID = child.getKey();
                    String pointsEarned = String.valueOf((long) child.getValue());
                    points.put(businessID, pointsEarned);
                    getBusinessInfo(businessID);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    void getBusinessInfo(final String businessID) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        db.child("businesses").child(businessID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Business business = new Business();
                business.name = (String) dataSnapshot.child("name").getValue();
                business.description = points.get(businessID);
                business.description += " points earned.";
                business.picture = (String) dataSnapshot.child("pictureURL").getValue();
                businesses.add(business);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
