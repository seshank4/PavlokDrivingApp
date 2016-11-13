package edu.bu.cs591.ateam.pavlokdrivingapp;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.RelativeLayout;

import java.net.URI;

public class MainActivity extends AppCompatActivity {

    private final String APP_ID = "8882d3c9f67eff55ff7b0c535d2a6ccd189d47cd7a7b42c531ad25d413baadd4";
    private final String redirectURI = "http://pavlok-bu-cs591/auth/pavlok/result";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = (Button)findViewById(R.id.btnOauthTest);

        final RelativeLayout activity_main = (RelativeLayout) findViewById(R.id.activity_main);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String page = "http://pavlok-mvp.herokuapp.com/oauth/authorize?client_id="+APP_ID+"&redirect_uri="+redirectURI+"&response_type=code";
                Uri uri = Uri.parse(page);
                WebView webView = new WebView(MainActivity.this);

                WebViewClient client = new WebViewClient(){
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {


                        String url = request.getUrl().toString();

                        if(url.contains("pavlok-bu-cs591/auth/pavlok/result")){

                            handleUri(request.getUrl());
                            return false;
                        }else {

                            return super.shouldOverrideUrlLoading(view, request);
                        }
                    }
                };
                webView.setWebViewClient(client);


                webView.requestFocus(View.FOCUS_DOWN);

                Intent intent = new Intent(Intent.ACTION_VIEW,uri);

                final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        getWindowManager().getDefaultDisplay().getWidth(),
                        getWindowManager().getDefaultDisplay().getHeight());

                //startActivity(intent);
              //  final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(300,300);

                webView.loadUrl(page);

                Dialog dialog = new Dialog(MainActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                dialog.addContentView(webView,params);
                dialog.show();

            }
        });
    }


    protected void handleUri(Uri uri) {

            String code  = uri.getQueryParameter("code");


            doBeep(code);

            Intent intent = new Intent(MainActivity.this,MainActivity.class);
            startActivity(intent);

    }

    private String doBeep(String code) {

        String token = "";

        PavlokConnection conn = new PavlokConnection();

        conn.execute("beep",255,code);



        return token;
    }
}
