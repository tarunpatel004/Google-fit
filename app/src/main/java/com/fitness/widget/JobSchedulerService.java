package com.fitness.widget;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.fitness.Application;
import com.fitness.util.Constants;
import com.fitness.util.GoogleApiHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.result.DailyTotalResult;

import java.util.concurrent.TimeUnit;

/**
 * Created by Dell on 11/14/2018.
 * This service is used for update the widget periodically
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class JobSchedulerService extends JobService {
    private GoogleApiHelper apiHelper;
    private GoogleApiClient mGoogleAPIClient;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {

        Log.e("Job ", "onStartJob: ");
        apiHelper = new GoogleApiHelper(this);
        mGoogleAPIClient = apiHelper.getGoogleApiClient();

        if (mGoogleAPIClient.isConnected()) {
            new StepsAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            new CaloriesAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            apiHelper.connect();
            apiHelper.setConnectionListener(new GoogleApiHelper.ConnectionListener() {
                @Override
                public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                }

                @Override
                public void onConnectionSuspended(int i) {

                }

                @Override
                public void onConnected(Bundle bundle) {
                    new StepsAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    new CaloriesAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            });
        }

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        if (mGoogleAPIClient != null && mGoogleAPIClient.isConnected()) {
            mGoogleAPIClient.disconnect();
        }
        return true;
    }

    private class StepsAsync extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            getStepsDataForToday();

            return null;
        }
    }

    private class CaloriesAsync extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            getCaloriesDataForToday();

            return null;
        }
    }

    private void getCaloriesDataForToday() {
        double total = 0;
        PendingResult<DailyTotalResult> result = Fitness.HistoryApi.readDailyTotal(mGoogleAPIClient, DataType.TYPE_CALORIES_EXPENDED);
        DailyTotalResult totalResult = result.await(30, TimeUnit.SECONDS);
        if (totalResult.getStatus().isSuccess()) {
            final DataSet totalSet = totalResult.getTotal();
            if (totalSet != null) {
                total = totalSet.isEmpty()
                        ? 0
                        : totalSet.getDataPoints().get(0).getValue(Field.FIELD_CALORIES).asFloat();

                final double finalTotal = total;

                Application.setPreferences(Constants.TodayCalories, (int) finalTotal + "");

                updateWidget();

            }
        } else {
            Log.w("Calories", "There was a problem getting the calories.");

        }
    }

    private void getStepsDataForToday() {
        DailyTotalResult result = Fitness.HistoryApi.readDailyTotal(mGoogleAPIClient, DataType.TYPE_STEP_COUNT_DELTA).await(1, TimeUnit.MINUTES);
        Log.e("Data", "From Job=====");
        for (final DataPoint dp : result.getTotal().getDataPoints()) {
            for (final Field field : dp.getDataType().getFields()) {
                Application.setPreferences(Constants.TodaySteps, dp.getValue(field).asInt() + "");
            }
        }
        updateWidget();
    }

    private void updateWidget() {
        Intent intent = new Intent(this, MyWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
// Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
// since it seems the onUpdate() is only fired on that:
        int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), MyWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }
}
