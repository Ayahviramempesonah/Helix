package com.helix.helixgps.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import io.michaelrocks.paranoid.Obfuscate;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;
import com.helix.helixgps.R;
import com.helix.helixgps.activity.SettingActivity;
import com.helix.helixgps.helper.App;
import com.helix.helixgps.helper.HelixHelper;
import com.helix.helixgps.helper.SessionManager;
import com.helix.helixgps.services.Ngentot;
import com.helix.helixgps.services.Services;

import java.util.Objects;

import static android.location.LocationManager.*;

@Obfuscate

public class MapsActivity extends FragmentActivity implements LocationListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMapClickListener {

    private GoogleMap mMap;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private HelixHelper helix;
    private Marker mCurrLocationMarker;

    String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION};
    private TextView tv_latitude, tv_longitude, tv_accuracy, tv_provider;
    private LinearLayout ln1;
    private LocationManager locationManager;
    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS status
    boolean canGetLocation = false;

    double latitude; // latitude
    double longitude; // longitude

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10000; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
    private SessionManager sesi;
    View mapView;
    private ImageView play, pause, ic_goto, ic_config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);
        if (!hasPermissions(this, permissions)) {
            ActivityCompat.requestPermissions(this, permissions, 1);
        }

        sesi = new SessionManager(this);

        initHelix();
        buttonUpd();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapView = mapFragment.getView();
        mapFragment.getMapAsync(this);

        locationHandler();

    }


    void locationHandler() {
        final Handler h = new Handler();
        final int delay = 1000;
        getLocation();
        h.postDelayed(new Runnable() {
            public void run() {

                getmLastLocation();
                Log.d("MapsActivity GoogleApi", String.valueOf(mGoogleApiClient.isConnected()));

                h.postDelayed(this, delay);
            }
        }, delay);
    }

    void buttonUpd() {
        boolean mock = sesi.getMock();
        runOnUiThread(() -> {
            if (mock) {
                startService(new Intent(getApplicationContext(), Ngentot.class));
                play.setVisibility(View.GONE);
                pause.setVisibility(View.VISIBLE);
            } else {
                getLocation();
                play.setVisibility(View.VISIBLE);
                pause.setVisibility(View.GONE);
            }
        });

    }

    void playMock() {

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        buttonUpd();
        sesi.setMock(true);
        startService(new Intent(getApplicationContext(), Ngentot.class));


    }

    void stopMock() {
        if (!mGoogleApiClient.isConnected()) {
            buildGoogleApiClient();
            mGoogleApiClient.connect();
        }
        buttonUpd();
        sesi.setMock(false);
        stopService(new Intent(getApplicationContext(), Ngentot.class));
        Services s = new Services(getApplicationContext());
        s.getLocation();
    }

    @SuppressLint("LongLogTag")
    void initHelix() {

        tv_latitude = findViewById(R.id.tv_latitude);
        tv_longitude = findViewById(R.id.tv_longitude);
        tv_accuracy = findViewById(R.id.tv_accuracy);
        tv_provider = findViewById(R.id.tv_provider);

        ic_goto = findViewById(R.id.ic_goto_location);
        ic_config = findViewById(R.id.ic_config);
        play = findViewById(R.id.play);
        pause = findViewById(R.id.pause);
        play.setOnClickListener(view -> {
            playMock();
            runOnUiThread(() -> {

                play.setVisibility(View.GONE);
                pause.setVisibility(View.VISIBLE);

            });
        });
        pause.setOnClickListener(view -> {
            stopMock();
            runOnUiThread(() -> {

                play.setVisibility(View.VISIBLE);
                pause.setVisibility(View.GONE);

            });
        });

        ic_goto.setOnClickListener(view -> {
            gotoLocation();
        });
        ic_config.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), SettingActivity.class));
        });


        ln1 = findViewById(R.id.stats);

        String now = HelixHelper.getDateTimeToday();
        String imei = getImei();

        Log.d("MapsActivity NOW", now);
        Log.d("MapsActivity IMEI", imei);


    }

    @SuppressLint({"ObsoleteSdkInt", "HardwareIds"})
    String getImei() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String imei = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            imei = telephonyManager.getImei();
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            imei = telephonyManager.getDeviceId();
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            imei = Settings.Secure.getString(
                    getApplicationContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID);
        }
        return imei;
    }


    public void gotoLocation() {

        final View v = getLayoutInflater().inflate(R.layout.goto_view, null, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(v);
        builder.setTitle("Masukan Koordinat Lokasi");
        builder.setMessage("");
        builder.setPositiveButton("Start Mock", (dialogInterface, i) -> {
            TextInputEditText tv_lat = v.findViewById(R.id.et_lat);
            TextInputEditText tv_lng = v.findViewById(R.id.et_lang);
            String lat = Objects.requireNonNull(tv_lat.getText()).toString();
            String longitude = Objects.requireNonNull(tv_lng.getText()).toString();

            if (!lat.isEmpty() && !longitude.isEmpty()) {

                sesi.setLat(lat);
                sesi.setLong(longitude);
                HelixHelper.toast(getApplicationContext(), "Start Mock Location\n "+lat+","+longitude);
                playMock();

            }else {
                HelixHelper.toast(getApplicationContext(), "Kolom Koordinate Kosong");
            }
            dialogInterface.dismiss();

        });
        builder.setNegativeButton("Batal", (dialogInterface, i) -> dialogInterface.dismiss());
        builder.create().show();
    }


    public void setIc_config() {

        final View v = getLayoutInflater().inflate(R.layout.config_view, null, false);
        Switch network_mode = v.findViewById(R.id.network_mode);
        Switch sw_coor = v.findViewById(R.id.sw_coor);
        Switch sw_muter = v.findViewById(R.id.sw_muter);
        Switch sw_acc = v.findViewById(R.id.sw_acc);
        SharedPreferences prefs = getSharedPreferences("HelixGPS" ,0);
        boolean acc = prefs.getBoolean("is_acc", false);
        Boolean muter = prefs.getBoolean("is_muter", false);
        Boolean coor =  prefs.getBoolean("is_coor", false);
        Boolean net = prefs.getBoolean("net", false);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(v);
        builder.setTitle("BSHelix Config");
        builder.setMessage("");
        builder.setPositiveButton("Save", (dialogInterface, i) -> {
            sw_acc.setChecked(acc);
            sw_acc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        prefs.edit().putBoolean("is_acc", true).apply();
                        return;
                    }
                    prefs.edit().putBoolean("is_acc", false).apply();
                }
            });
            if (sw_acc.isChecked()) {
                prefs.edit().putBoolean("is_acc", true).apply();
            }

            network_mode.setChecked(net);
            network_mode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        prefs.edit().putBoolean("net", true).apply();
                        return;
                    }
                    prefs.edit().putBoolean("net", false).apply();
                }
            });
            if (network_mode.isChecked()) {
                prefs.edit().putBoolean("net", true).apply();
            }
            dialogInterface.dismiss();

         });
        builder.setNegativeButton("Batal", (dialogInterface, i) -> dialogInterface.dismiss());
        builder.create().show();
    }
    protected synchronized void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") == 0) {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkLocationPermission();
            return;
        }
        if (mapView != null &&
                mapView.findViewById(Integer.parseInt("1")) != null) {
            // Get the button view
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();
            // position on right bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 30, 30);
        }
        mMap.setMyLocationEnabled(true);
        mMap.setOnMapClickListener(this);


    }

    @SuppressLint("MissingPermission")
    public Location getLocation() {
        try {
            locationManager = (LocationManager) getApplicationContext()
                    .getSystemService(LOCATION_SERVICE);
            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        checkLocationPermission();
                    }
                    locationManager.requestLocationUpdates(
                            NETWORK_PROVIDER,
                            1000,
                            0, this);
                    Log.d("MapsActivity", "Network");
                    if (locationManager != null) {
                        mLastLocation = locationManager
                                .getLastKnownLocation(NETWORK_PROVIDER);
                        if (mLastLocation != null) {
                            latitude = mLastLocation.getLatitude();
                            longitude = mLastLocation.getLongitude();

                            onLocationChanged(mLastLocation);

                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (mLastLocation == null) {
                        assert locationManager != null;
                        locationManager.requestLocationUpdates(
                                GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("MapsActivity", "GPS Enabled");
                        if (locationManager != null) {
                            mLastLocation = locationManager
                                    .getLastKnownLocation(GPS_PROVIDER);
                            if (mLastLocation != null) {
                                latitude = mLastLocation.getLatitude();
                                longitude = mLastLocation.getLongitude();
                                onLocationChanged(mLastLocation);

                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return mLastLocation;
    }

    @SuppressLint("MissingPermission")
    public Location getmLastLocation() {
        try {
            locationManager = (LocationManager) getApplicationContext()
                    .getSystemService(LOCATION_SERVICE);
            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        checkLocationPermission();
                    }
                    locationManager.requestLocationUpdates(
                            NETWORK_PROVIDER,
                            1000,
                            0, this);
                    Log.d("MapsActivity", "Network");
                    if (locationManager != null) {
                        mLastLocation = locationManager
                                .getLastKnownLocation(NETWORK_PROVIDER);
                        if (mLastLocation != null) {
                            latitude = mLastLocation.getLatitude();
                            longitude = mLastLocation.getLongitude();
                            runOnUiThread(() -> updateStats(mLastLocation));

                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (mLastLocation == null) {
                        assert locationManager != null;
                        locationManager.requestLocationUpdates(
                                GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("MapsActivity", "GPS Enabled");
                        if (locationManager != null) {
                            mLastLocation = locationManager
                                    .getLastKnownLocation(GPS_PROVIDER);
                            if (mLastLocation != null) {
                                latitude = mLastLocation.getLatitude();
                                longitude = mLastLocation.getLongitude();
                                runOnUiThread(() -> updateStats(mLastLocation));
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return mLastLocation;
    }

    void updateStats(Location mLastLocation) {
        tv_latitude.setText(String.valueOf(mLastLocation.getLatitude()));
        tv_longitude.setText(String.valueOf(mLastLocation.getLongitude()));
        tv_accuracy.setText(String.valueOf(mLastLocation.getAccuracy()));
        tv_provider.setText(String.valueOf(mLastLocation.getProvider()));

    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") != 0) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, "android.permission.ACCESS_FINE_LOCATION")) {
                ActivityCompat.requestPermissions(this, new String[]{"android.permission.ACCESS_FINE_LOCATION"}, 99);
                return false;
            }
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.ACCESS_FINE_LOCATION"}, 99);
            return false;
        }
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("MapsActivity", "Google Connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("MapsActivity", "Google Suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("MapsActivity", "Google Failed To Connect");

    }

    @Override
    public void onMapClick(LatLng latLng) {

        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        sesi.setLat(String.valueOf(latLng.latitude));
        sesi.setLong(String.valueOf(latLng.longitude));

        MarkerOptions mark = new MarkerOptions();
        mark.position(latLng);
        Log.d("MapsOnClick", latLng.toString());
        mark.title(latLng.latitude + "," + latLng.longitude);

        mark.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        mCurrLocationMarker = mMap.addMarker(mark);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.latitude, latLng.longitude), 16f));


    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        // mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 16f));
        mLastLocation = location;
        runOnUiThread(() -> updateStats(mLastLocation));


    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Log.d("onStatusChanged", s);

    }


    @Override
    public void onProviderEnabled(@NonNull String provider) {
        Log.d("onProviderEnabled", provider);

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Log.d("onProviderDisabled", provider);

    }

    @Override
    public void onResume() {
        super.onResume();
        buttonUpd();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        buttonUpd();
    }

    @Override
    public void onPause() {
        super.onPause();
        buttonUpd();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        buttonUpd();
    }
}