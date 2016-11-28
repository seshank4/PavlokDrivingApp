package edu.bu.cs591.ateam.pavlokdrivingapp;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Ganesh on 10/25/2016.
 */
public class PavlokConnection extends AsyncTask {

    @Override
    protected Object doInBackground(Object[] params) {

        String action = (String) params[0];
        int intensity = (int) params[1];
        String code = (String) params[2];
        String token = authorizeAndGetToken(code);
        try {
            doAction(token, action, intensity);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String authorizeAndGetToken(String code) {
        URL url = null;
        try {
            url = new URL("http://pavlok-mvp.herokuapp.com/oauth/token");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        HttpURLConnection connection = null;

        connection = getHttpURLConnection(url, connection);

        JSONObject obj = new JSONObject();
        try {
            obj.put("client_id", "8882d3c9f67eff55ff7b0c535d2a6ccd189d47cd7a7b42c531ad25d413baadd4");
            obj.put("client_secret", "07eaa8a1d37b2cfb029d910d467af98ee1d90daf685a477cadf2069ec00add4f");
            obj.put("code", code);
            obj.put("grant_type", "authorization_code");
            obj.put("redirect_uri","http://pavlok-bu-cs591/auth/pavlok/result");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        DataOutputStream printout = null;
        DataInputStream input;

        try {
            printout = new DataOutputStream(connection.getOutputStream());
            printout.write(obj.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }


        ObjectMapper mapper = new ObjectMapper();
        Authorized responseObj = null;
        try {
            System.out.println(connection.getResponseCode());
            responseObj = mapper.readValue(connection.getInputStream(), Authorized.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(responseObj !=null){
            return responseObj.getAccess_token();
        }
        return "";
    }

    @NonNull
    private HttpURLConnection getHttpURLConnection(URL url, HttpURLConnection connection) {
        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        connection.setRequestProperty("Content-Type", "application/json");

        connection.setDoInput(true);
        connection.setDoOutput(true);
        try {
            connection.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public void doAction(String access_token, String action, int intensity) throws IOException {

        URL url = new URL("http://pavlok-mvp.herokuapp.com/api/v1/stimuli/"+action+"/"+intensity);

        HttpURLConnection connection = null;

        connection = getHttpURLConnection(url, connection);

        JSONObject obj = new JSONObject();
        try {
            obj.put("access_token", access_token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        DataOutputStream printout = null;
        DataInputStream input;

        try {
            printout = new DataOutputStream(connection.getOutputStream());
            printout.write(obj.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(connection.getResponseCode());

    }
}
