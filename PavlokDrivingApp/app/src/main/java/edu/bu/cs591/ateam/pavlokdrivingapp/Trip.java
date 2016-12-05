package edu.bu.cs591.ateam.pavlokdrivingapp;

/**
 * Created by sesha on 12/5/2016.
 */

public class Trip {

    private String source;
    private String destination;
    private int tripId;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }
}
