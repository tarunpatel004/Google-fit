package com.fitness.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.fitness.Application;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;


public class Utils {

    /**
     * Check Internet Connection
     * <p>
     * base activity
     *
     * @return boolean Utils
     */
    // check internet connection
    public static boolean checkInternetConnection(Context context) {
        try {
            ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            boolean status = conMgr.getActiveNetworkInfo() != null && conMgr.getActiveNetworkInfo().isAvailable() && conMgr.getActiveNetworkInfo().isConnected();
            if (status)
                return status;
            else {

                Utils.showAlertToast(Application.get(), "Internet connection is not available.");
                return status;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target)
                    .matches();
        }
    }

    public static int getHeightinCm(float heightInMeter) {
        return (int) (heightInMeter * 100);
    }

    private static void noInternetConnection() {
        try {
//            Intent noInternet = new Intent(Application.get(), NoInternetConnectionActivity.class);
//            Application.get().startActivity(noInternet);
//            Utils.showAlertToast(Application.get(), "Internet connection is not available.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Show alert toast.
     *
     * @param context the context
     * @param text    the text
     */
// for display tost in application
    public static void showAlertToast(Context context, String text) {
        try {
            if (text.length() > 0) {
                Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
                TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                if (v != null) v.setGravity(Gravity.CENTER);
                toast.show();
            }
        } catch (Exception e) {
        }
    }

    /**
     * Show alert dialog.
     *
     * @param context the context
     * @param title   the title
     * @param text    the text
     */
// display alert dialog box
    // in prameter passed title and messge for the se content in dialog box
    public static void showAlertDialog(Activity context, String title, String text) {
        try {

            if (context.isFinishing()) {
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(text)
                    .setTitle(title)
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //do things
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Bitmap createBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        else
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
//        return bitmap;
    }


    /**
     * This will return starting milliseconds of the selected day
     *
     * @param selectedDate
     * @return
     */
    public static long getStartingMillisOfTheDay(Calendar selectedDate) {

        Calendar startCalendar = selectedDate;

        startCalendar.set(Calendar.YEAR, selectedDate.get(Calendar.YEAR));
        startCalendar.set(Calendar.MONTH, selectedDate.get(Calendar.MONTH));
        startCalendar.set(Calendar.DAY_OF_MONTH, selectedDate.get(Calendar.DAY_OF_MONTH));

        startCalendar.set(Calendar.HOUR_OF_DAY, 0);
        startCalendar.set(Calendar.MINUTE, 0);
        startCalendar.set(Calendar.SECOND, 0);
        startCalendar.set(Calendar.MILLISECOND, 0);

        return startCalendar.getTimeInMillis();

    }

    /**
     * This will return ending millis of the selected day
     *
     * @param selectedDate
     * @return
     */
    public static long getEndingMillisOfTheDay(Calendar selectedDate) {

        Calendar startCalendar = selectedDate;

        startCalendar.set(Calendar.YEAR, selectedDate.get(Calendar.YEAR));
        startCalendar.set(Calendar.MONTH, selectedDate.get(Calendar.MONTH));
        startCalendar.set(Calendar.DAY_OF_MONTH, selectedDate.get(Calendar.DAY_OF_MONTH));

        startCalendar.set(Calendar.HOUR_OF_DAY, 23);
        startCalendar.set(Calendar.MINUTE, 59);
        startCalendar.set(Calendar.SECOND, 59);
        startCalendar.set(Calendar.MILLISECOND, 999);

        return startCalendar.getTimeInMillis();

    }

    /**
     * This function get Image URI from bitmap
     *
     * @param inContext
     * @param inImage
     * @return
     */
    public static Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "tmp", null);
        return Uri.parse(path);

    }

}

