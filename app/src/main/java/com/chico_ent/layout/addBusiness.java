package com.chico_ent.layout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class addBusiness extends AppCompatActivity {

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_business);

        ((Button) findViewById(R.id.submit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabase = FirebaseDatabase.getInstance().getReference();

                DatabaseReference dealsDatabase = mDatabase.child("itemsToRender");

                String name = ((EditText) findViewById(R.id.businessName)).getText().toString();
                String description = ((EditText) findViewById(R.id.businessDescription)).getText().toString();
                String pictureURL = ((EditText) findViewById(R.id.businessPictureURL)).getText().toString();
                String location = ((EditText) findViewById(R.id.businessLocation)).getText().toString();

                if (name.length() * description.length() * pictureURL.length() == 0)
                    return;

                DatabaseReference item = dealsDatabase.push();

                item.child("name").setValue(name);
                item.child("description").setValue(description);
                item.child("pictureURL").setValue(pictureURL);
                item.child("location").setValue(location);

                finish();

            }
        });
    }
}
