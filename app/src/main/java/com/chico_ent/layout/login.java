package com.chico_ent.layout;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class login extends AppCompatActivity {

    CallbackManager callbackManager;

    private FirebaseAuth mAuth;

    public final static String DESIRED_ACTION = "DESIRED_ACTION";
    public final static String NEW_ACCOUNT = "NEW_ACCOUNT";
    public final static String EXISTING_ACCOUNT = "EXISTING_ACCOUNT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();


        /*try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.app_banz152.layout",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }*/



        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("email");
        loginButton.setReadPermissions("public_profile");

        callbackManager = CallbackManager.Factory.create();

        if (isAlreadyLoggedIn())
            startBusinessesActivity();

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.i("Facebook", "Successfully logged into Facebook.");
                        AccessToken accessToken = loginResult.getAccessToken();
                        String print = "Successfully logged into Facebook as ";
                        print += accessToken.getUserId();
                        print += " with access token ";
                        print += accessToken.getToken();
                        print += ".";
                        Log.i("Facebook", print);
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Log.i("Facebook", "Cancelled login to Facebook.");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Log.i("Facebook", "There was an error while trying to login to Facebook.");
                    }
                });

        ((Button) findViewById(R.id.signUpEmail)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(login.this, loginEmail.class);
                intent.putExtra(DESIRED_ACTION, NEW_ACCOUNT);
                startActivity(intent);
            }
        });

        ((Button) findViewById(R.id.signIn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(login.this, loginEmail.class);
                intent.putExtra(DESIRED_ACTION, EXISTING_ACCOUNT);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null)
            startBusinessesActivity();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    boolean isAlreadyLoggedIn() {
        boolean loggedInFB, loggedInFirebase;
        loggedInFirebase = !(mAuth.getCurrentUser() == null);
        loggedInFB = !(null == AccessToken.getCurrentAccessToken());
        return loggedInFB || loggedInFirebase;
    }

    void startBusinessesActivity() {
        Intent intent = new Intent(login.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void handleFacebookAccessToken(AccessToken token) {

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Facebook to Firebase", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            startBusinessesActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Facebook to Firebase", "signInWithCredential:failure", task.getException());
                            Toast.makeText(login.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
