package com.chico_ent.layout;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
            .defaultDisplayImageOptions(defaultOptions)
            .build();
        ImageLoader.getInstance().init(config);

        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("email").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null || dataSnapshot.getValue().equals("")) {
                    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                    if (email == null || email.equals("")) {
                        if (AccessToken.getCurrentAccessToken() != null) {
                            String userID = AccessToken.getCurrentAccessToken().getUserId();
                            email = userID + "@facebook.com";
                        }
                    }
                    FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("email").setValue(email);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();


        // Code for making new flash-deals.
        DatabaseReference dealsDatabase = mDatabase.child("deals");

        String name = "Tower of Power";
        String description = "See the play on 9/16 to get a point. Students only pay $10!";
        String businessID = "-KtKcCI7H5kKTt4YUoTf";
        String pictureURL = "https://firebasestorage.googleapis.com/v0/b/ent-now.appspot.com/o/pictures%2Fchico%20perfoming%20arts.png?alt=media&token=27d9fc06-f27f-48b1-8708-869f9bd584d8";
        long points = 3;
        long start = 0; //1506754800;
        long end = 0;   //1506841199;

        if (name.length() * description.length() * pictureURL.length() == 0)
            return;

        DatabaseReference item = dealsDatabase.push();

        item.child("name").setValue(name);
        item.child("description").setValue(description);
        item.child("pictureURL").setValue(pictureURL);
        item.child("businessID").setValue(businessID);
        item.child("points").setValue(points);
        item.child("start").setValue(start);
        item.child("end").setValue(end);
        item.child("daysAvailable").child("Sunday").setValue("true");
        item.child("daysAvailable").child("Monday").setValue("true");
        item.child("daysAvailable").child("Tuesday").setValue("true");
        item.child("daysAvailable").child("Wednesday").setValue("true");
        item.child("daysAvailable").child("Thursday").setValue("true");
        item.child("daysAvailable").child("Friday").setValue("true");
        item.child("daysAvailable").child("Saturday").setValue("true");


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.logout, menu);
        menu.findItem(R.id.logout).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                LoginManager.getInstance().logOut();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this, login.class);
                startActivity(intent);
                finish();
                return false;
            }
        });

        /*menu.findItem(R.id.makeDeal).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Intent intent = new Intent(MainActivity.this, addDeal.class);
                startActivity(intent);
                return false;
            }
        });

        menu.findItem(R.id.makeBusiness).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Intent intent = new Intent(MainActivity.this, addBusiness.class);
                startActivity(intent);
                return false;
            }
        });*/

        menu.findItem(R.id.checkIn).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startActivity(new Intent(MainActivity.this, CheckInActivity.class));
                return false;
            }
        });

        menu.findItem(R.id.my_account).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startActivity(new Intent(MainActivity.this, myProfile.class));
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return DealsBusinessesFragments.newInstance(position);
        }


        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Deals";
                case 1:
                    return "Businesses";
            }
            return null;
        }
    }
}
