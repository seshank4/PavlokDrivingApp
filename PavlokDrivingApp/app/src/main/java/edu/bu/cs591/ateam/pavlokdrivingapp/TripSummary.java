package edu.bu.cs591.ateam.pavlokdrivingapp;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

/**
 *  various pieces of code taken from previous homework assignments
 */

public class TripSummary extends AppCompatActivity implements OnMapReadyCallback {

    final static String MYTAG = "PAVLOK";

    //TODO: acquire trip id from main activity
    final static int TripID = 1;

    //TODO: pull trip info from database using trip id & user id

    // load start & end locations for a trip to set map view
    final static double[] START = {42.361727, -71.189173};
    final static double[] END = {42.350672, -71.103538};

    // FIND CENTER POINT
    double cLong = ((START[1] + END[1])/2);
    double cLat = ((START[0] + END[0])/2);
    LatLng CENTER = new LatLng(cLat, cLong);

    // load lat/long points into array to place pointers on map
    final static double[][] INFRACTIONS = { {42.359002, -71.178088}, {42.363133, -71.142637}, {42.351294, -71.105314} };


    private GoogleMap mMap;
    private CameraUpdate camUpdate;

    private GoogleApiClient client;
    private LocationManager lm;
    private LocationListener ll;
    Date tripStartTime;
    Date tripEndTime;
    String sourceAddr="";
    String destAddr = "";
    String sourceSubDiv="";
    String destSubDiv="";

    double sourceLat = 0.0;
    double sourceLong = 0.0;
    double destLat = 0.0;
    double destLong = 0.0;

    TextView tvTitle;
    Button btnReload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        Log.i(MYTAG, "onCreate Called.");



        Bundle bundle = getIntent().getExtras();
        int tripId = 0;
        if(bundle != null){
            tripId = bundle.getInt("tripId");
        }

        try {
            getSourceDestLoc(tripId);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        tvTitle = (TextView)findViewById(R.id.tvTitle);
        btnReload = (Button)findViewById(R.id.btnReload);
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        ll = new MyLocationListener();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        btnReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(CENTER, 12));
            }
        });
    }

    private void getSourceDestLoc(int tripId) throws SQLException {
        Connection conn = null;
        try {
            int count = 0;
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://pavlokdb.cwxhunrrsqfb.us-east-2.rds.amazonaws.com:3306", "ateam", "theateam");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT trip_start_dt,trip_end_dt,source_addr,source_subdiv,source_lat,source_long,destination_addr,dest_subdiv,dest_lat,dest_long FROM pavlokdb.trip_summary WHERE trip_id = '"+tripId+"'");
            if (rs.next()) {
                tripStartTime=rs.getDate("trip_start_dt");
                tripEndTime=rs.getDate("trip_end_dt");
                sourceAddr = rs.getString("source_addr");
                destAddr = rs.getString("destination_addr");
                sourceSubDiv = rs.getString("source_subdiv");
                destSubDiv = rs.getString("dest_subdiv");
                sourceLat = rs.getDouble("source_lat");
                destLat = rs.getDouble("dest_lat");
                sourceLong = rs.getDouble("source_long");
                destLong = rs.getDouble("dest_long");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            conn.close();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(MYTAG, "onResume Called, Requesting Location Updates");
        try {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2500L, 0.0f, ll);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2500L, 0.0f, ll);
        }catch(SecurityException e){

        }
    }

    //INNER CLASS.
    class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            float lat = (float) location.getLatitude();
            float lon = (float) location.getLongitude();

            float[] resultsReturn = {-1.0f};
            Location lastLocation = null;
            try {
                lastLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            } catch (SecurityException e) {
            }

            if (lastLocation != null) {  //ref: http://stackoverflow.com/questions/13814928/getlastknownlocation-from-network-provider-on-phone-returns-null
                LatLng prevLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                location.distanceBetween(prevLatLng.latitude, prevLatLng.longitude, lat, lon, resultsReturn);
            }

            LatLng curLatLng = new LatLng(lat, lon);

            gotoLocation(cLat, cLong, 12);
            Log.i(MYTAG, "Location Has Changed. (" + lat + ", " + lon + ")");
            Log.i(MYTAG, "Distance moved (in meters) = " + resultsReturn[0]);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.i(MYTAG, "Location Provider Status Has Changed. " + provider);
        }

        public void onProviderEnabled(String provider) {
            Log.i(MYTAG, "Location Provider Has been DISabled. " + provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.i(MYTAG, "Location Provider Has been ENabled. " + provider);
        }
    }

    //----this is what happens when a language doesn(Java) 't have default parms! icky... --------//
    void gotoLocation(double aLat, double aLong) {
        gotoLocation(aLat, aLong, 12, "BU Headquarters");
    }

    void gotoLocation(double aLat, double aLong, int aZoom) {
        gotoLocation(aLat, aLong, aZoom, "BU Headquarters");
    }

    void gotoLocation(double aLat, double aLong, String aStrLoc) {
        gotoLocation(aLat, aLong, 12, aStrLoc);
    }

    void gotoLocation(double aLat, double aLong, int aZoom, String aStrLoc) {
        LatLng latLng = new LatLng(aLat, aLong);
        camUpdate = CameraUpdateFactory.newLatLngZoom(latLng, aZoom);
        mMap.animateCamera(camUpdate);
        mMap.addMarker(new MarkerOptions().position(latLng).title(aStrLoc));
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i(MYTAG, "onMapReady Called.");
        mMap = googleMap;

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CENTER, 12));
        LatLng start = new LatLng(START[0], START[1]);
        LatLng end = new LatLng(END[0], END[1]);
        mMap.addMarker(new MarkerOptions().position(start).title("Starting point").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        for (int i = 0; i < INFRACTIONS.length; i++) {
            LatLng point = new LatLng(INFRACTIONS[i][0], INFRACTIONS[i][1]);
            mMap.addMarker(new MarkerOptions().position(point).title("Infraction " + i).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
        }
        mMap.addMarker(new MarkerOptions().position(end).title("Destination").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        Log.i(MYTAG, "Center point: " + CENTER.toString());

//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(MYTAG, "onStart Called.");
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Maps Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://edu.bu.cs591.ateam.pavlokdrivingapp/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(MYTAG, "onStop Called.");
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Maps Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://edu.bu.cs591.ateam.pavlokdrivingapp/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
