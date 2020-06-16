package com.madhuban.sahaayak;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.madhuban.sahaayak.UpdateCheck.VersionChecker;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import es.dmoral.toasty.Toasty;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesProvider;

import static android.Manifest.permission_group.STORAGE;


public abstract class BaseNavigationActivity extends AppCompatActivity implements
        BottomNavigationView.OnNavigationItemSelectedListener, OnLocationUpdatedListener {

    String TAG=BaseNavigationActivity.class.getSimpleName();

    Double getLat,getLong;
    String CityName,LocalityName,PostalCode,FeatureName,SubLocality="",StateName="",DistrictName="";
    StringBuilder result;
    String store_address,store_name="abc";
    private LocationGooglePlayServicesProvider provider;

    FloatingActionButton floatingActionButton;
    protected BottomNavigationView bottomNavigationView;
    public Context mContext;
    String token;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    DatabaseReference myRef;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    String firebaseUserId;
    boolean isSigned;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewId());
        mContext = BaseNavigationActivity.this;


        preferences=getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        editor=preferences.edit();

        bottomNavigationView= (BottomNavigationView) findViewById(R.id.bottomNav);
//         floatingActionButton=findViewById(R.id.fab);



        bottomNavigationView.clearAnimation();
        bottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setItemIconTintList(null);


        token=preferences.getString("Token","");
        isSigned=preferences.getBoolean("isUserSignedIn",false);
        firebaseUserId=preferences.getString("firebaseUserId","");

        startLocation();
        if (token.length() >0 && isSigned &&firebaseUserId.length()>0){

            myRef=database.getReference().child("Token");
            myRef.child(firebaseUserId).setValue(token);

            Log.d("Base_class","token"+token);
            Log.d("Base_class","isSigned"+isSigned);
            Log.d("Base_class","firebaseId"+firebaseUserId);




        }else {

            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this, new OnSuccessListener<InstanceIdResult>() {
                @Override
                public void onSuccess(InstanceIdResult instanceIdResult) {
                    String token = instanceIdResult.getToken();



                    editor.putString("Token",token);
                    editor.apply();

                    if (firebaseUserId.length()>0){

//                        String path="/";
//                        myRef=database.getReference().child("Token");
//                        myRef.child(firebaseUserId).setValue(token);
//                        Log.d("Base_class","askForToken "+token);
                    }


                }
            });
        }






    }



    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()){

                case R.id.home:
                    startActivity(new Intent(this,CardActivity.class));
                    //finish();

                    break;
                case R.id.store:
                    startActivity(new Intent(this,MainActivity.class));
                    finish();

                    break;

                case R.id.essentials:
                    startActivity(new Intent(this, EssentialsAct.class));
                    finish();
                    break;

                case R.id.chat:
                    startActivity(new Intent(this, Apollo_Risk_Scan.class));
                    finish();
                    break;

            }

//            Intent main_activity_intent1 = new Intent(this, MDAMainActivity.class);
//            main_activity_intent1.putExtra("message_screen","launch_message_screen");
//            startActivity(main_activity_intent1);
                // finish();
