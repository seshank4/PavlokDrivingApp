package edu.bu.cs591.ateam.pavlokdrivingapp;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
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

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

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
    private static final int GPS_REQUEST_CODE = 10;

    //Tomtom
    private LocationManager locationManager;
    private LocationListener locationListener = null;
    private LocationListener vehicleSpeedLL = null;

    private Button btnTomTom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();
        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    GPS_REQUEST_CODE);
        }

        final Button btn = (Button)findViewById(R.id.btnOauthTest);
        final Button stopBtn = (Button)findViewById(R.id.btnStopTrip);
        btnTomTom = (Button) findViewById(R.id.btnTomTom);

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

            final String authCode =this.code;
            btn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    //runtime permissions check
                    btn.setVisibility(disableStart);
                    if (disableStart == View.VISIBLE) {
                        stopBtn.setVisibility(View.VISIBLE);
                    } else {
                        stopBtn.setVisibility(View.INVISIBLE);
                    }
                    String page = "http://pavlok-mvp.herokuapp.com/oauth/authorize?client_id=" + APP_ID + "&redirect_uri=" + redirectURI + "&response_type=code";
                    Uri uri = Uri.parse(page);
                    WebView webView = new WebView(MainActivity.this);
                    WebViewClient client = new WebViewClient() {
                        @Override
                        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                            String url = request.getUrl().toString();
                            if (url.contains("pavlok-bu-cs591/auth/pavlok/result")) {
                                handleUri(request.getUrl());
                                return false;
                            } else {
                                return super.shouldOverrideUrlLoading(view, request);
                            }
                        }

                        @Override
                        public boolean shouldOverrideUrlLoading(WebView view, String url) {

                            if (url.contains("pavlok-bu-cs591/auth/pavlok/result")) {
                                handleUri(Uri.parse(url));
                                return false;
                            } else {
                                return super.shouldOverrideUrlLoading(view, url);
                            }
                        }
                    };
                    webView.setWebViewClient(client);
                    webView.requestFocus(View.FOCUS_DOWN);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                            getWindowManager().getDefaultDisplay().getWidth(),
                            getWindowManager().getDefaultDisplay().getHeight());
                    webView.loadUrl(page);
                    Dialog dialog = new Dialog(MainActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                    dialog.addContentView(webView, params);
                    dialog.show();

                    if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    }
                    //if permissions have already been granted, grab a reference to the class defined
                    // MyLocationListener
                    else {
                        // gets the gps coords every 5 seconds and when you have moved more than 1 meter
                        // leave at 0 for testing
                        Log.e("calling requestlocation", "calling requestlocation");
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100000, 0, locationListener);
                    }
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
                    SpeedCheckTask.stopTrip = true;
                    btn.setVisibility(View.VISIBLE);
                    stopBtn.setVisibility(View.INVISIBLE);
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                GPS_REQUEST_CODE);
                    }else {
                        if(null != locationManager && null != locationListener) {
                            locationManager.removeUpdates(locationListener);
                        }
                        Log.d("location", "removed updates successfulyy");
                    }
                }
            });

            mDrawerList = (ListView) findViewById(R.id.navList);
            // Set the adapter for the list view
            String[] osArray = {"Log Out"};
            mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
            mDrawerList.setAdapter(mAdapter);
            // Set the list's click listener
            mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Toast.makeText(MainActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();
                    if (((TextView) view).getText().toString().equals("Log Out")) {
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // clear back stack
                        startActivity(intent);
                    }
                }
            });
            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                    R.string.drawer_open, R.string.drawer_close) {
                /**
                 * Called when a drawer has settled in a completely open state.
                 */
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    getSupportActionBar().setTitle("Navigation!");
                    invalidateOptionsMenu();
                }

                /**
                 * Called when a drawer has settled in a completely closed state.
                 */
                public void onDrawerClosed(View view) {
                    super.onDrawerClosed(view);
                    getSupportActionBar().setTitle(mActivityTitle);
                    invalidateOptionsMenu();
                }
            };
            mDrawerToggle.setDrawerIndicatorEnabled(true);
            mDrawerLayout.setDrawerListener(mDrawerToggle);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults){

        switch (requestCode) {
            case GPS_REQUEST_CODE: {
                // If user grants the app access, we can use the requested service.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Toast.makeText(MainActivity.this, "in else activity granted", Toast.LENGTH_LONG).show();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void handleUri(Uri uri) {
            this.code  = uri.getQueryParameter("code");
            SpeedCheckTask task = new SpeedCheckTask(this.code);
            task.execute();
            Intent intent = new Intent(MainActivity.this,MainActivity.class);
            intent.putExtra("startBtnVisibility",View.INVISIBLE);
            startActivity(intent);
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
