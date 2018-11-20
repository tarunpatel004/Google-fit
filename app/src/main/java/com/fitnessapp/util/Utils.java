package com.fitnessapp.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.fitnessapp.R;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class Utils {


    public static int getHeightenCm(float heightInMeter) {
        return (int) (heightInMeter * 100);
    }


    /**
     * Show alert toast.
     *
     * @param context the context
     * @param text    the text
     */
// for display toast in application
    public static void showAlertToast(Context context, String text) {
        try {
            if (text.length() > 0) {
                Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
                TextView v = toast.getView().findViewById(android.R.id.message);
                if (v != null) v.setGravity(Gravity.CENTER);
                toast.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
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
    // in parameter passed title and msg for the se content in dialog box
    public static void showAlertDialog(Activity context, String title, String text) {
        try {

            if (context.isFinishing()) {
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(text)
                    .setTitle(title)
                    .setCancelable(false)
                    .setPositiveButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
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

    public static String getTodayDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.date_format);
        return dateFormat.format(calendar.getTime());
    }

    public static String getFormattedDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.date_format);
        return dateFormat.format(date);
    }


    public static long getStartingMillisOfTheDay(Calendar selectedDate) {

        selectedDate.set(Calendar.YEAR, selectedDate.get(Calendar.YEAR));
        selectedDate.set(Calendar.MONTH, selectedDate.get(Calendar.MONTH));
        selectedDate.set(Calendar.DAY_OF_MONTH, selectedDate.get(Calendar.DAY_OF_MONTH));

        selectedDate.set(Calendar.HOUR_OF_DAY, 0);
        selectedDate.set(Calendar.MINUTE, 0);
        selectedDate.set(Calendar.SECOND, 0);
        selectedDate.set(Calendar.MILLISECOND, 0);

        return selectedDate.getTimeInMillis();

    }


    public static long getEndingMillisOfTheDay(Calendar selectedDate) {

        selectedDate.set(Calendar.YEAR, selectedDate.get(Calendar.YEAR));
        selectedDate.set(Calendar.MONTH, selectedDate.get(Calendar.MONTH));
        selectedDate.set(Calendar.DAY_OF_MONTH, selectedDate.get(Calendar.DAY_OF_MONTH));

        selectedDate.set(Calendar.HOUR_OF_DAY, 23);
        selectedDate.set(Calendar.MINUTE, 59);
        selectedDate.set(Calendar.SECOND, 59);
        selectedDate.set(Calendar.MILLISECOND, 999);

        return selectedDate.getTimeInMillis();

    }


    public static Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, Constants.ImageName, null);
        return Uri.parse(path);

    }

}

