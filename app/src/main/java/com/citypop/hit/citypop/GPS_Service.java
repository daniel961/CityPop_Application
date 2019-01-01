package com.citypop.hit.citypop;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.widget.Toast;

public class GPS_Service extends Service{

    private LocationManager locationManager;
    private LocationListener locationListener;



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {//when service starts its start from onCreate methode

       locationListener = new LocationListener() {
           @Override
           public void onLocationChanged(Location location) { //when there is location update this methode will called
                //we need to transfer the data from this method to our Activity
                // and we do it using the Brodcast Reciver Class
               Intent i = new Intent("location_update");
               i.putExtra("longitude",location.getLongitude());
               i.putExtra("latitude",location.getLatitude());
               //and to brodcast the intent we call the send brodcast method
               sendBroadcast(i);



           }

           @Override
           public void onStatusChanged(String provider, int status, Bundle extras) {

           }

           @Override
           public void onProviderEnabled(String provider) {

           }

           @Override
           public void onProviderDisabled(String provider) { //if the GPS is off this method called
               //here we call the GPS settings to the user
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);

           }
       };

       locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

       locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,8000,0,locationListener);



    }

    @Override
    public void onDestroy() {
        //when the service is dead we make sure that the locations calls will dead too
        //to avoid memory leaks
        super.onDestroy();
        if(locationManager != null){
            locationManager.removeUpdates(locationListener);
        }
    }
}
