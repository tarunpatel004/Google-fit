package com.fitnessapp;


import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.fitnessapp.util.GoogleApiHelper;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class Application extends android.app.Application {
    private static Application _instance;
    private static SharedPreferences _preferences;
    private GoogleApiHelper googleApiHelper;

    public static Application get() {
        return _instance;
    }

    private static SharedPreferences getSharedPreferences() {
        if (_preferences == null)
            _preferences = PreferenceManager.getDefaultSharedPreferences(_instance);
        return _preferences;
    }

    public static void clearSharedPreferences()

    {
        getSharedPreferences().edit().clear().apply();
    }

    /**
     * Sets shared preferences.
     */
    public static void setPreferences(String key, String value) {
        getSharedPreferences().edit().putString(key, value).apply();
    }

    public static void setPreferencesBoolean(String key, boolean value) {
        getSharedPreferences().edit().putBoolean(key, value).apply();
    }

    public static String getPrefaceData(String key) {
        return getSharedPreferences().getString(key, "");
    }


    public static boolean getPreferenceBoolean(String key) {
        return getSharedPreferences().getBoolean(key, false);
    }



    @Override
    public void onCreate() {
        super.onCreate();
        _instance = this;
        _preferences = PreferenceManager.getDefaultSharedPreferences(this);
        setUpFonts();

        googleApiHelper = new GoogleApiHelper(this);

    }

    private GoogleApiHelper getGoogleApiHelperInstance() {
        return this.googleApiHelper;
    }

    public static GoogleApiHelper getGoogleApiHelper() {
        return get().getGoogleApiHelperInstance();
    }

    private void setUpFonts() {
        try {
            CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                    .setDefaultFontPath("Raleway-Regular.ttf")
                    .setFontAttrId(R.attr.fontPath)
                    .build());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}