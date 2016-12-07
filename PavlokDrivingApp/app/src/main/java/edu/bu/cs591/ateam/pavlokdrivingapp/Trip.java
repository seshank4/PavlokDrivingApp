package edu.bu.cs591.ateam.pavlokdrivingapp;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Model class to store the trip information. This implements parcelable so as to enable us to send the
 * ArrayList of Trip class though an intent
 * Created by sesha on 12/5/2016.
 */

public class Trip implements Parcelable {

    private String source;
    private String destination;
    private int tripId;
    private String tripStartDate;

    public Trip() {

    }

    private Trip(Parcel in) {
        source = in.readString();
        destination = in.readString();
        tripId = in.readInt();
        tripStartDate = in.readString();
    }

    public String getTripStartDate() {
        return tripStartDate;
    }

    public void setTripStartDate(String tripStartDate) {
        this.tripStartDate = tripStartDate;
    }

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

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * serialize
     * @param dest
     * @param flags
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(source);
        dest.writeString(destination);
        dest.writeInt(tripId);
        dest.writeString(String.valueOf(tripStartDate));
    }

    public static final Parcelable.Creator<Trip> CREATOR = new Parcelable.Creator<Trip>() {
        public Trip createFromParcel(Parcel in) {
            return new Trip(in);
        }
        public Trip[] newArray(int size) {
            return new Trip[size];
        }
    };
}
