package com.helix.helixgps.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.provider.ProviderProperties;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.helix.helixgps.MapsActivity;
import com.helix.helixgps.helper.SessionManager;

import java.io.File;
import java.security.Provider;
import java.util.Random;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class Ngentot extends Service {
    private FusedLocationProviderClient fusedLocationProviderClient;
    SessionManager sesi;
    private LocationManager locationManager;
    private Handler handler;
    private boolean startMock;
    private PendingIntent mPendingIntent;
    private String lat, lng;
    private Random random;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("WrongConstant")
    @RequiresApi(api = Build.VERSION_CODES.S)
    public void onCreate() {
        super.onCreate();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        sesi = new SessionManager(this);
        random = new Random();
        locationManager = ((LocationManager) getSystemService(LOCATION_SERVICE));
        locationManager.addTestProvider("gps", true, false, false, false, false, true, true, ProviderProperties.POWER_USAGE_LOW, ProviderProperties.ACCURACY_FINE);

        locationManager.setTestProviderEnabled("gps", true);
        locationManager.setTestProviderStatus("gps", 2, null, System.currentTimeMillis());

        try {
             if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                try {
                    // @throws IllegalArgumentException if a provider with the given name already exists
                    locationManager.addTestProvider("network", true, false, false, false, true, true, true, 1, 1);
                } catch (IllegalArgumentException e) {
                }
                try {
                    // @throws IllegalArgumentException if no provider with the given name exists
                    locationManager.setTestProviderEnabled("network", true);
                    locationManager.setTestProviderStatus("network", 2, null, System.currentTimeMillis());
                } catch (IllegalArgumentException e) {
                    locationManager.addTestProvider("network", true, false, false, false, true, true, true, 1, 1);
                }
                try {
                    // @throws IllegalArgumentException if a provider with the given name already exists
                    locationManager.addTestProvider(LocationManager.GPS_PROVIDER, true, false, false, false, true, true, true, 1, 1);
                } catch (IllegalArgumentException e) {
                }
                try {
                    // @throws IllegalArgumentException if no provider with the given name exists
                    locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
                    locationManager.setTestProviderStatus("gps", 2, null, System.currentTimeMillis());
                } catch (IllegalArgumentException e) {
                    locationManager.addTestProvider(LocationManager.GPS_PROVIDER, true, false, false, false, true, true, true, 1, 1);
                }
//android dibawah 1000000
            } else {
                try {
                    // @throws IllegalArgumentException if a provider with the given name already exists
                    locationManager.addTestProvider("network", true, false, false, false, true, true, true, 2,1);
                } catch (IllegalArgumentException e) {
                }
                try {
                    // @throws IllegalArgumentException if no provider with the given name exists
                    locationManager.setTestProviderEnabled("network", true);
                    locationManager.setTestProviderStatus("network", 2, null, System.currentTimeMillis());
                } catch (IllegalArgumentException e) {
                    locationManager.addTestProvider("network", true, false, false, false, true, true, true, 2,1);
                }
                try {
                    // @throws IllegalArgumentException if a provider with the given name already exists
                    locationManager.addTestProvider(LocationManager.GPS_PROVIDER, true, false, false, false, true, true, true, 2,1);
                } catch (IllegalArgumentException e) {
                }
                try {
                    // @throws IllegalArgumentException if no provider with the given name exists
                    locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
                    locationManager.setTestProviderStatus("gps", 2, null, System.currentTimeMillis());
                } catch (IllegalArgumentException e) {
                    locationManager.addTestProvider(LocationManager.GPS_PROVIDER, true, false, false, false, true, true, true, 2,1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
         handler = new Handler(Looper.getMainLooper());
    }

    public int onStartCommand(Intent paramIntent, int paramInt1, int paramInt2) {


        startMock = true;
        new LocationUpdateTask().start();

        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MapsActivity.class), FLAG_UPDATE_CURRENT);

        Notification.Builder notificationBuilder = new Notification.Builder(this);
        notificationBuilder.setContentIntent(mPendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            mPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(this, MapsActivity.class), FLAG_UPDATE_CURRENT);
            NotificationChannel notificationChannel =
                    new NotificationChannel(
                            "1337",
                            "BSH-VIP",
                            NotificationManager.IMPORTANCE_HIGH);
            notificationBuilder.setContentIntent(mPendingIntent);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
            notificationBuilder.setChannelId("1337");
        }

        mPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(this, MapsActivity.class), FLAG_UPDATE_CURRENT);
        Double lat = Double.parseDouble(sesi.getLat());
        Double lng = Double.parseDouble(sesi.getLong());
        this.lat = String.valueOf(lat);
        this.lng = String.valueOf(lng);
        notificationBuilder.setContentTitle("BSH VIP Fake");
        notificationBuilder.setContentIntent(mPendingIntent);
        notificationBuilder.setContentText(lat + "," + lng);
        notificationBuilder.setSmallIcon(android.R.drawable.star_on);
        Notification notification = notificationBuilder.build();
        startForeground(2, notification);
        return Service.START_STICKY;
    }


    @SuppressLint("MissingPermission")
    private void updateMockLocation() {
        Location localLocation1 = new Location("gps");
        Double lat = Double.parseDouble(sesi.getLat());
        Double lng = Double.parseDouble(sesi.getLong());
        this.lat = String.valueOf(lat);
        this.lng = String.valueOf(lng);

        localLocation1.setLatitude(lat);
        localLocation1.setLongitude(lng);

        localLocation1.setAccuracy(randomAcc());
        localLocation1.setSpeed(randomSpeed());

        localLocation1.setBearing(1);
        localLocation1.setAltitude(1);
        localLocation1.setBearing(randomAcc());
        localLocation1.setBearingAccuracyDegrees(Float.parseFloat(String.valueOf(randomAcc())));

        localLocation1.setTime(System.currentTimeMillis());

        localLocation1.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        this.locationManager = ((LocationManager) getSystemService(LOCATION_SERVICE));
        if (this.locationManager != null) {
            this.locationManager.setTestProviderLocation("gps", localLocation1);
        }
        fusedLocationProviderClient.setMockLocation(localLocation1);
        fusedLocationProviderClient.setMockMode(true);

    }

    @SuppressLint("MissingPermission")
    private void updateMockLocation2() {
        Location localLocation1 = new Location("network");
        Double lat = Double.parseDouble(sesi.getLat());
        Double lng = Double.parseDouble(sesi.getLong());
        this.lat = String.valueOf(lat);
        this.lng = String.valueOf(lng);

        localLocation1.setLatitude(lat);
        localLocation1.setLongitude(lng);

        localLocation1.setAccuracy(randomAcc());
        localLocation1.setSpeed(randomSpeed());

        localLocation1.setBearing(1);
        localLocation1.setAltitude(1);
        localLocation1.setBearing(randomAcc());
        localLocation1.setBearingAccuracyDegrees(Float.parseFloat(String.valueOf(randomAcc())));

        localLocation1.setTime(System.currentTimeMillis());

        localLocation1.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        this.locationManager = ((LocationManager) getSystemService(LOCATION_SERVICE));
        if (this.locationManager != null) {
            this.locationManager.setTestProviderLocation("network", localLocation1);
        }
        fusedLocationProviderClient.setMockLocation(localLocation1);
        fusedLocationProviderClient.setMockMode(true);

    }
    @SuppressLint({"MissingPermission", "WrongConstant"})
    public void onDestroy() {
        super.onDestroy();
        this.startMock = false;

        this.fusedLocationProviderClient.setMockMode(false);
        this.locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        try {
             locationManager.setTestProviderEnabled("gps", false);
            if (locationManager.getProvider("gps") != null) {
                locationManager.removeTestProvider("gps");
                 locationManager.setTestProviderEnabled("network", false);
                if (locationManager.getProvider("network") != null) {
                    locationManager.removeTestProvider("network");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
         LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(105);
        locationRequest.setInterval(4000L);
        locationRequest.setFastestInterval(300L);
        locationRequest.setSmallestDisplacement(0.0F);
        Looper looper = Looper.myLooper();
        this.fusedLocationProviderClient.requestLocationUpdates(locationRequest, this.mLocationCallback, looper);
        removeNotification();
        startService(new Intent(this, Service.class));
    }

    MockLocationListener listener;


    public abstract interface MockLocationListener
    {
        public abstract void onMockLocationChanged(Location paramLocation);
    }

    private LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Location lastLocation = locationResult.getLastLocation();
            if (listener != null) {
                listener.onMockLocationChanged(lastLocation);
            }
        }
    };
    private void removeNotification()
    {
        stopForeground(true);
        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancel(99999);
    }
    private float randomAcc() {
        return random.nextFloat() * (25.2f - 3.0f) + 3.0f;

    }

    private float randomSpeed() {
        return random.nextFloat() * (1f - 0.1f) + 0.1f;

    }

    private class LocationUpdateTask extends Thread {
        @SuppressLint("NewApi")
        @Override
        public void run() {
            super.run();
            try {
                while (startMock) {

                    updateMockLocation();
                    updateMockLocation2();

                    Thread.sleep(500L);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}

