package com.chico_ent.layout;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class loginEmail extends AppCompatActivity {

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_email);

        auth = FirebaseAuth.getInstance();

        Intent sentIntent = getIntent();
        String desired_action = sentIntent.getStringExtra(login.DESIRED_ACTION);
        if (desired_action.equals(login.EXISTING_ACCOUNT)) {
            findViewById(R.id.confirmPassword).setVisibility(View.GONE);
            ((Button) findViewById(R.id.submit)).setText(R.string.login);
            ((Button) findViewById(R.id.submit)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String email, password, confirmPassword;
                    email = ((EditText) findViewById(R.id.username)).getText().toString();
                    password = ((EditText) findViewById(R.id.password)).getText().toString();

                    auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(loginEmail.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d("Login Email", "signInWithEmail:success");
                                        FirebaseUser user = auth.getCurrentUser();
                                        startBusinessesActivity();
                                    } else {
                                        // If sign in fails, display a message to the user
                                        setErrorText(getString(R.string.login_failed));
                                        Log.w("Login Email", "signInWithEmail:failure", task.getException());
                                        Toast.makeText(loginEmail.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                }
            });
        }
        else if (desired_action.equals(login.NEW_ACCOUNT)) {
            ((Button) findViewById(R.id.submit)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String email, password, confirmPassword;
                    email = ((EditText) findViewById(R.id.username)).getText().toString();
                    password = ((EditText) findViewById(R.id.password)).getText().toString();
                    confirmPassword = ((EditText) findViewById(R.id.confirmPassword)).getText().toString();

                    if (password.length() < 5) {
                        //Toast.makeText(getApplicationContext(), getString(R.string.password_length), Toast.LENGTH_SHORT).show();
                        setErrorText(getString(R.string.password_length));
                        return;
                    }
                    if (!confirmPassword.equals(password)) {
                        setErrorText(getString(R.string.password_match));
                        return;
                    }
                    if (!isEmailValid(email)) {
                        setErrorText(getString(R.string.email_invalid));
                        return;
                    }

                    auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(loginEmail.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d("Login Email", "createUserWithEmail:success");
                                        FirebaseUser user = auth.getCurrentUser();
                                        String email = user.getEmail();
                                        String uid = user.getUid();
                                        FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("email").setValue(email);
                                        FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("isAdmin").setValue("false");
                                        Toast.makeText(loginEmail.this, getString(R.string.account_created),
                                                Toast.LENGTH_SHORT).show();
                                        startBusinessesActivity();
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w("Login Email", "createUserWithEmail:failure", task.getException());
                                        setErrorText(getString(R.string.email_not_unique));
                                    }
                                }
                            });
                }
            });
        }
    }

    void startBusinessesActivity() {
        Intent intent = new Intent(loginEmail.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    void setErrorText(String text) {
        TextView tView = (TextView) findViewById(R.id.error);
        tView.setText(text);
        tView.setVisibility(View.VISIBLE);
    }


    /**
     * method is used for checking valid email id format.
     *
     * @param email
     * @return boolean true for valid false for invalid
     */
    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
