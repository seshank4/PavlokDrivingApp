package edu.bu.cs591.ateam.pavlokdrivingapp;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private final String APP_ID = "8882d3c9f67eff55ff7b0c535d2a6ccd189d47cd7a7b42c531ad25d413baadd4";
    private final String redirectURI = "http://pavlok-bu-cs591/auth/pavlok/result";
    private String code = "";
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;
    public int disableStart = View.VISIBLE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button btn = (Button)findViewById(R.id.btnOauthTest);
        final Button stopBtn = (Button)findViewById(R.id.btnStopTrip);
        Button summary = (Button)findViewById(R.id.btnSummary);


        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            if (View.VISIBLE == bundle.getInt("startBtnVisibility") || View.INVISIBLE == bundle.getInt("startBtnVisibility")) {

                disableStart = bundle.getInt("startBtnVisibility");
                if(disableStart == View.INVISIBLE){
                    btn.setVisibility(View.INVISIBLE);
                    stopBtn.setVisibility(View.VISIBLE);
                }else{
                    btn.setVisibility(View.VISIBLE);
                    stopBtn.setVisibility(View.INVISIBLE);
                }

            }
        }


        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        final LinearLayout activity_main = (LinearLayout) findViewById(R.id.activity_main);
        final SpeedCheckTask task = new SpeedCheckTask();
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                btn.setVisibility(disableStart);
                if(disableStart == View.VISIBLE) {
                    stopBtn.setVisibility(View.VISIBLE);
                }else{
                    stopBtn.setVisibility(View.INVISIBLE);
                }
                task.execute();
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
                Intent intent = new Intent("");
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

        summary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TripSummary.class);
                startActivity(intent);
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                task.stopTrip = true;
                btn.setVisibility(View.VISIBLE);
                stopBtn.setVisibility(View.INVISIBLE);
            }
        });
        mDrawerList = (ListView) findViewById(R.id.navList);
        // Set the adapter for the list view
        String[] osArray = { "Log Out" };
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
        mDrawerList.setAdapter(mAdapter);
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();
                if(((TextView)view).getText().toString().equals("Log Out")){
                    Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // clear back stack
                    startActivity(intent);
                }
//                else {
//                    Intent intent = new Intent(MainActivity.this, );
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);
//                }
            }
        });
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {
            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Navigation!");
                invalidateOptionsMenu();
            }
            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu();
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.my_menu, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void handleUri(Uri uri) {
            this.code  = uri.getQueryParameter("code");
            doBeep(code);
            Intent intent = new Intent(MainActivity.this,MainActivity.class);
            intent.putExtra("startBtnVisibility",View.INVISIBLE);
            startActivity(intent);
    }

    private String doBeep(String code) {
        String token = "";
        PavlokConnection conn = new PavlokConnection();
        conn.execute("beep",255,code);
        return token;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
}
