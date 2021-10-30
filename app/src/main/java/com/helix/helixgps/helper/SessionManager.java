package com.helix.helixgps.helper;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * AldytOI wAS Here
 */
public class SessionManager {
    private static final String KEY_TOKEN = "toket";
    private static final String KEY_LOGIN = "isLogin";
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    int PRIVATE_MODE = 0;
    Context c;

     String PREF_NAME = "HelixGPS";

    //constructor
    public SessionManager(Context c) {
        this.c = c;
        pref = c.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    //add session login dengan PREFS
    public void createLogin(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.putBoolean(KEY_LOGIN, true);
        editor.apply();
        //commit digunakan untuk menyimpan perubahan
    }

    //clear semua data prefs
    public void logout() {
        pref.edit().clear().apply();
    }

    //getter key token
    public String getToken() {
        return pref.getString(KEY_TOKEN, "");
    }

    //getter login
    public boolean isLogin() {
        return pref.getBoolean(KEY_LOGIN, false);
    }

   

    public void setId(String id) {
        editor.putString("id", id);
        editor.apply();
    }

    public String getId() {
        return pref.getString("id", "");
    }

    public void setName(String name) {
        editor.putString("name", name);
        editor.apply();
    }

    public String getName() {
        return pref.getString("name", "");
    }

    public void setDevice(String device) {
        editor.putString("device", device);
        editor.apply();
    }

    public String getDevice() {
        return pref.getString("device", "");
    }

    
    
    public void setData(String data) {
        editor.putString("data", data);
        editor.apply();
    }

    public String getData() {
        return pref.getString("data", "");
    }

    
    
    public void setHp(String nohp) {
        editor.putString("nohp", nohp);
        editor.apply();
    }

    public String getHp() {
        return pref.getString("hp", "");
    }

    public void setExpdate(String exp) {
        editor.putString("expdate", exp);
        editor.apply();
    }

    public String getExpdate() {
        return pref.getString("expdate", "");
    }

    public void setIsExp(String isExp) {
        editor.putString("isExp", isExp);
        editor.apply();
    }

    public String getIsExp() {
        return pref.getString("isExp", "");
    }

    public void setEmail(String email) {
        editor.putString("email", email);
        editor.apply();
    }

    public String getEmail() {
        return pref.getString("email", "");
    }

    public void setID(String id) {
        editor.putString("id", id);
        editor.apply();
    }

    public String getID() {
        return pref.getString("id", "");
    }


    public void setLat(String lat) {
        editor.putString("lalat", lat);
        editor.apply();
    }

    public String getLat() {
        return pref.getString("lalat", "");
    }


    public void setLong(String lot) {
        editor.putString("lolong", lot);
        editor.apply();
    }

    public String getLong() {
        return pref.getString("lolong", "");
    }










}
