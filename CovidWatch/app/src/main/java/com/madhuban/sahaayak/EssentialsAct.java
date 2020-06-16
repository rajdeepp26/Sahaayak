package com.madhuban.sahaayak;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.madhuban.sahaayak.AppIntro.WelcomeActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

import static android.Manifest.permission_group.STORAGE;

public class EssentialsAct extends BaseNavigationActivity implements BottomNavigationView.OnNavigationItemSelectedListener {


    //bottom navigation method
    @Override
    public int getContentViewId() {
        return R.layout.activity_essentials;
    }

    @Override
    public int getNavigationMenuItemId() {
        return R.id.essentials;
    }

    RelativeLayout storeSample,recyclerParent;
    LinearLayout active_user_layout,mainView;
    ImageView active_userBtn;


    TextView banner,active_userText;
    String TAG=EssentialsAct.class.getSimpleName();
    FloatingActionButton floatingActionButton,reloadBtn;
    StringBuilder result;
    String store_address,store_name="abc";
    Essentials_issue_Adapter adapter;
    public ArrayList<Essen_issuePOJO> essen_issuePOJOS;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef,getMyRef,ref,updatedb,updateIssueData;
    String Username,userEmailId,UserMessage,tok;
    String getMessage,fetchMessage,getName,getIssueId,getAdmin,issueStatus,getAdmin_token;
    String getMAil;
    String getAddress;
    String getTime;
    String SubLocality="",StateName="",DistrictName="",uniqueIdToFb="";
    long issueId=1,active_userCount=5;
    int dbCount=0;

    String admin_token=" ";
    boolean isDatainDb=false;

    RecyclerView recyclerView;
    String NetworkError="<b> <font color='#F44336'>Please connect to the Internet</font></b>";

    private RequestQueue mRequestQue;
    private String URL = "https://fcm.googleapis.com/fcm/send";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG,"onCreate_called");

        banner=findViewById(R.id.banner);
        floatingActionButton=findViewById(R.id.fab);
        // editText=findViewById(R.id.edit_store_name);
        storeSample=findViewById(R.id.issueSample);
        recyclerParent=findViewById(R.id.recyclerviewParent);
        essen_issuePOJOS= new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerview);
        reloadBtn=findViewById(R.id.reload);
        active_user_layout=findViewById(R.id.active_user_Layout);
        active_userBtn=findViewById(R.id.active_users);
        active_userText=findViewById(R.id.active_userText);
        mainView=findViewById(R.id.mainView);

        mRequestQue = Volley.newRequestQueue(this);

        initRecyclerView();

        preferences =getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        editor = preferences.edit();

        tok=preferences.getString("Token","");
        Username=preferences.getString("User_name","");
        Cons.UserName=Username;
        userEmailId=preferences.getString("Email_id","");
        Cons.UserEmailId=userEmailId;
        store_address=preferences.getString("User_address","");
        Log.d("issue_add",store_address);
        SubLocality=preferences.getString("SubLocality","");
        StateName=preferences.getString("StateName","");
        DistrictName=preferences.getString("DistrictName","");




        Log.e("userInfo",Username+" ,"+userEmailId+","+SubLocality);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                showForgotDialog(EssentialsAct.this);

//                 Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                 startActivityForResult(takePicture, 0);//zero can be replaced with any action code (called requestCode)

            }
        });

        reloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable())
                {
                    Toast.makeText(EssentialsAct.this,"Refreshing...",Toast.LENGTH_LONG).show();
                    pullData();


                }else {
                    banner.setText(Html.fromHtml(NetworkError), TextView.BufferType.SPANNABLE);

                }

            }
        });

        if (isNetworkAvailable()){
            StateName=preferences.getString("StateName","");
            DistrictName=preferences.getString("DistrictName","");
            SubLocality=preferences.getString("SubLocality","");
            if (!SubLocality.isEmpty()){
                pullData();
            }else {

                String tx="No issues reported from your "+"<b> <font color='#F44336'>"+SubLocality+"</font></b>"+" area.";
                banner.setText(Html.fromHtml(tx), TextView.BufferType.SPANNABLE);

            }


        }else banner.setText(Html.fromHtml(NetworkError), TextView.BufferType.SPANNABLE);

        //Toasty.error(EssentialsAct.this, "Please, connect to Internet", Toast.LENGTH_SHORT, true).show();

        final Animation slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
        final Animation slideDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);

        active_userCount =preferences.getLong("issueId",5);
        active_userBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (active_userCount<=10){

                    active_userCount=10;
                }

                String sc="<font color='#000'>Users count from </font>"+"<b> <font color='#F44336'>"+SubLocality+"</font></b>"+" <font color='#000'>is</font>"+"<b> <font color='#F44336'>"+active_userCount+"</font></b>"+"<font color='#000'>. <br>Kindly share the app and be a</font>"+"<b> <font color='#F44336'>"+"Sahaayak"+"</font></b>"+"<font color='#000'>. </font></br>";
                active_userText.setText(Html.fromHtml(sc), TextView.BufferType.SPANNABLE);
                Snackbar snackbar;
                snackbar = Snackbar.make(mainView,  Html.fromHtml(sc), Snackbar.LENGTH_LONG);
                snackbar.setBackgroundTint(ContextCompat.getColor(EssentialsAct.this, R.color.white));
                snackbar.show();
