package edu.bu.cs591.ateam.pavlokdrivingapp;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
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

import org.w3c.dom.Text;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static java.lang.String.valueOf;

/**
 *  various pieces of code taken from previous homework assignments,
 *  including gotoLocation(), MyLocationListener()
 */

public class TripSummary extends AppCompatActivity implements OnMapReadyCallback {

    final static String MYTAG = "PAVLOK";

    // Initialize trip co-ords
    double cLong;
    double cLat;
    LatLng CENTER;
    LatLng start;
    LatLng end;

    private ArrayList<ArrayList<Double>> infractions;
    private int counter = 0;

    private GoogleMap mMap;
    private CameraUpdate camUpdate;

    private GoogleApiClient client;
    private LocationManager lm;
    private LocationListener ll;
    Date tripStartTime;
    Date tripEndTime;
    String sourceAddr = "";
    String destAddr = "";
    String sourceSubDiv = "";
    String destSubDiv = "";

    double sourceLat = 0.0;
    double sourceLong = 0.0;
    double destLat = 0.0;
    double destLong = 0.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        Log.i(MYTAG, "onCreate Called.");

        Bundle bundle = getIntent().getExtras();
        int tripId = 0;
        if (bundle != null) {
            tripId = bundle.getInt("tripId");
        }

        try {
            getSourceDestLoc(tripId);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        populateInfractions(tripId);

        // Create list view of infraction data
        ListView lvSummary = (ListView) findViewById(R.id.lvSummary);
        lvSummary.setAdapter(new MyCustomTripAdapter(this, infractions, tripStartTime, tripEndTime, sourceAddr, destAddr));

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void populateInfractions(int tripId) {

        Connection conn = null;
        infractions = new ArrayList<>();
        try {
            int infractionsCount = 0;
            Class.forName("com.mysql.jdbc.Driver");

            conn = DriverManager.getConnection("jdbc:mysql://pavlokdb.cwxhunrrsqfb.us-east-2.rds.amazonaws.com:3306", "ateam", "theateam");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT latitude,longitude,speed_limit,vehicle_speed FROM pavlokdb.trip_detail WHERE trip_id = '" + tripId + "'");
            while (rs.next()) {
                counter += 1;
                ArrayList<Double> location = new ArrayList<>();
                location.add(rs.getDouble("latitude"));
                location.add(rs.getDouble("longitude"));
                location.add(rs.getDouble("speed_limit"));
                location.add(rs.getDouble("vehicle_speed"));
                infractions.add(location);
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void getSourceDestLoc(int tripId) throws SQLException {
        Connection conn = null;
        try {
            int count = 0;
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://pavlokdb.cwxhunrrsqfb.us-east-2.rds.amazonaws.com:3306", "ateam", "theateam");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT trip_start_dt,trip_end_dt,source_addr,source_subdiv,source_lat,source_long,destination_addr,dest_subdiv,dest_lat,dest_long FROM pavlokdb.trip_summary WHERE trip_id = '" + tripId + "'");
            if (rs.next()) {


                tripStartTime = rs.getDate("trip_start_dt");
                tripEndTime = rs.getDate("trip_end_dt");
                sourceAddr = rs.getString("source_addr");
                destAddr = rs.getString("destination_addr");
                sourceSubDiv = rs.getString("source_subdiv");
                destSubDiv = rs.getString("dest_subdiv");
                sourceLat = rs.getDouble("source_lat");
                destLat = rs.getDouble("dest_lat");
                sourceLong = rs.getDouble("source_long");
                destLong = rs.getDouble("dest_long");

                // Set start & end points
                start = new LatLng(sourceLat, sourceLong);
                end = new LatLng(destLat, destLong);

                // FIND CENTER POINT
                cLat = (sourceLat + destLat) / 2;
                cLong = (sourceLong + destLong) / 2;
                CENTER = new LatLng(cLat, cLong);
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
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i(MYTAG, "onMapReady Called.");
        mMap = googleMap;

        // Center the map view on the calculated center-point, with a default zoom level of 12
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CENTER, 12));
        Log.i(MYTAG, "Center point: " + CENTER.toString());

        // Add a green marker on the map with the starting location
        mMap.addMarker(new MarkerOptions().position(start).title("Starting point").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).draggable(false).snippet("Nearest address: " + sourceAddr));

        // Add orange markers on the map for each infraction, labeled in order of occurrence
        for (int i = 0; i < infractions.size(); i++) {
            LatLng point = new LatLng(infractions.get(i).get(0), infractions.get(i).get(1));
            mMap.addMarker(new MarkerOptions().position(point).title("Infraction " + i).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)).draggable(false).snippet("Speed limit: " + infractions.get(i).get(2) + "mph, Your speed: " + infractions.get(i).get(3) + "mph"));
        }

        // Add a red marker on the map with the destination location
        mMap.addMarker(new MarkerOptions().position(end).title("Destination").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).draggable(false).snippet("Nearest address: " + destAddr));
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(MYTAG, "onStart Called.");
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW,
                "Maps Page",
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
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
                Action.TYPE_VIEW,
                "Maps Page",
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                Uri.parse("android-app://edu.bu.cs591.ateam.pavlokdrivingapp/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}


// Custom Adapter for displaying trip summary information by infraction #
class MyCustomTripAdapter extends BaseAdapter {
    private static ArrayList<ArrayList<Double>> infractions;
    private LayoutInflater mInflater;
    private Date sTime;
    private Date eTime;
    private String sAddr;
    private String eAddr;

    public MyCustomTripAdapter(Context context, ArrayList<ArrayList<Double>> results,
                               Date startTime, Date endTime, String startAddr, String endAddr) {
        infractions = results;
        sTime = startTime;
        eTime = endTime;
        sAddr = startAddr;
        eAddr = endAddr;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {return 0;}
        if (position == getCount()-1) {return 2;}
        else {return 1; }
    }

    public int getCount() {
        return infractions.size()+2;
    }

    public Object getItem(int position) {
        return infractions.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        Log.i("PAVLOK COUNT", valueOf(getCount()));
        Log.i("PAVLOK POSITION", valueOf(position));
        int type = this.getItemViewType(position);
        Log.i("PAVLOK TYPE", valueOf(type));
        switch (type){
            /*
             *  For the first item in the list view, use 'listview_row_trip_summary_start' layout to
             *  display the Start-of-trip information.
             */
            case 0:
                ViewStart starter;
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.listview_row_trip_summary_start, parent, false);
                    starter = new ViewStart();
                    starter.tvStartA2 = (TextView)convertView.findViewById(R.id.tvStartA2);
                    starter.tvStartTime2 = (TextView)convertView.findViewById(R.id.tvStartTime2);
                    convertView.setTag(starter);
                } else {
                    starter = (ViewStart)convertView.getTag();
                }
                starter.tvStartA2.setText(sAddr);
                starter.tvStartTime2.setText(valueOf(sTime));
                return convertView;

            /*
             *  All infractions are displayed using the 'listview_row_trip_summary' layout, with
             *  each infraction in order corresponding with the map markers.
             */
            case 1:
                ViewHolder holder;
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.listview_row_trip_summary, null);
                    holder = new ViewHolder();
                    holder.tvNum = (TextView)convertView.findViewById(R.id.tvNum);
                    holder.tvLimit2 = (TextView)convertView.findViewById(R.id.tvLimit2);
                    holder.tvSpeed2 = (TextView)convertView.findViewById(R.id.tvSpeed2);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder)convertView.getTag();
                }
                holder.tvNum.setText(String.valueOf(position-1));
                holder.tvLimit2.setText(valueOf(infractions.get(position-1).get(2)));
                holder.tvSpeed2.setText(valueOf(infractions.get(position-1).get(3)));
                return convertView;

            /*
             *  For the last item in the list view, use 'listview_row_trip_summary_stop' layout
             *  to display the End-of-trip information.
             */
            case 2:
                ViewStop stopper;
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.listview_row_trip_summary_stop, parent, false);
                    stopper = new ViewStop();
                    stopper.tvStopA2 = (TextView)convertView.findViewById(R.id.tvStopA2);
                    stopper.tvStopTime2 = (TextView)convertView.findViewById(R.id.tvStopTime2);
                    convertView.setTag(stopper);
                } else {
                    stopper = (ViewStop)convertView.getTag();
                }
                stopper.tvStopA2.setText(eAddr);
                stopper.tvStopTime2.setText(valueOf(eTime));
                return convertView;

            default:
                // unknown data type
                throw new UnsupportedOperationException("Unknown data type");
            }
        }

    static class ViewHolder {
        TextView tvNum;
        TextView tvLimit2;
        TextView tvSpeed2;
    }

    static class ViewStart {
        TextView tvStartA2;
        TextView tvStartTime2;
    }

    static class ViewStop {
        TextView tvStopA2;
        TextView tvStopTime2;
    }
}

