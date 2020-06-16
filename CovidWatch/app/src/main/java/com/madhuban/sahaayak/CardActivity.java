package com.madhuban.sahaayak;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.madhuban.sahaayak.UpdateCheck.VersionChecker;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import es.dmoral.toasty.Toasty;

import static android.widget.Toast.LENGTH_LONG;

public class CardActivity extends BaseNavigationActivity implements BottomNavigationView.OnNavigationItemSelectedListener {


    //bottom navigation method
    @Override
    public int getContentViewId() {
        return R.layout.activity_card;
    }

    @Override
    public int getNavigationMenuItemId() {
        return R.id.home;
    }


    Handler handler = new Handler();
    int status = 0;

    ProgressDialog progressdialog;

    LinearLayout mainView,infoLayout,secLayout;
    RelativeLayout relativeLayout;
    ImageButton infoBtn;
    CardView moh_cad,dashboard_card,covid_faq,who_page,state_data_card,twitter_card;
    TextView activeCaseTv,curedCasesTv,deathCaseTv,infoTimeupdateTv;
    String active,cured,death;
    Button reloadBtn;
    String pConcatenated="";
    String qC="";
    String rC="";
    String  timeStamp="";
    WebView webView;
    ProgressBar pbar;
    AppUpdateManager appUpdateManager;
    int UPDATE_REQUEST_CODE=111;
    private ListView list;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> arrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_card);

        mainView=findViewById(R.id.mainView);
        twitter_card=findViewById(R.id.twitter_card);
        secLayout=findViewById(R.id.secLayout);
        moh_cad=findViewById(R.id.moh_cad);
        //state_data_card=findViewById(R.id.state_data);
        who_page=findViewById(R.id.who_page);
        covid_faq=findViewById(R.id.covid_faq);
        dashboard_card=findViewById(R.id.dashboard);
        activeCaseTv=findViewById(R.id.active_case);
        curedCasesTv=findViewById(R.id.cured_cases);
        deathCaseTv=findViewById(R.id.death);
        infoTimeupdateTv=findViewById(R.id.timeStamp);
        relativeLayout=findViewById(R.id.webparent);

        list = (ListView) findViewById(R.id.listview);
        arrayList = new ArrayList<String>();

        // Adapter: You need three parameters 'the context, id of the layout (it will be where the data is shown),
        // and the array that contains the data
        adapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_spinner_item, arrayList);

        // Here, you set the data in your ListView
        list.setAdapter(adapter);

       // requestPermission();
       // getData();
        moh_cad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoLayout.setVisibility(View.GONE);
                startActivity(new Intent(CardActivity.this,MohWeb.class));
                finish();
            }
        });
//        state_data_card.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String URL="https://www.mohfw.gov.in/pdf/DistrictWiseList324.pdf";
//                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(URL)));
//
//
//            }
//        });

        dashboard_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getData();
            }
        });

        covid_faq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoLayout.setVisibility(View.GONE);
              String URL="https://www.mohfw.gov.in/pdf/FAQ.pdf";
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(URL)));


            }
        });

        who_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoLayout.setVisibility(View.GONE);
                startActivity(new Intent(CardActivity.this,ChatBot.class));
                finish();
            }
        });
        twitter_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String URL="https://twitter.com/MoHFW_INDIA";
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(URL)));

            }
        });

        //https://www.mohfw.gov.in/pdf/DistrictWiseList324.pdf"   "https://twitter.com/MoHFW_INDIA" twitter
        new MyAyncTask().execute();

        webView=findViewById(R.id.tnp_webview);
        pbar=findViewById(R.id.progressBar1);

       // CreateProgressDialog();


        //pbar.setVisibility(View.VISIBLE);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        webView.clearCache(true);
        webView.clearHistory();
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.setVerticalScrollBarEnabled(true);
        webView.setHorizontalScrollBarEnabled(true);
        webView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