//                Snackbar.make(mainView, Html.fromHtml(sc), Snackbar.LENGTH_LONG)
//                        .show();



            }
        });



    }

    @Override
    protected void onResume() {
        super.onResume();

        SubLocality=preferences.getString("SubLocality","");
        isDatainDb=preferences.getBoolean("isDatainDb",false);
      //  SubLocality=preferences.getString("SubLocalityName","");
        String txt="Showing issues of people in"+"<b> <font color='#F44336'>"+SubLocality+"</font></b>"+" area.";
        String str="No issues posted in "+"<b> <font color='#F44336'>"+SubLocality+"</font></b>"+" area.";
        if (isDatainDb){

            banner.setText(Html.fromHtml(txt), TextView.BufferType.SPANNABLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // progressDialog.dismiss();

                    if (isNetworkAvailable()){
                        // SubLocality=preferences.getString("SubLocality","");

                        pullData();
                        recyclerParent.setVisibility(View.VISIBLE);
                        storeSample.setVisibility(View.GONE);

                    }else {
                        banner.setText(Html.fromHtml(NetworkError), TextView.BufferType.SPANNABLE);
                        storeSample.setVisibility(View.VISIBLE);
                        if (recyclerParent.getVisibility()==View.GONE)
                        {
                            recyclerParent.setVisibility(View.VISIBLE);
                        }
                    }

                }
            }, 1000);

        }else{

            banner.setText(Html.fromHtml(str), TextView.BufferType.SPANNABLE);


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

        Log.d("isAdmin", String.valueOf(Cons.isAdmin));
        Log.d("issue_solved", String.valueOf(Cons.issueSolved));

        if (Cons.isAdmin){

            makeAdmin();


        }
        if (Cons.issueSolved){

            solveStatus();
        }


    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    private void initRecyclerView() {



        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(),
                DividerItemDecoration.VERTICAL));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //recyclerView.setItemViewCacheSize(5);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_AUTO);






    }

    private void showForgotDialog(Context c) {
        final EditText taskEditText = new EditText(c);
        AlertDialog dialog = new AlertDialog.Builder(c)
                .setTitle("Enter your message")
                .setMessage(" Type message")
                .setView(taskEditText)
                .setCancelable(false)
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UserMessage = String.valueOf(taskEditText.getText());
                        Log.e("estoreName",UserMessage);
                        if (UserMessage.length()>1)
                        {
                            sentDataTofb();
                            isDatainDb=true;
                            editor.putBoolean("isDatainDb",isDatainDb);
                            editor.apply();
                            getTokenFromFb();

                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }
    void sentDataTofb(){

        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateformat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss aa");
        String datetime = dateformat.format(c.getTime());

        String issId= String.valueOf(preferences.getLong("issueId",1));
        final String path=datetime+"/";

        uniqueIdToFb=userEmailId.replaceAll("[^a-zA-Z]", "");
        Cons.uniquwIdtoFb=uniqueIdToFb;

        int dbId=preferences.getInt("dbCount",0);

        Log.d("dbCount", String.valueOf(dbId+" "+dbCount));

       // final String pa=uniqueIdToFb+"/"+dbId;
        myRef=database.getReference().child("user_issue").child(StateName).child(DistrictName).child(SubLocality).child(path);
        myRef.child("Username").setValue(Username);
        myRef.child("Email_id").setValue(userEmailId);
        myRef.child("Time").setValue(datetime);
        myRef.child("Address").setValue(store_address);
        myRef.child("message").setValue(UserMessage);
        myRef.child("issueId").setValue(issId);
        myRef.child("status").setValue("0");
        myRef.child("isAdmin").setValue("0");
        myRef.child("myToken").setValue(tok);
        myRef.child("saved").setValue("yes",new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                dbCount++;
                editor.putInt("dbCount",dbCount);
                editor.apply();
            }
        });

       // myRef.child("store_name").setValue(getStoreName);

