package com.fitness.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.fitness.Application;
import com.fitness.R;
import com.fitness.ui.MainActivity;
import com.fitness.ui.SplashActivity;
import com.fitness.util.Constants;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class MyWidgetProvider extends AppWidgetProvider {

    Context mContext;
    RemoteViews remoteViews;
    AppWidgetManager mappWidgetManager;
    int[] allWidgetIds;
    int[] appWidgetIds;
    int mWidgetId;
    public static String WIDGET_UPDATE = "com.fitness.widget.UPDATE";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (WIDGET_UPDATE.equals(intent.getAction())) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                ComponentName thisAppWidget = new ComponentName(context.getPackageName(), MyWidgetProvider.class.getName());
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
                onUpdate(context, appWidgetManager, appWidgetIds);
            }
        }
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        this.mContext = context;
        this.mappWidgetManager = appWidgetManager;
        this.appWidgetIds = appWidgetIds;

        Log.d("mytag", "UPDate");

        try {

            // Get all ids
            ComponentName thisWidget = new ComponentName(context,
                    MyWidgetProvider.class);
            allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

            for (int widgetId : allWidgetIds) {
                this.mWidgetId = widgetId;
                remoteViews = new RemoteViews(context.getPackageName(),
                        R.layout.widget_layout);

                //Update the views according to values
                remoteViews.setTextViewText(R.id.txt_steps, Application.getPrefranceData(Constants.TodaySteps).isEmpty() ? "0" : Application.getPrefranceData(Constants.TodaySteps));
                remoteViews.setTextViewText(R.id.txt_calories, Application.getPrefranceData(Constants.TodayCalories).isEmpty() ? "0" : Application.getPrefranceData(Constants.TodayCalories));

               //set click event
                Intent intent = new Intent(context, SplashActivity.class);
                intent.setData(Uri.parse(WIDGET_UPDATE));
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
                remoteViews.setOnClickPendingIntent(R.id.open_app_click, pendingIntent);
                appWidgetManager.updateAppWidget(widgetId, remoteViews);


            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(createIntent(context));
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 5 * 60 * 1000, createIntent(context));
    }

    private PendingIntent createIntent(Context context) {
        Intent intent = new Intent(WIDGET_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }
}