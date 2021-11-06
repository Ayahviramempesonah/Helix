package com.helix.helixgps.helper;

 import android.annotation.SuppressLint;
 import android.content.ContentResolver;
 import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
 import android.provider.Settings;
 import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

 import static android.provider.Settings.Secure.ANDROID_ID;

@SuppressLint("SimpleDateFormat")
 public class HelixHelper {
    private static final int DEBUG = 1;
    public static final String APP = "HelixGPS-1337";
    public static final String BASE_URL = "http://bsh-team.net/api/";
    public static final String BASE_URL_IMAGE = "http://bsh-team.net/img/";
    private static ContentResolver contentResolver;

    public static String getDateTimeToday() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }


    public static boolean isOnline(Context c) {
        ConnectivityManager cm = (ConnectivityManager) c
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }


    public static void toast(Context c, String msg) {
        Toast.makeText(c, msg, Toast.LENGTH_SHORT).show();
    }

    public static String tglSekarang() {
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static Date strTodate(String data) {
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        Date startDate = null;
        String newDateString = "";
        try {
            startDate = df.parse(data);
            // newDateString = df.format(startDate);
            System.out.println(newDateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return startDate;
    }





    public static void log(String pesan) {
        if (DEBUG == 1) {
            Log.d(APP, pesan);
        }
    }


}