//          Toast.makeText(getContext(), "base_attach", Toast.LENGTH_SHORT).show();

        return true;


    }
    @Override
    protected void onStart() {
        super.onStart();
        updateNavigationBarState();
    }

    public void updateNavigationBarState() {
        int actionId = getNavigationMenuItemId();
        selectBottomNavigationBarItem(actionId);
    }

    void selectBottomNavigationBarItem(int itemId) {
        Menu menu = bottomNavigationView.getMenu();
        for (int i = 0, size = menu.size(); i < size; i++) {
            MenuItem item = menu.getItem(i);
            boolean shouldBeChecked = item.getItemId() == itemId;
            if (shouldBeChecked) {
                item.setChecked(true);
                break;
            }
        }
    }

    public abstract int getContentViewId();

    public abstract int getNavigationMenuItemId();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //DebugLog.debug(TAG, "onCreateOptionsMenu Called. ");


            getMenuInflater().inflate(R.menu.common_menu, menu);
            return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.about:
                Intent settings_intent_one = new Intent(getApplicationContext(),
                        AboutActivity.class);
                startActivity(settings_intent_one);
                return true;

            case R.id.share:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.madhuban.sahaayak");
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                return true;
            case R.id.update:
                versionCheck();
                return true;


        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onLocationUpdated(Location location) {
        getAddress();
    }

    private String getAdd() {

        result = new StringBuilder();
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(getLat, getLong, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                result.append(address.getAddressLine(0)).append(",");
                result.append(address.getLocality()).append(",");
                result.append(address.getCountryName());
                store_address= String.valueOf(result);
                CityName=address.getLocality();
                PostalCode = addresses.get(0).getPostalCode();
                LocalityName= address.getAddressLine(0); //street address
                FeatureName=address.getFeatureName();          //house name less accurate

                SubLocality=address.getSubLocality();          //locality name like Kormangla
                StateName=address.getAdminArea();              // State name
                DistrictName=address.getSubAdminArea();       // District name

                editor.putString("User_address",LocalityName);
                editor.putString("SubLocality",SubLocality);
                editor.putString("StateName",StateName);
                editor.putString("DistrictName",DistrictName);
                editor.apply();

                // Toast.makeText(MainActivity.this,"Postal "+PostalCode+ "Feature"+FeatureName+"Locality"+LocalityName,Toast.LENGTH_LONG).show();
                Log.d(TAG,"Postal "+PostalCode+ "Feature"+FeatureName+"Locality"+LocalityName+" Sub "+SubLocality+" State "+StateName+" District "+DistrictName);
                Log.e("locate", String.valueOf(address));
                Log.e("state", StateName);
            }
        } catch (IOException e) {
            Log.e("tag", e.getMessage());
        }

        return result.toString();
    }
    private void startLocation() {

        provider = new LocationGooglePlayServicesProvider();
        provider.setCheckLocationSettings(true);

        SmartLocation smartLocation = new SmartLocation.Builder(this).logging(true).build();

        smartLocation.location(provider).start(BaseNavigationActivity.this);



    }

    void getAddress(){

        SmartLocation.with(this).location()
                .oneFix()
                .start(new OnLocationUpdatedListener() {
                    @Override
                    public void onLocationUpdated(Location location) {

                        if (location != null) {
                            getLat=location.getLatitude();
                            getLong=location.getLongitude();
                            //getAdd(getLat,getLong);
                            getAdd();
                        }



                    } });



    }

    void versionCheck(){
        VersionChecker versionChecker = new VersionChecker();
        try {
            String latestVersion = versionChecker.execute().get();
            String versionName = BuildConfig.VERSION_NAME.replace("-DEBUG","");
            if (latestVersion != null && !latestVersion.isEmpty()) {
                if (!latestVersion.matches(versionName)) {
                    showDialogToSendToPlayStore();
                }else{
                    //Toast.makeText(BaseNavigationActivity.this,"Alredy on latest version",Toast.LENGTH_LONG).show();
                    Toasty.info(BaseNavigationActivity.this, "Alredy on latest version!", Toast.LENGTH_SHORT, true).show();

                }
            }
            Log.d("update", "Current version " + versionName + ", Playstore version " + latestVersion);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

    public void showDialogToSendToPlayStore(){


        AlertDialog.Builder dialog = new AlertDialog.Builder(BaseNavigationActivity.this);
        dialog.setCancelable(false);
        dialog.setIcon(R.drawable.splash_icon);
        dialog.setTitle("New version available");
        dialog.setMessage("\n\nTo enjoy the new features update to latest version." );
        dialog.setPositiveButton("UPDATE NOW", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                //Action for "update".
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" +"com.madhuban.sahaayak")));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" +"com.madhuban.sahaayak")));
                }
            }
        })
                .setNegativeButton("Cancel ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Action for "Cancel".
                        // Toast.makeText(MainActivity.this,"Pease update to latest version",Toast.LENGTH_LONG).show();
                        Toasty.info(BaseNavigationActivity.this, "Please update to latest version", Toast.LENGTH_SHORT, true).show();

                        dialog.dismiss();
                    }
                });

        final AlertDialog alert = dialog.create();
        alert.show();
    }

}