//        dbCount++;
//        editor.putInt("dbCount",dbCount);
//        editor.apply();



    }
    void pullData(){
        uniqueIdToFb=userEmailId.replaceAll("[^a-zA-Z]", "");
        getMyRef=database.getReference().child("user_issue").child(StateName).child(DistrictName).child(SubLocality);
//        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
//        DatabaseReference databaseReference =    mFirebaseDatabase.getReference().child("data");
        getMyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                essen_issuePOJOS.removeAll(essen_issuePOJOS);

                issueId=dataSnapshot.getChildrenCount()+1;
                editor.putLong("issueId",issueId);
                editor.apply();
                Log.e("count",dataSnapshot.getChildrenCount() + "");

                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    Log.v(TAG,"key"+ childDataSnapshot.getKey()); //displays the key for the node
                    Log.v(TAG,"child"+ childDataSnapshot);
                    //gives the value for given keyname



                    getName= (String) childDataSnapshot.child("Username").getValue();
                    getMAil= (String) childDataSnapshot.child("Email_id").getValue();
                    getAddress= (String) childDataSnapshot.child("Address").getValue();
                    getTime= (String) childDataSnapshot.child("Time").getValue();
                    getMessage= (String) childDataSnapshot.child("message").getValue();
                    getIssueId= (String) childDataSnapshot.child("issueId").getValue();
                    issueStatus= (String) childDataSnapshot.child("status").getValue();
                    getAdmin= (String) childDataSnapshot.child("isAdmin").getValue();
                    getAdmin_token=(String)childDataSnapshot.child("myToken").getValue();

                    essen_issuePOJOS.add(new Essen_issuePOJO(getName,getMAil,getMessage,getTime,getAddress,getIssueId,issueStatus,getAdmin,getAdmin_token));
                    // addToList();
                    Log.e("datasnap", String.valueOf(dataSnapshot));
                    Log.e("fetchInfo",getName+" "+getMAil+" "+getTime+" "+getAddress+" "+getMessage+" "+getAdmin+" "+issueStatus);
                    Log.d(TAG,"fetching_from_fb");
                    // Log.e("storeName",fetchStoreName);
                  //  Log.d(TAG,"storName"+fetchStoreName);
                    //Log.d(TAG,"fetching"+storedata);
                }
                Log.e("fetchInfo",getName+" "+getMAil);

                adapter = new Essentials_issue_Adapter(essen_issuePOJOS, getApplication());
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                getItemCount();



            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

                banner.setText("Database error");

            }
        });




    }

    void getItemCount(){

        String txt="Showing issues of people in"+"<b> <font color='#F44336'>"+SubLocality+"</font></b>"+" area.";
        String str="No issues posted in "+"<b> <font color='#F44336'>"+SubLocality+"</font></b>"+" area.";

        if (adapter.getItemCount()!=0){

            banner.setText(Html.fromHtml(txt), TextView.BufferType.SPANNABLE);
            recyclerParent.setVisibility(View.VISIBLE);
            storeSample.setVisibility(View.GONE);


        }else {

            banner.setText(Html.fromHtml(str), TextView.BufferType.SPANNABLE);
            storeSample.setVisibility(View.VISIBLE);
            if (recyclerParent.getVisibility()==View.GONE)
            {
                recyclerParent.setVisibility(View.VISIBLE);
            }
        }


    }


    @Override
    public void onBackPressed() {

        BackToHome();
        finishAffinity();
        // super.onBackPressed();

    }

    public void BackToHome()
    {
        Intent intent = new Intent(EssentialsAct.this,CardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }


    void getTokenFromFb()
    {
        String firebaseId=preferences.getString("firebaseUserId","");
        ref=database.getReference().child("Token");

//        ref.equalTo(firebaseId);
        Log.d("key",ref.getKey());
        Log.d("equals", String.valueOf(ref.equalTo(firebaseId)));
        Log.d("ref", String.valueOf(ref.child(firebaseId)));
        Log.d("fireBaseId",firebaseId);

//        ref.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for (DataSnapshot child : dataSnapshot.getChildren()) {
//
//                    Log.d("child", String.valueOf(child));
//
//                    if (child.getKey().equals(firebaseId)) {
//
//                        String token= (String) child.getValue();
//
//                        Log.d("token",token);
//
//                    }
//
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });


        String myTok=preferences.getString("","");


             new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    // admin_token="c9_REh2JRg6DIw1bEs3-7P:APA91bG8DqMfmbGXo0K1DeZaVRlQz20VTiUJpb4Woz4-Vc4zHN2zcMwad8j_hFDc31Vp1t4OGxZJSR6PwbyOp4-7JS1FtKlCyTqG9AKXMn69WZIuTt9I3DtGSPftQkYy9GmKrfxAwzgR";

                    admin_token=Cons.ADMIN_TOKEN;
                    Log.e("admin","token :"+Cons.ADMIN_TOKEN);
                    sendNotification(admin_token);

                }
             }, 4000);






        Log.d("tok",tok);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    Log.e("login","signed2");
                    String myDevicetoken=tok;
                    Log.e("myDeviceToken",myDevicetoken);
                    sendSelfNotification(myDevicetoken);

                }
            }, 2000);










    }

    private void sendNotification(String s) {

        Log.d("send","Send");

        JSONObject json = new JSONObject();
        try {
            Log.d("token","Admin :"+s);
            json.put("to",s);
            JSONObject notificationObj = new JSONObject();
            notificationObj.put("title","New issue reported");
            notificationObj.put("body","Hey, "+ Username +" posted a new Isuue from "+ StateName+"/" + DistrictName+"/" + SubLocality +". Kindly check and revert back.");

//            JSONObject extraData = new JSONObject();
//            extraData.put("brandId","puma");
//            extraData.put("category","Shoes");



            json.put("notification",notificationObj);
            //  json.put("data",extraData);


            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL,
                    json,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            Log.d("MUR", "onResponse: ");
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("MUR", "onError: "+error.networkResponse);
                }
            }
            ){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String,String> header = new HashMap<>();
                    header.put("content-type","application/json");
                    header.put("authorization","key=AAAA7t2-4ek:APA91bGt0aHFO2pW1xSOoI-buA0TeTVidma9ITrM408ImvZbuYkKhgTGVe6f64kVr5Ii5VwOtB27VZpkXmzpOMV0PUTKQ2JAx7eCqPycgQ3iIUUcxW_0hOqXSCzEp3uwXxWwP0RnGl5F");
                    return header;
                }
            };
            mRequestQue.add(request);
        }
        catch (JSONException e)

        {
            e.printStackTrace();
        }
    }


    private void sendSelfNotification(String s) {

        Log.d("send1","Send");

        JSONObject json = new JSONObject();
        try {
            Log.d("token","self :"+s);
            json.put("to",s);
            JSONObject notificationObj = new JSONObject();
            notificationObj.put("title",Username);
            notificationObj.put("body","Thank you for posting your issue.Soon we will get back to you.");

//            JSONObject extraData = new JSONObject();
//            extraData.put("brandId","puma");
//            extraData.put("category","Shoes");



            json.put("notification",notificationObj);
            //  json.put("data",extraData);


            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL,
                    json,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            Log.d("SELF_MUR", "onResponse: ");
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("MUR", "onError: "+error.networkResponse);
                }
            }
            ){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String,String> header = new HashMap<>();
                    header.put("content-type","application/json");
                    header.put("authorization","key=AAAA7t2-4ek:APA91bGt0aHFO2pW1xSOoI-buA0TeTVidma9ITrM408ImvZbuYkKhgTGVe6f64kVr5Ii5VwOtB27VZpkXmzpOMV0PUTKQ2JAx7eCqPycgQ3iIUUcxW_0hOqXSCzEp3uwXxWwP0RnGl5F");
                    return header;
                }
            };
            mRequestQue.add(request);
        }
        catch (JSONException e)

        {
            e.printStackTrace();
        }
    }



    void makeAdmin(){

        //admin_token=tok;

        if (Cons.isAdmin){

           final int i=Cons.item_position;
                   //preferences.getInt("dbCount",1);
            Log.d("i", String.valueOf(i));
            final String t=Cons.adminTimeStamp+"/";

            final String pa=uniqueIdToFb+"/"+i+"/";
            updatedb=database.getReference().child("user_issue").child(StateName).child(DistrictName).child(SubLocality).child(t);
            HashMap<String, Object> result = new HashMap<>();
            result.put("isAdmin", "1");
            updatedb.updateChildren(result);
            Log.d("changeAdmin", String.valueOf(Cons.isAdmin));



        }



    }

    void solveStatus(){
        final int i=Cons.item_position;
                //preferences.getInt("dbCount",1);
        final String t=Cons.timeStamp+"/";
        Log.d("t",t);

        final String pa=uniqueIdToFb+"/"+i+"/";
        updateIssueData=database.getReference().child("user_issue").child(StateName).child(DistrictName).child(SubLocality).child(t);
        HashMap<String, Object> result = new HashMap<>();
        result.put("status", "1");
        updateIssueData.updateChildren(result);
        Log.d("changeStatus", String.valueOf(Cons.isAdmin));



    }

}
