package com.madhuban.sahaayak;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.madhuban.sahaayak.AppIntro.WelcomeActivity;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import es.dmoral.toasty.Toasty;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesProvider;

import static android.Manifest.permission_group.STORAGE;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{


    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    private LocationGooglePlayServicesProvider provider;
    Double getLat,getLong;
    String CityName,LocalityName,PostalCode,FeatureName,SubLocality="";
    //g sign in
    SignInButton signInButton;
    private GoogleApiClient googleApiClient;
    private static final int RC_SIGN_IN = 1;
    String name, email;
    String idToken;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    String app_version="";
    Button logOut;
    TextView UsernameTv,emailIdTv,version_text;
    ImageView profile_image;
    boolean isUserSignedIn=false;
    ProgressDialog progressDialog;

    String TAG=MainActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
       // this.requestWindowFeature(Window.FEATURE_NO_TITLE);

//Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_login);

       // requestPermission();
        preferences =getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        editor = preferences.edit();


        signInButton=(SignInButton)findViewById(R.id.sign_in_btn);
        UsernameTv=(TextView)findViewById(R.id.name);
        emailIdTv=(TextView)findViewById(R.id.email);
        profile_image=(ImageView)findViewById(R.id.img_profile);
        logOut=(Button)findViewById(R.id.logOut);


        if (isUserSignedIn){

            Toast.makeText(LoginActivity.this,"Login_Activity_signedIn",Toast.LENGTH_LONG).show();

            Intent intent = new Intent(LoginActivity.this, WelcomeActivity.class);
            startActivity(intent);
            finish();

            Log.e("login","signed1");

//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                   // progressDialog.dismiss();
//
//
//                }
//            }, 1000);

        }

        //g sign in
        GoogleSignInOptions gso =  new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))//you can also use R.string.default_web_client_id
                .requestEmail()
                .build();
        googleApiClient=new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();


        firebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance();
        //this is where we start the Auth state Listener to listen for whether the user is signed in or not
        authStateListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                // Get signedIn user
                FirebaseUser user = firebaseAuth.getCurrentUser();


                //if user is signed in, we call a helper method to save the user details to Firebase
                if (user != null) {
                    // User is signed in

                    String firebaseUserId;
                    firebaseUserId=FirebaseAuth.getInstance().getCurrentUser().getUid();
                    editor.putString("firebaseUserId",firebaseUserId);
                    editor.apply();
                    Log.d("fireBaseUserId",firebaseUserId);

                    // you could place other firebase code
                    //logic to save the user details to Firebase
                    isUserSignedIn=true;
                    editor.putBoolean("isUserSignedIn",isUserSignedIn);
                    editor.apply();

                    Log.d("isUserSignIn", String.valueOf(isUserSignedIn));

                    UsernameTv.setText("");
                    emailIdTv.setText("");
                    String UserName= preferences.getString("User_name",null);

                    String EmailID= preferences.getString("Email_id",null);

                    try{
                        String Photo=preferences.getString("Image_url",null);
                        Log.d(TAG,"name"+UserName+"ImageUrl"+Photo);
                        Glide.with(LoginActivity.this).load(Photo).centerCrop().into(profile_image);
                    }catch (NullPointerException e){
                        //  Toast.makeText(getApplicationContext(),"image not found",Toast.LENGTH_LONG).show();
                        Toasty.error(LoginActivity.this, "image not found", Toast.LENGTH_SHORT, true).show();

                    }
                    if (UserName!=null && EmailID!=null)
                    {
                        Log.d(TAG,"mail"+EmailID);

                        UsernameTv.setText(UserName);
                        emailIdTv.setText(EmailID);

                    }


                   // logOut.setVisibility(View.VISIBLE);
                    signInButton.setVisibility(View.GONE);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //progressDialog.dismiss();
                            Intent intent = new Intent(LoginActivity.this, WelcomeActivity.class);
                            startActivity(intent);
                            finish();
                            Log.e("login","signed2");

                        }
                    }, 3000);
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");

                }
            }
        };



        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNetworkAvailable())
                {
                    Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                    startActivityForResult(intent,RC_SIGN_IN);
                    progressDialog
                            = new ProgressDialog(LoginActivity.this);
                    progressDialog.setTitle("Signing In...");
                    progressDialog.show();
                }else {
                    Toasty.error(LoginActivity.this, "Please, connect to Internet", Toast.LENGTH_SHORT, true).show();

                }

            }
        });

        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isNetworkAvailable())
                {
                    FirebaseAuth.getInstance().signOut();
                    Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(
                            new ResultCallback<Status>() {
                                @Override
                                public void onResult(Status status) {
                                    if (status.isSuccess()) {
                                        //  Toast.makeText(getApplicationContext(), "Successfully Signed Out", Toast.LENGTH_LONG).show();
                                        Toasty.success(LoginActivity.this, "Signed Out Successfully!", Toast.LENGTH_SHORT, true).show();

                                        profile_image.setImageResource(R.drawable.ic_person_black_24dp);
                                        UsernameTv.setText("");
                                        emailIdTv.setText("");
                                        logOut.setVisibility(View.GONE);
                                        signInButton.setVisibility(View.VISIBLE);
                                        editor.remove("User_name");
                                        editor.remove("Email_id");
                                        editor.remove("Image_url");
                                        editor.clear();
                                        editor.commit();
                                        isUserSignedIn=false;
                                        editor.putBoolean("isUserSignedIn",isUserSignedIn);
                                        editor.apply();

                                    } else {
                                        // Toast.makeText(getApplicationContext(), "Session not close", Toast.LENGTH_LONG).show();
                                        Toasty.error(LoginActivity.this, "Session not close", Toast.LENGTH_SHORT, true).show();

                                    }
                                }
                            });

                }else {

                    Toasty.error(LoginActivity.this, "Please, connect to Internet", Toast.LENGTH_SHORT, true).show();

                }

            }

        });

    }
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RC_SIGN_IN){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result){
        if(result.isSuccess()){
            GoogleSignInAccount account = result.getSignInAccount();
            idToken = account.getIdToken();
            name = account.getDisplayName();
            email = account.getEmail();
            Uri personPhoto = account.getPhotoUrl();

            UsernameTv.setText(name);
            emailIdTv.setText(email);


            try{
                Glide.with(this).load(account.getPhotoUrl()).centerCrop().into(profile_image);
            }catch (NullPointerException e){
                Toasty.error(LoginActivity.this, "image not found", Toast.LENGTH_SHORT, true).show();
            }


            //  profile_image.setImageURI(account.getPhotoUrl());
            signInButton.setVisibility(View.GONE);
            logOut.setVisibility(View.GONE);// hide log out as of now
            Log.d(TAG, "ID: "+idToken+"name: "+name+"email: "+email+"imageUrl"+personPhoto);
            // you can store user data to SharedPreference
            // editor.putString("imagePreferance", encodeTobase64());
            editor.putString("User_name",name);
            editor.putString("Email_id",email);
            editor.putString("Image_url", String.valueOf(personPhoto));
            editor.apply();

            AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
            firebaseAuthWithGoogle(credential);
        }else{
            // Google Sign In failed, update UI appropriately
            Log.e(TAG, "Login Unsuccessful. "+result);
            // Toast.makeText(this, "Sign In Unsuccessful", Toast.LENGTH_SHORT).show();
            Toasty.error(LoginActivity.this, "Sign In Unsuccessful, Please try again.", Toast.LENGTH_SHORT, true).show();
            progressDialog.dismiss();
            if(!isNetworkAvailable()){
                // Toast.makeText(this, "Please, connect to Internet", Toast.LENGTH_SHORT).show();
                Toasty.error(LoginActivity.this, "Please, connect to Internet", Toast.LENGTH_SHORT, true).show();

            }
        }
    }
    private void firebaseAuthWithGoogle(AuthCredential credential) {

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                        if (task.isSuccessful()) {
                            // Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                            Toasty.success(LoginActivity.this, "Sign In successful!", Toast.LENGTH_SHORT, true).show();
                            progressDialog.dismiss();
                        } else {
                            Log.w(TAG, "signInWithCredential" + task.getException().getMessage());
                            task.getException().printStackTrace();
//                            Toast.makeText(MainActivity.this, "Authentication failed.",
//                                    Toast.LENGTH_SHORT).show();
                            Toasty.error(LoginActivity.this, "Authentication failed", Toast.LENGTH_SHORT, true).show();

                        }

                    }
                });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (authStateListener != null){
            // FirebaseAuth.getInstance().signOut();
        }
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }



}
