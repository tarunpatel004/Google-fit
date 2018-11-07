package com.fitness;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.view.WindowManager;

import com.fitness.util.GoogleApiHelper;

import java.lang.reflect.Field;
import java.net.CookieManager;
import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class Application extends android.app.Application {
    private static final String SET_COOKIE_KEY = "Set-Cookie";
    private static final String COOKIE_KEY = "Cookie";
    private static final String SESSION_COOKIE = "sessionid";
    private static Application _instance;
    private static SharedPreferences _preferences;
    private static Typeface fontawesome;
    //DatabaseHelper Object
    // GSOn
    private CookieManager cookieManager;
    private GoogleApiHelper googleApiHelper;

    public static Application get() {
        return _instance;
    }

    public static SharedPreferences getSharedPreferences() {
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
     *
     * @return the shared preferences
     */
    public static void setPreferences(String key, String value) {
        getSharedPreferences().edit().putString(key, value).apply();
    }

    /**
     * Sets shared preferences.
     *
     * @return the shared preferences
     */
    public static void setPreferencesInt(String key, int value) {
        getSharedPreferences().edit().putInt(key, value).apply();
    }

    public static void setPreferencesBoolean(String key, boolean value) {
        getSharedPreferences().edit().putBoolean(key, value).apply();
    }

    public static String getPrefranceData(String key) {
        return getSharedPreferences().getString(key, "");
    }

    public static int getPrefranceDataInt(String key) {
        return Integer.parseInt(getSharedPreferences().getString(key, "0"));
    }

    public static int getPreferanceInt(String key) {
        return getSharedPreferences().getInt(key, 0);
    }

    public static boolean getPrefranceBoolean(String key) {
        return getSharedPreferences().getBoolean(key, false);
    }


    /*
     * Set default font famiily for the application
     * */
    public static void setDefaultFont(Context context,
                                      String staticTypefaceFieldName, String fontAssetName) {
        final Typeface regular = Typeface.createFromAsset(context.getAssets(),
                fontAssetName);
        replaceFont(staticTypefaceFieldName, regular);
    }

    protected static void replaceFont(String staticTypefaceFieldName,
                                      final Typeface newTypeface) {
        try {
            final Field staticField = Typeface.class
                    .getDeclaredField(staticTypefaceFieldName);
            staticField.setAccessible(true);
            staticField.set(null, newTypeface);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void showAlertDialog(Context context) {
        /** define onClickListener for dialog */
        DialogInterface.OnClickListener listener
                = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // do some stuff eg: context.onCreate(super)
            }
        };

        /** create builder for dialog */
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setCancelable(false)
                .setMessage("Message...")
                .setTitle("Title")
                .setPositiveButton("OK", listener);
        /** create dialog & set builder on it */
        Dialog dialog = builder.create();
        /** this required special permission but u can use aplication context */
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        /** show dialog */
        dialog.show();
    }


    public static Typeface getAulyars() {
        return Typeface.createFromAsset(get().getAssets(), "Aulyars_Regular.otf");
    }

    public static Typeface getFontAuwsom() {
        return Typeface.createFromAsset(get().getAssets(), "image/FontAwesome.otf");
    }

    public static Typeface getFontawesome() {
        if (fontawesome != null)
            return fontawesome;
        else
            return fontawesome = Typeface.createFromAsset(get().getAssets(), "fontawesome.ttf");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        _instance = this;
        _preferences = PreferenceManager.getDefaultSharedPreferences(this);
        setUpFonts();

        googleApiHelper = new GoogleApiHelper(this);

    }

    public GoogleApiHelper getGoogleApiHelperInstance() {
        return this.googleApiHelper;
    }

    public static GoogleApiHelper getGoogleApiHelper() {
        return get().getGoogleApiHelperInstance();
    }

    /**
     * Checks the response headers for session cookie and saves it
     * if it finds it.
     *
     * @param headers Response Headers.
     */
    public final void checkSessionCookie(Map<String, String> headers) {
        if (headers.containsKey(SET_COOKIE_KEY)
                && headers.get(SET_COOKIE_KEY).startsWith(SESSION_COOKIE)) {
            String cookie = headers.get(SET_COOKIE_KEY);
            if (cookie.length() > 0) {
                String[] splitCookie = cookie.split(";");
                String[] splitSessionId = splitCookie[0].split("=");
                cookie = splitSessionId[1];
                setPreferences(SESSION_COOKIE, cookie);
            }
        }
    }

    /**
     * Adds session cookie to headers if exists.
     *
     * @param headers
     */
    public final void addSessionCookie(Map<String, String> headers) {
        String sessionId = getPrefranceData(SESSION_COOKIE);
        if (sessionId.length() > 0) {
            StringBuilder builder = new StringBuilder();
            builder.append(SESSION_COOKIE);
            builder.append("=");
            builder.append(sessionId);
            if (headers.containsKey(COOKIE_KEY)) {
                builder.append("; ");
                builder.append(headers.get(COOKIE_KEY));
            }
            headers.put(COOKIE_KEY, builder.toString());
        }
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