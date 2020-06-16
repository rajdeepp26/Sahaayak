package com.madhuban.sahaayak;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import es.dmoral.toasty.Toasty;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesProvider;

import static android.Manifest.permission_group.STORAGE;


public class MainActivity extends BaseNavigationActivity implements BottomNavigationView.OnNavigationItemSelectedListener, OnLocationUpdatedListener {


    //bottom navigation method
    @Override
    public int getContentViewId() {
        return R.layout.activity_main;
    }

    @Override
    public int getNavigationMenuItemId() {
        return R.id.store;
    }

    RelativeLayout storeSample,recyclerParent;

    Handler handler = new Handler();
    int status = 0;

    ProgressDialog progressdialog;


    String TAG=MainActivity.class.getSimpleName();
    FloatingActionButton floatingActionButton,reloadBtn;
    Double getLat,getLong;
    String CityName,LocalityName,PostalCode,FeatureName,SubLocality="",StateName="",DistrictName="";
    StringBuilder result;
    EditText editText;
    TextView banner;
    String store_address,store_name="abc";
    private LocationGooglePlayServicesProvider provider;
    RecyclerView_Adapter adapter;

    public ArrayList<StorePOJO> storePOJOS;
    private static final int LOCATION_PERMISSION_ID = 1001;
    private static final int CAMERA_PERMISSION_ID = 1002;

