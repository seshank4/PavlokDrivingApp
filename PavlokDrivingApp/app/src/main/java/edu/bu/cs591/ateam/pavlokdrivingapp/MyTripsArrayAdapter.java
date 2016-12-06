package edu.bu.cs591.ateam.pavlokdrivingapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sesha on 12/5/2016.
 */

public class MyTripsArrayAdapter extends ArrayAdapter {

    ArrayList<Trip> trips;
    private LayoutInflater mInflater;
    public MyTripsArrayAdapter(Context context, List<Trip> trips)
    {
        super(context,0,trips);
        this.trips = (ArrayList<Trip>) trips;
        mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.listview_row_main_activity,parent,false);
            // inflate custom layout called row
            holder = new ViewHolder();
            holder.tvSource = (TextView)convertView.findViewById(R.id.tvSource);
            holder.tvDestination = (TextView)convertView.findViewById(R.id.tvDestination);
            holder.tvTripTime = (TextView)convertView.findViewById(R.id.tvTripTime);
            holder.tvToText= (TextView)convertView.findViewById(R.id.tvToText);
            convertView.setTag(holder);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)convertView.getTag();
        }
        Trip trip = (Trip)trips.get(position);
        holder.tvSource.setText(trip.getSource().substring(0,trip.getSource().indexOf(",")));
        holder.tvDestination.setText(trip.getDestination().substring(0,trip.getDestination().indexOf(",")));
        holder.tvTripTime.setText(trip.getTripStartDate());
        holder.tvToText.setText(" To ");
        return convertView;
    }

    private static class ViewHolder{

        private TextView tvSource;
        private TextView tvDestination;
        private TextView tvTripTime;
        private TextView tvToText;

    }
}
