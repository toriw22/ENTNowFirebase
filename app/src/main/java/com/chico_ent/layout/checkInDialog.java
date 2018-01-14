package com.chico_ent.layout;

import android.app.Dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by amarj on 7/21/2017.
 */

public class checkInDialog extends DialogFragment {

    private DatabaseReference mDatabase;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();


        final View root = inflater.inflate(R.layout.check_in_dialog, null);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(root);

        root.findViewById(R.id.submitCheckIn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabase = FirebaseDatabase.getInstance().getReference();

                String businessID = ((EditText) root.findViewById(R.id.businessID)).getText().toString();

                mDatabase.child("deals").orderByChild("businessID").equalTo(businessID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String name, description, pictureURL;
                        Log.i("DATASNAPSHOT", dataSnapshot.getValue().toString());

                        for (DataSnapshot imageSnapshot: dataSnapshot.getChildren()) {
                            name = (String) imageSnapshot.child("name").getValue();
                            description = (String) imageSnapshot.child("description").getValue();
                            pictureURL = (String) imageSnapshot.child("pictureURL").getValue();

                            Context context = getApplicationContext();
                            CharSequence text = name + "\n" + description;
                            int duration = Toast.LENGTH_SHORT;

                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                dismiss();
            }
        });
        return builder.create();
    }

}
