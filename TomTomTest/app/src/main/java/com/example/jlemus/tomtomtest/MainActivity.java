package com.example.jlemus.tomtomtest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class MainActivity extends AppCompatActivity {

    //final TextView mTextView; = (TextView) findViewById(R.id.text);
    private TextView mTextView;
    private String TAG = "tag_tom";

    //tomtom documentation for api:
    // http://developer.tomtom.com/products/onlinenavigation/onlinesearch/documentation/Reverse_Geocoding/ReverseGeocode

    //volley documentation:
    //https://developer.android.com/training/volley/simple.html

    //tomtom api variables setup here
    private String BASE_URL =  "api.tomtom.com/";
    private int VERSION_NUMBER = 2;
    private String POSITION = "42.35268089999999,-71.12817139999999"; // This is specified as a comma separated string composed by lat., lon.
    private String EXT = "json/"; // the extension of the response. (json, jsonp, js, or xml)
    private String API_KEY = "h8fxx4ptxbtb4y7xv5r9x7ga";
    private Boolean RETURN_SPEED_LIMIT = true;


    // example string request

    //GET https://<baseURL>/search/<versionNumber>/reverseGeocode/<position>.<ext>?key=<apiKey>
    // [&spatialKeys=<spatialKeys>][&returnSpeedLimit=<returnSpeedLimit>][&heading=<heading>]
    // [&radius=<radius>][&streetNumber=<streetNumber>][&returnRoadUse=<returnRoadUse>]
    // [&roadUse=<roadUse>]


    String url = "https://" + BASE_URL + "search/" + VERSION_NUMBER + "/reverseGeocode/" +
            POSITION + "." + EXT + "?key=" + API_KEY + "&returnSpeedLimit=true";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //mTextView = (TextView) findViewById(R.id)
        mTextView = (TextView) findViewById(R.id.text2);
        RequestQueue queue = Volley.newRequestQueue(this);
        //String url = "https://api.github.com/gists";
        Log.e(TAG, url);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        mTextView.setText("Response is: "+ response.substring(0,500));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mTextView.setText("That didn't work!");
            }
        });
// Add the request to the RequestQueue.
        queue.add(stringRequest);


    }
}

