package com.fitnessapp.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.RemoteViews;
import com.fitnessapp.R;
import com.fitnessapp.database.DailyActivity;
import com.fitnessapp.database.DatabaseHelper;
import com.fitnessapp.ui.SplashActivity;
import com.fitnessapp.util.Utils;


public class MyWidgetProvider extends AppWidgetProvider {

    private static final String WIDGET_UPDATE = "com.fitnessapp.widget.UPDATE";

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


        try {

            // Get all ids
            ComponentName thisWidget = new ComponentName(context,
                    MyWidgetProvider.class);
            int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

            for (int widgetId : allWidgetIds) {
                RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                        R.layout.widget_layout);

                DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);

                DailyActivity dailyActivity = databaseHelper.myDao().getTodayRecord(Utils.getTodayDate());
                //Update the views according to values

                if (dailyActivity != null) {
                    remoteViews.setTextViewText(R.id.txt_steps, dailyActivity.getSteps().isEmpty() ? "0" : dailyActivity.getSteps());
                    remoteViews.setTextViewText(R.id.txt_calories, dailyActivity.getCalories().isEmpty() ? "0" : dailyActivity.getCalories());
                } else {
                    remoteViews.setTextViewText(R.id.txt_steps, "0");
                    remoteViews.setTextViewText(R.id.txt_calories, "0");
                }


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
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}