//        webView.setWebViewClient(new WebViewClient() {
//
//            @Override
//            public void onPageFinished(WebView view, String url)
//            {
//
//                webView.loadUrl("javascript:(function() { " +
//                    "document.getElementsByTagName('header')[0].style.display='none'; " +
//                    "document.getElementsByClassName('footer-section')[0].style.display='none'; " +
//                    "document.getElementById('map').style.display='none'; " +
//                   "document.getElementsByClassName('barchart')[0].style.display='block'; " +
//                    "document.getElementsByClassName('col-md-12 ')[0].style.display='none'; " +
//                    "})()");
//
//
//            }
//        });
       // webView.loadUrl("https://www.mohfw.gov.in/dashboard/index.php#_ABSTRACT_RENDERER_ID_1");


        if (isNetworkAvailable())
        {
            webView.loadUrl("https://www.mohfw.gov.in/index.php");

        }
        else
        {
            //  Snackbar.make(Apollo_Risk_Scan.this,"Please connect to internet...",Snackbar.LENGTH_LONG).show();
            Toast.makeText(this,"Please connect to internet!",LENGTH_LONG).show();
        }

        infoLayout = (LinearLayout)findViewById(R.id.infoLayout);
        infoBtn=findViewById(R.id.infoBtn);
        final Animation slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
        final Animation slideDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);

        infoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                if(infoLayout.getVisibility()==View.GONE){
//
//                    infoLayout.startAnimation(slideUp);
//                    infoLayout.setVisibility(View.VISIBLE);
//                    webView.setAlpha(.5f);
//                }else
//                {
//                    infoLayout.startAnimation(slideDown);
//                    infoLayout.setVisibility(View.GONE);
//                    webView.setAlpha(1);
//                }
            }
        });




    }

    public class WebViewClient extends android.webkit.WebViewClient
    {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            // TODO Auto-generated method stub

            pbar.setVisibility(View.VISIBLE);
           // ShowProgressDialog();
            webView.setAlpha(0.1f);
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            // TODO Auto-generated method stub
            view.loadUrl(url);
            return true;
        }
        @Override
        public void onPageFinished(WebView view, String url) {

            // TODO Auto-generated method stub

            super.onPageFinished(view, url);
            webView.loadUrl("javascript:(function() { " +
                    "document.getElementsByTagName('header')[0].style.display='none'; " +
                    "document.getElementsByClassName('footer-section')[0].style.display='none'; " +
                    "document.getElementById('map').style.display='none'; " +
                    "document.getElementsByClassName('barchart')[0].style.display='block'; " +
                    "document.getElementsByClassName('col-md-6')[0].style.display='none'; "+
                    "document.getElementById('legend')[0].style.display='none'; "+
                    "document.getElementsByClassName('col-md-6 scrollrow')[0].style.display='none'; " +
                    "document.getElementsByClassName('col-md-12 ')[0].style.display='none'; " +
                    "})()");
            pbar.setVisibility(View.GONE);

            webView.setAlpha(1);

        }

    }


    public void CreateProgressDialog()
    {

        progressdialog = new ProgressDialog(CardActivity.this);

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

    @Override
    protected void onStart() {
        super.onStart();
        inAppUpdate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //getData();

        if (!isNetworkAvailable()){

            activeCaseTv.setText("00");
            curedCasesTv.setText("00");
            deathCaseTv.setText("00");

           // Snackbar.make(mainView,"INTERNET disabled, Please connect to the Internet", BaseTransientBottomBar.LENGTH_INDEFINITE);
           // Snackbar.make(findViewById(android.R.id.content), "INTERNET disabled, Please connect to the Internet!", Snackbar.LENGTH_INDEFINITE).setAction("OK",);
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

       // blickInfoIcon();

        secLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(infoLayout.getVisibility()==View.VISIBLE) {
                    infoLayout.setVisibility(View.GONE);
                    webView.setAlpha(1);
                }

            }
        });

        mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(infoLayout.getVisibility()==View.VISIBLE) {
                    infoLayout.setVisibility(View.GONE);
                    webView.setAlpha(1);
                }

            }
        });


    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    void getData(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                final StringBuilder builder = new StringBuilder();

                try {
                    String url="https://www.mohfw.gov.in/";//your website url
                    Document doc = Jsoup.connect(url).get();

                    Element body = doc.body();
                    builder.append(body.text());
                    Elements links = doc.select("a[href]");
                    Elements media = doc.select("[src]");
                    Elements imports = doc.select("link[href]");
                    Elements divs = doc.select("div#main-content");

                    Elements activeCases = doc.getElementsByClass("bg-blue");
                    Elements curedCases = doc.getElementsByClass("bg-green");
                    Elements deathCase = doc.getElementsByClass("bg-red");

                    Elements p= activeCases.tagName("strong");
                    Elements q= curedCases.tagName("strong");
                    Elements r= deathCase.tagName("strong");



                    String pConcatenated="";
                    for (Element x: p) {
                        pConcatenated+= x.text();
                    }
                    String qC="";
                    for (Element x: q) {
                        qC+= x.text();
                    }
                    String rC="";
                    for (Element x: r) {
                        rC+= x.text();
                    }

                    active= pConcatenated.replaceAll("[^0-9]", "");
                    activeCaseTv.setText(active);
                    activeCaseTv.setSelected(true);

                    cured= qC.replaceAll("[^0-9]", "");
                    curedCasesTv.setText(cured);

                    death= rC.replaceAll("[^0-9]", "");
                    deathCaseTv.setText(death);




//                     curedCasesTv.setSelected(true);

//                     deathCaseTv.setSelected(true);


                    Log.e("active", active);
                    Log.e("cured", cured);
                    Log.e("death", death);
                    Log.e("p", pConcatenated+"  "+qC+"  "+rC);



                    for (Element div : divs)
                        System.out.println(div.text());
                    Log.e("div",divs.text());

                    Log.e("media ", String.valueOf(media));
                    Log.e("links ", String.valueOf(links));




                } catch (Exception e) {
                    builder.append("Error : ").append(e.getMessage()).append("\n");
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //result.setText(builder.toString());
                        Log.e("response",builder.toString());

                    }
                });
            }
        }).start();
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
                    isPerpermissionForAllGranted=false;
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                if(isPerpermissionForAllGranted){
                    //shoro();
                    // startLocation();


                }
                break;
        }
    }

    public class MyAyncTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected void onPreExecute() {
            // Here you can show progress bar or something on the similar lines.
            // Since you are in a UI thread here.
            activeCaseTv.setText("");
            curedCasesTv.setText("");
            deathCaseTv.setText("");
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // After completing execution of given task, control will return here.
            // Hence if you want to populate UI elements with fetched data, do it here.

            active= pConcatenated.replaceAll("[^0-9]", "");
            activeCaseTv.setText(active);
           // activeCaseTv.setSelected(true);

            cured= qC.replaceAll("[^0-9]", "");
            curedCasesTv.setText(cured);

            death= rC.replaceAll("[^0-9]", "");
            deathCaseTv.setText(death);

            infoTimeupdateTv.setText(timeStamp);

            Log.e("active", active);
            Log.e("cured", cured);
            Log.e("death", death);
            Log.e("p", pConcatenated+"  "+qC+"  "+rC);

            arrayList.add(active);
            arrayList.add(cured);
            arrayList.add(death);
            adapter.notifyDataSetChanged();

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            // You can track you progress update here
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Here you are in the worker thread and you are not allowed to access UI thread from here.
            // Here you can perform network operations or any heavy operations you want.

            final StringBuilder builder = new StringBuilder();

            try {
                String url="https://www.mohfw.gov.in/";//your website url
                Document doc = Jsoup.connect(url).get();

                Element body = doc.body();
                builder.append(body.text());
                Elements links = doc.select("a[href]");
                Elements media = doc.select("[src]");
                Elements imports = doc.select("link[href]");
                Elements divs = doc.select("div#main-content");


                Elements stateData = doc.getElementsByClass("table table-striped");
               // String x=stateData.attr()
                Elements rows = doc.select("tr");
                for(org.jsoup.nodes.Element row :rows)
                {
                    org.jsoup.select.Elements columns = row.select("td");
                    for (org.jsoup.nodes.Element column:columns)
                    {
                        System.out.print(column.text());
                        Log.e("tDAta", String.valueOf(column.text()));
                    }
                    System.out.println();
                }

                StringBuilder str = new StringBuilder();
                Element tableHeader = doc.select("tr").get(1);
                for( Element element : tableHeader.children() )
                {
                    // Here you can do something with each element
                    System.out.println(element.text());
                    Log.e("dDAta", String.valueOf(element.text()));


                }
                Log.e("stateDAta", String.valueOf(stateData));

                Elements activeCases = doc.getElementsByClass("bg-blue");
                Elements curedCases = doc.getElementsByClass("bg-green");
                Elements deathCase = doc.getElementsByClass("bg-red");

                Elements timestamp = doc.getElementsByClass("status-update");
                Elements t= timestamp.tagName("span");
                Elements p= activeCases.tagName("strong");
                Elements q= curedCases.tagName("strong");
                Elements r= deathCase.tagName("strong");


                for (Element x: t) {

                    timeStamp+= x.text();
                }

                for (Element x: p) {
                    pConcatenated+= x.text();
                }

                for (Element x: q) {
                    qC+= x.text();
                }

                for (Element x: r) {
                    rC+= x.text();
                }


                for (Element div : divs)
                    System.out.println(div.text());
                Log.e("div",divs.text());

                Log.e("media ", String.valueOf(media));
                Log.e("links ", String.valueOf(links));
                Log.e("timestamp ", String.valueOf(timeStamp));
                Log.e("t ", String.valueOf(t));




            } catch (Exception e) {
                builder.append("Error : ").append(e.getMessage()).append("\n");
            }
            return null;
        }
    }

    void blickInfoIcon(){

        Animation animation = new AlphaAnimation(1, 0); //to change visibility from visible to invisible
        animation.setDuration(1000); //1 second duration for each animation cycle
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE); //repeating indefinitely
        animation.setRepeatMode(Animation.REVERSE); //animation will start from end point once ended.
        infoBtn.startAnimation(animation); //t
    }

    private void inAppUpdate() {
        // Creates instance of the manager.
        appUpdateManager = AppUpdateManagerFactory.create(this);

        // Returns an intent object that you use to check for an update.
        com.google.android.play.core.tasks.Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
            @Override
            public void onSuccess(AppUpdateInfo appUpdateInfo) {

                Log.e("AVAILABLE_VERSION_CODE", appUpdateInfo.availableVersionCode()+"");
                Log.e("APP_VERSION_CODE",BuildConfig.VERSION_CODE+"APP_VERSION_NAME"+BuildConfig.VERSION_NAME);
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                        && appUpdateInfo.clientVersionStalenessDays() != null
                        && appUpdateInfo.clientVersionStalenessDays() >= 1
                        // && appUpdateInfo.updatePriority() >= 5
                        // For a flexible update, use AppUpdateType.FLEXIBLE
                        && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    // Request the update.

                    try {
                        appUpdateManager.startUpdateFlowForResult(
                                // Pass the intent that is returned by 'getAppUpdateInfo()'.
                                appUpdateInfo,
                                // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                                AppUpdateType.IMMEDIATE,
                                // The current activity making the update request.
                                CardActivity.this,
                                // Include a request code to later monitor this update request.
                                UPDATE_REQUEST_CODE);
                    } catch (IntentSender.SendIntentException ignored) {

                    }
                }
            }
        });

        appUpdateManager.registerListener(installStateUpdatedListener);

    }


    InstallStateUpdatedListener installStateUpdatedListener = new
            InstallStateUpdatedListener() {
                @Override
                public void onStateUpdate(InstallState state) {
                    if (state.installStatus() == InstallStatus.DOWNLOADED){
                        popupSnackbarForCompleteUpdate();
                    } else if (state.installStatus() == InstallStatus.INSTALLED){
                        if (appUpdateManager != null){
                            appUpdateManager.unregisterListener(installStateUpdatedListener);
                        }

                    } else {
                        //Log.i(TAG, "InstallStateUpdatedListener: state: " + state.installStatus());
                    }
                }
            };

    private void popupSnackbarForCompleteUpdate() {

        Snackbar snackbar =
                Snackbar.make(
                        findViewById(android.R.id.content),
                        "Update almost finished!",
                        Snackbar.LENGTH_INDEFINITE);
        //lambda operation used for below action
        snackbar.setAction(this.getString(R.string.restart), view ->
        {
            if (appUpdateManager != null){
                appUpdateManager.completeUpdate();
            }
        });
        snackbar.setActionTextColor(getResources().getColor(R.color.blue));
        snackbar.show();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //DebugLog.debug(TAG, "onCreateOptionsMenu Called. ");


            getMenuInflater().inflate(R.menu.card_menu, menu);
            return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.info:
                if(infoLayout.getVisibility()==View.GONE){

                    infoLayout.setVisibility(View.VISIBLE);
                    webView.setAlpha(.5f);
                }else
                {
                    infoLayout.setVisibility(View.GONE);
                    webView.setAlpha(1);
                }
                return true;
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
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);



    }
    public void BackToHome()
    {
        Intent intent = new Intent(CardActivity.this,CardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
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
                    Toasty.info(CardActivity.this, "Alredy on latest version!", Toast.LENGTH_SHORT, true).show();

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


        AlertDialog.Builder dialog = new AlertDialog.Builder(CardActivity.this);
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
                        Toasty.info(CardActivity.this, "Please update to latest version", Toast.LENGTH_SHORT, true).show();

                        dialog.dismiss();
                    }
                });

        final AlertDialog alert = dialog.create();
        alert.show();
    }

}
