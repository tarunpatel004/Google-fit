package com.fitness.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fitness.Application;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class Utils {


    private static ProgressDialog pDialog;

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

    public static float getHeightinFeetCm(float heightInMeter) {
        return (float) (heightInMeter * 3.28084);
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
     * Gets image.
     *
     * @param src  the src
     * @param opts the opts
     * @return the image
     */
    public static Bitmap getImage(byte[] src, BitmapFactory.Options opts) {
        try {
            if (src != null)
                return BitmapFactory.decodeByteArray(src, 0, src.length, opts);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets bitmap from byte.
     *
     * @param strByte the str byte
     * @return the bitmap from byte
     */
    public static Bitmap getBitmapFromByte(String strByte) {
        try {
            byte[] imgByte = strByte.getBytes("UTF-8");
            return BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Bit map to string string.
     *
     * @param imgurl the imgurl
     * @return the string
     */
    public static String BitMapToString(String imgurl) {

        try {
            URL url = new URL(imgurl);
            Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] arr = baos.toByteArray();
            String result = Base64.encodeToString(arr, Base64.DEFAULT);
            return result;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * String to bit map bitmap.
     *
     * @param image the image
     * @return the bitmap
     */
    public static Bitmap StringToBitMap(String image) {
        try {

            byte[] encodeByte = Base64.decode(image, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    /**
     * Copy stream.
     *
     * @param is the is
     * @param os the os
     */
    public static void CopyStream(InputStream is, OutputStream os) {
        final int buffer_size = 1024;
        try {

            byte[] bytes = new byte[buffer_size];
            for (; ; ) {
                //Read byte from input stream

                int count = is.read(bytes, 0, buffer_size);
                if (count == -1)
                    break;

                //Write byte from output stream
                os.write(bytes, 0, count);
            }
        } catch (Exception ex) {
        }
    }

    /**
     * Gets asset image.
     *
     * @param context  the context
     * @param filename the filename
     * @return the asset image
     * @throws IOException the io exception
     */
    public static Drawable getAssetImage(Context context, String filename) throws IOException {
        try {
            AssetManager assets = context.getResources().getAssets();
            InputStream buffer = new BufferedInputStream((assets.open("drawables/" + filename + ".png")));
            Bitmap bitmap = BitmapFactory.decodeStream(buffer);
            return new BitmapDrawable(context.getResources(), bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Small screen boolean.
     *
     * @param context the context
     * @return the boolean
     */
    public static boolean smallScreen(Context context) {
        try {
            Configuration configuration = context.getResources().getConfiguration();
            int screenWidthDp = configuration.screenWidthDp;
            return screenWidthDp < 400;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void showToast(Activity localActivity, String value, int display) {
        try {
            Toast toast = Toast.makeText(localActivity, value, display);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } catch (Exception e) {

        }
    }

    /**
     * Remove the transparency from the image
     *
     * @param - Bitmap the bitmap
     * @return the bitmap
     */
    public static Bitmap cropBitmapTransparency(Bitmap sourceBitmap) {
        int minX = sourceBitmap.getWidth();
        int minY = sourceBitmap.getHeight();
        int maxX = -1;
        int maxY = -1;
        for (int y = 0; y < sourceBitmap.getHeight(); y++) {
            for (int x = 0; x < sourceBitmap.getWidth(); x++) {
                int alpha = (sourceBitmap.getPixel(x, y) >> 24) & 255;
                if (alpha > 0)   // pixel is not 100% transparent
                {
                    if (x < minX)
                        minX = x;
                    if (x > maxX)
                        maxX = x;
                    if (y < minY)
                        minY = y;
                    if (y > maxY)
                        maxY = y;
                }
            }
        }
        if ((maxX < minX) || (maxY < minY))
            return null; // Bitmap is entirely transparent

        // crop bitmap to non-transparent area and return:
        return Bitmap.createBitmap(sourceBitmap, minX, minY, (maxX - minX) + 1, (maxY - minY) + 1);
    }

    public static String getLocaleStringResource(Locale requestedLocale, int resourceId, Context context) {
        String result;
        if (requestedLocale == null) {
            requestedLocale = new Locale("en");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) { // use latest api
            Configuration config = new Configuration(context.getResources().getConfiguration());
            config.setLocale(requestedLocale);
            result = context.createConfigurationContext(config).getText(resourceId).toString();
        } else { // support older android versions
            Resources resources = context.getResources();
            Configuration conf = resources.getConfiguration();
            Locale savedLocale = conf.locale;
            conf.locale = requestedLocale;
            resources.updateConfiguration(conf, null);

            // retrieve resources from desired locale
            result = resources.getString(resourceId);

            // restore original locale
            conf.locale = savedLocale;
            resources.updateConfiguration(conf, null);
        }

        return result;
    }

    /**
     * Show progress dialog.
     *
     * @param Title        the title
     * @param Message      the message
     * @param isCancelable the is cancelable
     */
    public static void showProgressDialog(Context context, String Title, String Message, boolean isCancelable) {
        try {
            if (pDialog == null)
                pDialog = new ProgressDialog(context);
            if (Title != null && Title.length() > 0)
                pDialog.setTitle(Title);
            if (Message != null && Message.length() > 0)
                pDialog.setMessage(Message);
            pDialog.setCancelable(isCancelable);
            pDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Dismiss progress dialog.
     */
    public static void dismissProgressDialog() {
        try {
            if (pDialog != null)
                pDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static boolean vlidateEditText(EditText editText, String message) {
        if (!editText.getText().toString().trim().isEmpty())
            return false;
        else {
            editText.setError(message);
            return true;
        }
    }

    public static String convertImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos); //bm is the bitmap object
        byte[] b = baos.toByteArray();

        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    public static String convertDatetoString(Date date) {


        Format formatter = new SimpleDateFormat("MM/dd/yyyy");
        String s = formatter.format(date);

        return s;

    }


    public static void hideKeybord(FragmentActivity activity) {

        try {
            InputMethodManager inputManager = (InputMethodManager) activity
                    .getSystemService(Context.INPUT_METHOD_SERVICE);

            // check if no view has focus:
            View currentFocusedView = activity.getCurrentFocus();
            if (currentFocusedView != null) {
                inputManager.hideSoftInputFromWindow(currentFocusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static String getDiscountPercentage(String price, String saleprice) {
        double diference = Double.parseDouble(price) - Double.parseDouble(saleprice);
        double percentage = (100 * diference) / Double.parseDouble(price);

        return getDecimalFormatedText(String.format("%.2f", percentage) + "");
    }

    public static void setHorizontalLayoutManager(Context context, RecyclerView recycleView) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recycleView.setLayoutManager(linearLayoutManager);
    }

    public static String getDecimalFormatedText(String price) {
        String parseText = price;

        try {
            DecimalFormat format = new DecimalFormat();
            parseText = format.format(Double.parseDouble(price));
        } catch (Exception ex) {

        }
        return parseText;
    }

    public static byte[] readBytesFromFile(String filePath) {


        FileInputStream fileInputStream = null;
        byte[] bytesArray = null;

        try {

            File file = new File(filePath);
            bytesArray = new byte[(int) file.length()];

            //read file into bytes[]
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bytesArray);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        return bytesArray;


    }

    public static int getAge(String birthday) {
        SimpleDateFormat formate = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = formate.parse(birthday);

//            Calendar dob = Calendar.getInstance();
//            Calendar today = Calendar.getInstance();
//
//            dob.set(date.getYear(), date.getMonth(), date.getDay());
//
//            int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
//
//            if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
//                age--;
//            }
//
//            Integer ageInt = new Integer(age);


            int age;

            final Calendar calenderToday = Calendar.getInstance();
            int currentYear = calenderToday.get(Calendar.YEAR);
            int currentMonth = 1 + calenderToday.get(Calendar.MONTH);
            int todayDay = calenderToday.get(Calendar.DAY_OF_MONTH);

            SimpleDateFormat df = new SimpleDateFormat("yyyy");
            int year = Integer.parseInt(df.format(date));

            age = currentYear - year;

            if (date.getMonth() + 1 > currentMonth) {
                --age;
            } else if (date.getMonth() + 1 == currentMonth) {
                if (date.getDate() > todayDay) {
                    --age;
                }
            }
            return age;

        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }

    }

    public static String getDeviceID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    public static long getStartingMilliOftheDay(Calendar selectedDate) {

        Calendar c = selectedDate;
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long millis = (System.currentTimeMillis() - c.getTimeInMillis());

        Log.e("Starting Millis",millis+"==========");
        return millis;

    }


    public static long getEndingMilliOftheDay(Calendar selectedDate) {


        Calendar c = selectedDate;

        c.set(Calendar.MILLISECOND, 999);

        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        long millis = (System.currentTimeMillis() - c.getTimeInMillis());
        Log.e("Ending Millis",millis+"==========");

        return millis;

    }





//    public static float getStepsPercentage(int i) {
//
//        Application.getPrefranceData(Constants.max_steps)
//    }
}

