package com.example.polygons;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Bartłomiej on 14.04.2017.
 */

public class MyLocation implements LocationListener{

    private Context context;
    private LocationManager locationManager;
    private double latitude, longitude, speed; // location parameters
    private double prevLatitude, prevLongitude;
    private int locationUpdateTime = 1000; //check location in every locationUpdateTime (ms)
    private Location LastKnownLocation , PrevLocation;

    public MyLocation(Context context){
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
    }

    public LatLng getLocation(){

        checkLocation();

        return new LatLng(latitude, longitude);

    }

    private void checkLocation(){
        boolean isGPSEnabled = false;
        boolean isNetworkEnabled = false;
        Location location;

        try {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            Log.v("isGPSEnabled", "=" + isGPSEnabled);

            // getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            Log.v("isNetworkEnabled", "=" + isNetworkEnabled);

            if (isGPSEnabled == false && isNetworkEnabled == false) {
                // no network provider is enabled
            } else {
                if (isNetworkEnabled) {
                    location=null;
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, locationUpdateTime, 1, this);
                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    location=null;
                    if (location == null) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, locationUpdateTime, 1, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double getSpeed(){
        return speed;
    }

    @Override
    public void onLocationChanged(Location location) {
        LastKnownLocation = location;

        if(PrevLocation != null){
            if(location.hasSpeed()){
                speed = location.getSpeed();
            }else {
                float[] distance = new float[5];
                Location.distanceBetween(LastKnownLocation.getLatitude(), LastKnownLocation.getLongitude(),
                        PrevLocation.getLatitude(),PrevLocation.getLongitude(), distance);

                double timeDiff = LastKnownLocation.getTime() - PrevLocation.getTime();
                speed = distance[0]/timeDiff;
                speed = speed * 3.6;
            }
        }

        PrevLocation = LastKnownLocation;
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
