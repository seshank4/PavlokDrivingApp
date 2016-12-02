package edu.bu.cs591.ateam.pavlokdrivingapp;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.StrictMode;
import android.preference.PreferenceManager;
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

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;

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
    public static int userId = 0;
    public static final String MY_PREFS = "";
    private String BASE_URL = "api.tomtom.com/";
    private int VERSION_NUMBER = 2;
    private String EXT = "json/"; // the extension of the response. (json, jsonp, js, or xml)
    private String API_KEY = "h8fxx4ptxbtb4y7xv5r9x7ga";



    //Tomtom
    private LocationManager locationManager;
    private LocationListener locationListener = null;
    private LocationListener vehicleSpeedLL = null;

    private Button btnTomTom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //SharedPreferences prefs = this.getSharedPreferences("edu.bu.cs591.ateam.pavlokdrivingapp",Context.MODE_PRIVATE);
        //prefs.edit().putInt("userId",0).apply();

        locationManager = (LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener(MainActivity.this);
        vehicleSpeedLL = new myVehicleSpeedLL();
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
        boolean isRedirect = false;
        if(bundle != null) {
            isRedirect = bundle.getBoolean("isRedirect");
            this.code = bundle.getString("code");
        }

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        if(!isRedirect) {
            isRedirect = false;
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
        }

        final LinearLayout activity_main = (LinearLayout) findViewById(R.id.activity_main);

            final String authCode =this.code;
            btn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    //runtime permissions check
                    btn.setVisibility(View.INVISIBLE);
                    stopBtn.setVisibility(View.VISIBLE);


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
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, vehicleSpeedLL);
                    }
                    MyLocationListener.flag = false;
                    SpeedCheckTask task = new SpeedCheckTask(authCode);
                    task.execute();
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
                        if(null != locationManager && null != locationListener && null != vehicleSpeedLL) {
                            MyLocationListener.stopFlag=true;
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 0, locationListener);
                            Location destLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            double destLat = destLocation.getLatitude();
                            double destLong = destLocation.getLongitude();
                            final String string_url = "https://" + BASE_URL + "search/" + VERSION_NUMBER + "/reverseGeocode/" +
                                    destLat + "," + destLong + "." + EXT + "?key=" + API_KEY + "&returnSpeedLimit=true"
                                    + "&returnRoadUse=true" + "&roadUse=" + "[\"Arterial\"]";
                            //StrictMode stuff has to be here because there was an error being thrown.
                            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                            StrictMode.setThreadPolicy(policy);

                            // Creating the URL object to pass to the HTTP request function
                            // must put in try catch since url may be invalid
                            URL url = null;
                            try {
                                url = new URL(string_url);
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }

                            Log.e("in SpeedCheckTask.get",url.toString());
//        Toast.makeText(context, "lat and long are" + longitude + "," + latitude, Toast.LENGTH_SHORT).show();

        /*//sb variable is for testing
        StringBuilder sb = new StringBuilder();
        //pass url and sb to getHTTPConnection
        sb = getHttpURLConnection(url,sb);*/
                            HttpURLConnection connection;
                            TomTomResponse responseObj = null;
                            try {
                                connection = (HttpURLConnection) url.openConnection();

                                ObjectMapper mapper = new ObjectMapper();
                                try {
                                    System.out.println(connection.getResponseCode());
                                    //responseObj = mapper.readValue(connection.getInputStream(), TomTomResponse.class);
                                    JsonNode node  = mapper.readTree(connection.getInputStream());
                                    JsonNode subNode = node.get("addresses").get(0).get("address");
                                    responseObj = mapper.readValue(subNode,TomTomResponse.class);
                                    // subNode.get("address")
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                String destAddr = "";
                                String destSubDiv = "";
                                Date endTime = null;
                                destAddr = responseObj.getFreeformAddress();
                                destSubDiv = responseObj.getMunicipalitySubdivision();
                                endTime = Calendar.getInstance().getTime();
                                insertDestInfo(destAddr,destSubDiv,String.valueOf(destLat),String.valueOf(destLong),endTime);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            locationManager.removeUpdates(locationListener);
                            locationManager.removeUpdates(vehicleSpeedLL);

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
    private void insertDestInfo(String destAddr, String destSubDiv, String destLat, String destLong, Date destTime) {
        Statement stmt = null;
        Connection conn= null;
        java.sql.Timestamp sqlDate = new java.sql.Timestamp(destTime.getTime());
        int userId=0;
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("edu.bu.cs591.ateam.pavlokdrivingapp",Context.MODE_PRIVATE);
        int destTripId = prefs.getInt("tripId",0);
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://pavlokdb.cwxhunrrsqfb.us-east-2.rds.amazonaws.com:3306", "ateam", "theateam");
            stmt = conn.createStatement();
            conn.setAutoCommit(false);
            stmt.executeUpdate("update pavlokdb.trip_summary set destination_addr='"+destAddr+"',trip_end_dt='"+sqlDate+"',dest_subdiv='"+destSubDiv+"',dest_lat='"+destLat+"',dest_long='"+destLong+"' where trip_id = "+destTripId);

            conn.commit();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

            Intent intent = new Intent(MainActivity.this,MainActivity.class);
            //intent.putExtra("startBtnVisibility",View.INVISIBLE);
            intent.putExtra("isRedirect",true);
            intent.putExtra("code",this.code);
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

    class myVehicleSpeedLL implements LocationListener{

        @Override
        public void onLocationChanged(Location location) {
            SpeedCheckTask.vehicleSpeed = location.getSpeed();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }
}