    private Bitmap bitmap;
    private File destination = null;
    private InputStream inputStreamImg;
    private String imgPath = null;
    private final int PICK_IMAGE_CAMERA = 1, PICK_IMAGE_GALLERY = 2;
    private Uri filePath;
    byte[] imageBytes;
    String imageEncoded;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef,getMyRef;
    boolean isStoreDatainDb=false;
    String username,userEmailId,uniqueIdToFb;
    private StorageReference mStorageRef;
    String getName,getStoreName,fetchStoreName;
    String getMAil;
    String getAddress;
    String getImage;
    String getTime;
    Bitmap img= null;
    RecyclerView recyclerView;
    ProgressBar progressBar;
    String NetworkError="<b> <font color='#F44336'>Please connect to the Internet</font></b>";
    int storeId=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG,"onCreate_called");

        progressBar=findViewById(R.id.pbar);
        banner=findViewById(R.id.banner);
        reloadBtn=findViewById(R.id.reload);
        floatingActionButton=findViewById(R.id.fab);
        storeSample=findViewById(R.id.storeSample);
        recyclerParent=findViewById(R.id.recyclerviewParent);
       // editText=findViewById(R.id.edit_store_name);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        storePOJOS= new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerview);
        preferences =getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        editor = preferences.edit();

       // progressBar.setVisibility(View.VISIBLE);
   //     ObjectAnimator.ofInt(progressBar, "progress", 90).start();

       // requestPermission();
        initRecyclerView();
      //  CreateProgressDialog();
        username=preferences.getString("User_name","");
        userEmailId=preferences.getString("Email_id","");
        startLocation();
        Log.e("userInfo",username+" ,"+userEmailId);

         floatingActionButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {

                 startLocation();

                showForgotDialog(MainActivity.this);

//                 Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                 startActivityForResult(takePicture, 0);//zero can be replaced with any action code (called requestCode)

             }
         });
        reloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isNetworkAvailable()){

                    Toast.makeText(MainActivity.this,"Refreshing...",Toast.LENGTH_LONG).show();
                    pullData();


                }else {
                    banner.setText(Html.fromHtml(NetworkError), TextView.BufferType.SPANNABLE);

                }


            }
        });



        //Toast.makeText(MainActivity.this,"Please, connect to Internet",Toast.LENGTH_LONG).show();


           // Toasty.error(MainActivity.this, "Please, connect to Internet", Toast.LENGTH_SHORT, true).show();


        // new  AsyncCaller().execute();





    }
    private void showForgotDialog(Context c) {
        final EditText taskEditText = new EditText(c);
        AlertDialog dialog = new AlertDialog.Builder(c)
                .setTitle("Give a name")
                .setMessage(" Enter service name")
                .setView(taskEditText)
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getStoreName = String.valueOf(taskEditText.getText());
                        Log.e("estoreName",getStoreName);
                        selectImage();
                        storeId++;
                        editor.putInt("storeId",storeId);
                        editor.apply();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }
    void requestPermission(){
        if (Build.VERSION.SDK_INT >= 23) {

            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED &&checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Log.v(TAG,"Permission is granted");

            } else {

              //  Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA,Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
           // Log.v(TAG,"Permission is granted");

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0:
                boolean isPerpermissionForAllGranted = false;
                if (grantResults.length > 0 && permissions.length==grantResults.length) {
                    for (int i = 0; i < permissions.length; i++){
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED){
                            isPerpermissionForAllGranted=true;
                        }else{
                            isPerpermissionForAllGranted=false;
                        }
                    }

                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    isPerpermissionForAllGranted=true;
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                if(isPerpermissionForAllGranted){
                    //shoro();
                    startLocation();

                }
                break;
        }
    }

    private void initRecyclerView() {



            recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(),
                    DividerItemDecoration.VERTICAL));
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setItemViewCacheSize(10);
            recyclerView.setDrawingCacheEnabled(true);
            recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_AUTO);
            recyclerView.setSaveEnabled(true);
            recyclerView.setVerticalScrollBarEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            recyclerView.setTouchscreenBlocksFocus(true);
        }


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

        smartLocation.location(provider).start(MainActivity.this);



    }



    private void selectImage() {
        try {
            PackageManager pm = getPackageManager();
            int hasPerm = pm.checkPermission(Manifest.permission.CAMERA, getPackageName());
            if (hasPerm == PackageManager.PERMISSION_GRANTED) {
                final CharSequence[] options = {"Take Photo", "Choose From Gallery","Cancel"};
               AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Select Option");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (options[item].equals("Take Photo")) {
                            dialog.dismiss();
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent, PICK_IMAGE_CAMERA);
                        } else if (options[item].equals("Choose From Gallery")) {
                            dialog.dismiss();
                            Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(pickPhoto, PICK_IMAGE_GALLERY);
                        } else if (options[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            } else
                Toast.makeText(this, "Camera Permission error", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Camera Permission error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        inputStreamImg = null;
        if (requestCode == PICK_IMAGE_CAMERA) {
            try {
                Uri selectedImage = data.getData();
                bitmap = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bytes);
                imageBytes = bytes.toByteArray();
                Log.e("Activity", "Pick from Camera::>>> ");
                Log.d(TAG,"Picked from Camera");
                Log.e("CAM_bitmap", String.valueOf(bitmap));
                encodeBitmapAndSaveToFirebase(bitmap);

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                destination = new File(Environment.getExternalStorageDirectory() + "/" +
                        getString(R.string.app_name), "IMG_" + timeStamp + ".jpg");
//                FileOutputStream fo;
//                try {
//                    destination.createNewFile();
//                    fo = new FileOutputStream(destination);
//                    fo.write(bytes.toByteArray());
//                    fo.close();
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                imgPath = destination.getAbsolutePath();
//
                filePath= Uri.parse(imgPath);
                 // filePath=selectedImage;
//                Log.e("CAM_Imgbytes", String.valueOf(imageBytes));
                Log.e("CAM_dest", String.valueOf(destination));
                Log.e("CAM_imagPath",imgPath);
                Log.d("CfilePath", String.valueOf(filePath));
                Log.d("CAM_selectedImage", String.valueOf(selectedImage));

 //               storePOJOS.add(new StorePOJO(store_name,bitmap,store_address));
                //uploadImage();
                //sendImagetoFb();
              //  adapter.notifyDataSetChanged();
               // imageview.setImageBitmap(bitmap);
               // pullData();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestCode == PICK_IMAGE_GALLERY) {
            Uri selectedImage = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bytes);
                Log.e("GAL_bitmap", String.valueOf(bitmap));
                encodeBitmapAndSaveToFirebase(bitmap);
                Log.e("Activity", "Pick from Gallery::>>> ");
                Log.d(TAG,"Picked from Gallery");
//                imageBytes = bytes.toByteArray();
//
//                imgPath = getRealPathFromURI(selectedImage);
//
//                destination = new File(imgPath.toString());
//                Log.e("GAL_Imgbytes", String.valueOf(imageBytes));
//                Log.e("GAL_dest", String.valueOf(destination));
//                Log.e("GAL_imagPath",imgPath);
//                Log.d("GfilePath", String.valueOf(filePath));
//                Log.d("GAL_selectedImage", String.valueOf(selectedImage));
//                filePath= Uri.fromFile(destination);
//               // imageview.setImageBitmap(bitmap);
 //               storePOJOS.add(new StorePOJO(store_name,bitmap,store_address));
                //uploadImage();
               // sendImagetoFb();
//                adapter.notifyDataSetChanged();
               // pullData();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (provider != null) {
            provider.onActivityResult(requestCode, resultCode, data);
        }
    }


    public String GetFileExtension(Uri uri) {

        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri)) ;

    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Audio.Media.DATA};
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    // UploadImage method
    private void uploadImage()
    {
        if (filePath != null) {

            // Code for showing progressDialog while uploading
            final ProgressDialog progressDialog
                    = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            // Defining the child of storageReference
            StorageReference ref
                    = mStorageRef
                    .child(
                            "images/"+String.valueOf(filePath)
                                    + "."+GetFileExtension(filePath));

            // adding listeners on upload
            // or failure of image
            ref.putFile(filePath)
                    .addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {

                                @Override
                                public void onSuccess(
                                        UploadTask.TaskSnapshot taskSnapshot)
                                {

                                    // Image uploaded successfully
                                    // Dismiss dialog
                                    progressDialog.dismiss();
                                    Toast
                                            .makeText(MainActivity.this,
                                                    "Image Uploaded!!",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                }
                            })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {

                            // Error, Image not uploaded
                            progressDialog.dismiss();
                            Toast
                                    .makeText(MainActivity.this,
                                            "Failed " + e.getMessage(),
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    })
                    .addOnProgressListener(
                            new OnProgressListener<UploadTask.TaskSnapshot>() {

                                // Progress Listener for loading
                                // percentage on the dialog box
                                @Override
                                public void onProgress(
                                        UploadTask.TaskSnapshot taskSnapshot)
                                {
                                    double progress
                                            = (100.0
                                            * taskSnapshot.getBytesTransferred()
                                            / taskSnapshot.getTotalByteCount());
                                    progressDialog.setMessage(
                                            "Uploaded "
                                                    + (int)progress + "%");
                                }
                            });
        }
    }


    @Override
    public void onLocationUpdated(Location location) {
        getAddress();
    }

    public void sendImagetoFb() {

        if (imageBytes != null) {

            final ProgressDialog progressDialog
                    = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            Log.e("imagebytes", String.valueOf(imageBytes));
            StorageReference storageReference2 = mStorageRef.child(System.currentTimeMillis() + "." + GetFileExtension(filePath));
            storageReference2.putBytes(imageBytes)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                           // String TempImageName = editText.getText().toString().trim();
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Image Uploaded Successfully ", Toast.LENGTH_LONG).show();
                           // @SuppressWarnings("VisibleForTests")
                            //uploadinfo imageUploadInfo = new uploadinfo(TempImageName, taskSnapshot.getUploadSessionUri().toString());
//                            String ImageUploadId = databaseReference.push().getKey();
//                            databaseReference.child(ImageUploadId).setValue(imageUploadInfo);
                        }
                    });
        }
        else {

            Toast.makeText(MainActivity.this, "Please Select Image or Add Image Name", Toast.LENGTH_LONG).show();

        }
    }

    public void encodeBitmapAndSaveToFirebase(Bitmap bitmap) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
