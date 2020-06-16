package com.madhuban.sahaayak;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import static android.widget.Toast.LENGTH_LONG;

public class ChatBot extends AppCompatActivity {


    //bottom navigation method
//    @Override
//    public int getContentViewId() {
//        return R.layout.chatbot;
//    }
//
//    @Override
//    public int getNavigationMenuItemId() {
//        return R.id.chat;
//    }

    WebView webView;
    ProgressBar pbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatbot);

        webView=findViewById(R.id.tnp_webview);
        pbar=findViewById(R.id.progressBar1);



        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        webView.clearCache(true);
        webView.clearHistory();
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.setVerticalScrollBarEnabled(false);

        if (isNetworkAvailable())
        {
            webView.loadUrl("https://www.who.int/emergencies/diseases/novel-coronavirus-2019");
        }
        else
        {
            //  Snackbar.make(Apollo_Risk_Scan.this,"Please connect to internet...",Snackbar.LENGTH_LONG).show();
            Toast.makeText(this,"Please connect to internet!",LENGTH_LONG).show();
        }

    }
    public class WebViewClient extends android.webkit.WebViewClient
    {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            // TODO Auto-generated method stub
            pbar.setVisibility(View.VISIBLE);

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
            pbar.setVisibility(View.GONE);

        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onBackPressed() {


        if (webView.isFocused() && webView.canGoBack()){
            webView.goBack();
            return;
        }

        BackToHome();
        finishAffinity();
        // super.onBackPressed();

    }

    public void BackToHome()
    {
        Intent intent = new Intent(ChatBot.this,CardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