//        DatabaseReference ref = FirebaseDatabase.getInstance()
//                .getReference(Constants.FIREBASE_CHILD_RESTAURANTS)
//                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                .child(mRestaurant.getPushId())
//                .child("imageUrl");
//        ref.setValue(imageEncoded);
        Log.e("encodedImg",imageEncoded);

        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateformat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss aa");
        String datetime = dateformat.format(c.getTime());


        int storeid= preferences.getInt("storeId",1);
        uniqueIdToFb=userEmailId.replaceAll("[^a-zA-Z]", "");

        final String pa=uniqueIdToFb+"/"+storeid;

        final String path=datetime+"/";
        myRef=database.getReference().child("data").child(StateName).child(DistrictName).child(SubLocality).child(path);

//        StorePOJO stP=new StorePOJO();
//
//        stP.setImage(imageEncoded);
//        stP.setLocation(store_address);
//        stP.setTime(datetime);
//        stP.setUploader(username);
//        myRef.setValue(stP);

        myRef.child("Username").setValue(username);
        myRef.child("Email_id").setValue(userEmailId);
        myRef.child("Time").setValue(datetime);
        myRef.child("Address").setValue(LocalityName);
        myRef.child("image").setValue(imageEncoded);
        myRef.child("store_name").setValue(getStoreName);




        Log.d(TAG,"Data sent to fb");
        isStoreDatainDb=true;
        editor.putBoolean("isStoreDatainDb",isStoreDatainDb);
        editor.apply();


    }

    public static Bitmap decodeFromFirebaseBase64(String image) throws IOException {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);

    }

    void getImage(){

//            try {
//                Bitmap image = decodeFromFirebaseBase64(mRestaurant.getImageUrl());
//                mImageLabel.setImageBitmap(image);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }


      //  } else {
            // This block of code should already exist, we're just moving it to the 'else' statement:
//            Picasso.with(view.getContext())
//                    .load(mRestaurant.getImageUrl())
//                    .resize(MAX_WIDTH, MAX_HEIGHT)
//                    .centerCrop()
//                    .into(mImageLabel);

    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    @Override
    protected void onResume() {
        super.onResume();

       // pullData();
        SubLocality=preferences.getString("SubLocality","");
        isStoreDatainDb=preferences.getBoolean("isStoreDatainDb",false);
        Log.e("Sub",SubLocality);
        String styledText = "<b><font color='#fecd0d'>my</font><font color='#25a3d6'>GeoTracking</font> </b> is now called  <b> '<font color='#fecd0d'>all</font><font color='#25a3d6'>Geo</font>'</b>\n";
      //  allGeoTextView.setText(Html.fromHtml(styledText), TextView.BufferType.SPANNABLE);
        String txt="These are the active services in "+"<b> <font color='#F44336'>"+SubLocality+"</font></b>"+" area.";
        String tx="No service found in"+"<b> <font color='#F44336'>"+SubLocality+"</font></b>"+" area.";



        if (isStoreDatainDb){
            banner.setText(Html.fromHtml(txt), TextView.BufferType.SPANNABLE);
        }else{


            banner.setText(Html.fromHtml(tx), TextView.BufferType.SPANNABLE);



//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    // progressDialog.dismiss();
//
//                    if (isNetworkAvailable()){
//                        // SubLocality=preferences.getString("SubLocality","");
//
//                        pullData();
//
//                    }else banner.setText(Html.fromHtml(NetworkError), TextView.BufferType.SPANNABLE);
//
//                }
//            }, 1500);
        }

        if (!isNetworkAvailable()){
            banner.setText(Html.fromHtml(NetworkError), TextView.BufferType.SPANNABLE);

            final Snackbar snackBar = Snackbar.make(findViewById(android.R.id.content), "Internet disabled, Please connect to the Internet!",Snackbar.LENGTH_INDEFINITE);

            snackBar.setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Call your action method here
                    snackBar.dismiss();
                }
            });
            //snackBar.setBackgroundTint(getResources().getColor(R.color.yellow));
            snackBar.show();
        }

        if (isNetworkAvailable()){
            StateName=preferences.getString("StateName","");
            DistrictName=preferences.getString("DistrictName","");
            SubLocality=preferences.getString("SubLocality","");
            Log.d("StateNAme ",StateName);
            if (!StateName.isEmpty()&&!DistrictName.isEmpty()&&!SubLocality.isEmpty()){
                recyclerParent.setVisibility(View.VISIBLE);
                storeSample.setVisibility(View.GONE);

              //  ShowProgressDialog();
                pullData();
            }else {

                String tex="No Stores Found in"+"<b> <font color='#F44336'>"+SubLocality+"</font></b>"+" area.";
                banner.setText(Html.fromHtml(tex), TextView.BufferType.SPANNABLE);

                storeSample.setVisibility(View.VISIBLE);
                if (recyclerParent.getVisibility()==View.GONE)
                {
                    progressBar.setVisibility(View.GONE);
                    recyclerParent.setVisibility(View.VISIBLE);
                }

            }


        }else banner.setText(Html.fromHtml(NetworkError), TextView.BufferType.SPANNABLE);



    }

    private Bitmap decode(String getImage){
        Bitmap decodedByte;
        //Log.e("decode_str",getImage);
        byte[] decodedString = Base64.decode(getImage, Base64.DEFAULT);

        decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        return decodedByte;

    }

    void pullData(){
        uniqueIdToFb=userEmailId.replaceAll("[^a-zA-Z]", "");
        final String path=SubLocality+"/";
        getMyRef=database.getReference().child("data").child(StateName).child(DistrictName).child(SubLocality);
//        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
//        DatabaseReference databaseReference =    mFirebaseDatabase.getReference().child("data");
        getMyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Log.e("node", String.valueOf(dataSnapshot));
                new AsyncCaller().execute(dataSnapshot);
                progressBar.setVisibility(View.GONE);

//                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
//                    Log.v(TAG,"key"+ childDataSnapshot.getKey()); //displays the key for the node
//                    Log.v(TAG,"child"+ childDataSnapshot);
//                    //gives the value for given keyname
//
////                    getName= (String) childDataSnapshot.child("Username").getValue();
////                    getMAil= (String) childDataSnapshot.child("Email_id").getValue();
////                    getAddress= (String) childDataSnapshot.child("Address").getValue();
////                    getImage= (String) childDataSnapshot.child("image").getValue();
////                    getTime= (String) childDataSnapshot.child("Time").getValue();
////                    fetchStoreName= (String) childDataSnapshot.child("store_name").getValue();
//                    new AsyncCaller().execute(childDataSnapshot);
//                    progressBar.setVisibility(View.GONE);
//              //      storePOJOS.add(new StorePOJO(fetchStoreName,getImage,getAddress,getName,getTime));
//                   // addToList();
//                    Log.e("datasnap", String.valueOf(dataSnapshot));
//                    Log.e("fetchInfo",getName+" "+getMAil+" "+getTime+" "+getAddress+" "+getImage);
//                    Log.d(TAG,"fetching_from_fb");
//                   // Log.e("storeName",fetchStoreName);
//                    Log.d(TAG,"storName"+fetchStoreName);
//                    //Log.d(TAG,"fetching"+storedata);
//                }
                Log.e("fetchInfo",getName+" "+getMAil);

//                adapter = new RecyclerView_Adapter(storePOJOS, getApplication());
//                recyclerView.setAdapter(adapter);
//                adapter.notifyDataSetChanged();
//                progressBar.setVisibility(View.GONE);

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




    }
    void checkDuplicates(){

        getMyRef=database.getReference().child("data");
        getMyRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    Log.v(TAG,"key"+ childDataSnapshot.getKey()); //displays the key for the node
                    Log.v(TAG,"child"+ childDataSnapshot);
                    //gives the value for given keyname

//                    getName= (String) childDataSnapshot.child("Username").getValue();
//                    getMAil= (String) childDataSnapshot.child("Email_id").getValue();
//                    getAddress= (String) childDataSnapshot.child("Address").getValue();
//                    getImage= (String) childDataSnapshot.child("image").getValue();
//                    getTime= (String) childDataSnapshot.child("Time").getValue();
//
//
                    StorePOJO storedata=dataSnapshot.getValue(StorePOJO.class);
                    storePOJOS.add(storedata);
                   // addToList();
                    Log.e("datasnap", String.valueOf(dataSnapshot));
                    Log.e("fetchInfo",getName+" "+getMAil+" "+getTime+" "+getAddress+" "+getImage);
                    Log.d(TAG,"fetching_from_fb");
                }
                Log.e("fetchInfo",getName+" "+getMAil);

                adapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void addToList(){


        try {

            img = decodeFromFirebaseBase64(getImage);
           // storePOJOS.add(new StorePOJO(store_name,img,getAddress,getName,getTime));
            adapter.notifyDataSetChanged();

            Log.d(TAG,"AddedToList");

        } catch (IOException e) {
            e.printStackTrace();
        }catch (NullPointerException e){

//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    // progressDialog.dismiss();
//
//                }
//            }, 2000);

        }

    }

    private  class AsyncCaller extends AsyncTask<DataSnapshot, Void, String>
    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();




        }
        @Override
        protected String doInBackground(DataSnapshot... params) {



            //this method will be running on background thread so don't update UI frome here
            //do your long running http tasks here,you dont want to pass argument and u can access the parent class' variable url over here
            storePOJOS.removeAll(storePOJOS);
            storePOJOS.clear();
            for (DataSnapshot childDataSnapshot : params[0].getChildren()) {

                getName= (String) childDataSnapshot.child("Username").getValue();
                getMAil= (String) childDataSnapshot.child("Email_id").getValue();
                getAddress= (String) childDataSnapshot.child("Address").getValue();
                getImage= (String) childDataSnapshot.child("image").getValue();
                getTime= (String) childDataSnapshot.child("Time").getValue();
                fetchStoreName= (String) childDataSnapshot.child("store_name").getValue();
                storePOJOS.add(new StorePOJO(fetchStoreName,getImage,getAddress,getName,getTime));
            }



            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
//            ProgressBarAnimation anim = new ProgressBarAnimation(progressBar, 0, 300*100);
//            anim.setDuration(5000);
//            progressBar.setMax(100);
//            progressBar.startAnimation(anim);

//            ObjectAnimator.ofInt(progressBar, "progress", 10000)
//                    .setDuration(3000)
//                    .start();

 //           ObjectAnimator.ofInt(progressBar, "progress", 90).start();

          //  progressBar.setProgress(100);


        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            //this method will be running on UI thread


            adapter = new RecyclerView_Adapter(storePOJOS, getApplication());
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            if (adapter.getItemCount()!=0){

                recyclerParent.setVisibility(View.VISIBLE);
                storeSample.setVisibility(View.GONE);

                Log.d("itemCount", String.valueOf(adapter.getItemCount()));

                String txt="These are the active services in "+"<b> <font color='#F44336'>"+SubLocality+"</font></b>"+" area.";
                banner.setText(Html.fromHtml(txt), TextView.BufferType.SPANNABLE);
            }else {

                String tx="No active service found in"+"<b> <font color='#F44336'>"+SubLocality+"</font></b>"+" area.";

                banner.setText(Html.fromHtml(tx), TextView.BufferType.SPANNABLE);

                storeSample.setVisibility(View.VISIBLE);
                if (recyclerParent.getVisibility()==View.GONE)
                {
                    recyclerParent.setVisibility(View.VISIBLE);
                }

            }




        }

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        //DebugLog.debug(TAG, "onCreateOptionsMenu Called. ");
//
//
//            getMenuInflater().inflate(R.menu.menu, menu);
//            return true;
//
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        switch (item.getItemId())
//        {
//            case R.id.refresh:
//                pullData();
//                return true;
//
//
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        BackToHome();
        finishAffinity();
    }
    public void BackToHome()
    {
        Intent intent = new Intent(this,CardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }


    public void CreateProgressDialog()
    {

        progressdialog = new ProgressDialog(MainActivity.this);

        progressdialog.setIndeterminate(false);

        progressdialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        progressdialog.setCancelable(true);

        progressdialog.setMax(100);

        progressdialog.show();

    }

    public void ShowProgressDialog()
    {
        status = 0;

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(status < 100){

                    status +=1;

                    try{
                        Thread.sleep(50);
                    }catch(InterruptedException e){
                        e.printStackTrace();
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            progressdialog.setProgress(status);

                            if(status == 100){


                                progressdialog.dismiss();
                            }
                        }
                    });
                }
            }
        }).start();

    }
}
