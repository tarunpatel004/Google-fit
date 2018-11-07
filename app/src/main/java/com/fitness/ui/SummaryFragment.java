package com.fitness.ui;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fitness.Application;
import com.fitness.util.Constants;
import com.fitness.util.GoogleApiHelper;
import com.fitness.R;
import com.fitness.util.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DailyTotalResult;
import com.google.android.gms.fitness.result.DataReadResult;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarView;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;
import okhttp3.internal.Util;

/**
 * A simple {@link Fragment} subclass.
 */
public class SummaryFragment extends Fragment {


    @BindView(R.id.calendarView)
    HorizontalCalendarView calendarView;
    Unbinder unbinder;
    @BindView(R.id.progress_steps)
    CircularProgressBar progressSteps;
    @BindView(R.id.txt_steps)
    TextView txtSteps;
    @BindView(R.id.progress_calories)
    CircularProgressBar progressCalories;
    @BindView(R.id.txt_calories)
    TextView txtCalories;
    private Calendar selectedDate;
    private GoogleApiClient mGoogleAPIClient;
    private GoogleApiHelper googleAPIHelper;
    DateFormat dateFormat = DateFormat.getDateInstance();
    DateFormat timeFormat = DateFormat.getTimeInstance();

    public SummaryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_summary, container, false);
        unbinder = ButterKnife.bind(this, view);

        googleAPIHelper = new GoogleApiHelper(getActivity());

        mGoogleAPIClient = googleAPIHelper.getGoogleApiClient();

        initCalenderView(view);

        ((MainActivity) getActivity()).showMenu(false);
        getActivity().setTitle(getResources().getString(R.string.summary));

        return view;
    }


    private void initCalenderView(View view) {
         /* starts before 1 month from now */
        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.YEAR, -1);

/* ends after 1 month from now */
        Calendar endDate = Calendar.getInstance();
        selectedDate = Calendar.getInstance();

        HorizontalCalendar horizontalCalendar = new HorizontalCalendar.Builder(view, R.id.calendarView)
                .range(startDate, endDate)
                .datesNumberOnScreen(5)
                .build();

        getStepsAndCaloriesForToday();

        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {

                selectedDate = date;
                checkClientNGetData();
            }

            @Override
            public void onCalendarScroll(HorizontalCalendarView calendarView,
                                         int dx, int dy) {

            }

            @Override
            public boolean onDateLongClicked(Calendar date, int position) {
                return true;
            }
        });

    }

    /**
     * This method is used for call method of today's steps
     */
    private void getStepsAndCaloriesForToday() {

        if (googleAPIHelper.isConnected()) {
            new FetchCalorieForTodayAsync().execute();
            new FetchStepsForTodayAsync().execute();
        } else {
            googleAPIHelper.setConnectionListener(new GoogleApiHelper.ConnectionListener() {
                @Override
                public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                }

                @Override
                public void onConnectionSuspended(int i) {

                }

                @Override
                public void onConnected(Bundle bundle) {
                    new FetchCalorieForTodayAsync().execute();
                    new FetchStepsForTodayAsync().execute();
                }
            });
        }

    }

    private void checkClientNGetData() {

        if (googleAPIHelper.isConnected()) {
            new ViewSelectedDateData().execute();
        } else {

            googleAPIHelper.setConnectionListener(new GoogleApiHelper.ConnectionListener() {
                @Override
                public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                }

                @Override
                public void onConnectionSuspended(int i) {

                }

                @Override
                public void onConnected(Bundle bundle) {
                    new ViewSelectedDateData().execute();
                }

            });
        }
    }

    private class ViewSelectedDateData extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {

            displayStepDataForSelectedDay();
            displayCaloriesDataForSelectedDay();

            return null;
        }
    }

    //In use, call this every 30 seconds in active mode, 60 in ambient on watch faces
    private void displayStepDataForSelectedDay() {

        Log.e("selected date,", selectedDate.getTime().toString());

        DataSource ESTIMATED_STEP_DELTAS = new DataSource.Builder()
                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .setType(DataSource.TYPE_DERIVED)
                .setStreamName("estimated_steps")
                .setAppPackageName("com.google.android.gms")
                .build();

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                .aggregate(ESTIMATED_STEP_DELTAS, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByActivitySegment(1, TimeUnit.MILLISECONDS)
                .setTimeRange(Utils.getStartingMillisOfTheDay(selectedDate), Utils.getEndingMillisOfTheDay(selectedDate), TimeUnit.MILLISECONDS)
                .build();


        DataReadResult dataReadResult = Fitness.HistoryApi.readData(mGoogleAPIClient, readRequest).await(1, TimeUnit.MINUTES);

        //Used for aggregated data

        if (dataReadResult.getBuckets().size() > 0) {
            int total = 0;
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    if (dataSet.getDataType().getName().equals("com.google.step_count.delta")) {
                        if (dataSet.getDataPoints().size() > 0) {
                            // total steps
                            total = total + dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();

                        }
                    }
                }
                Log.e("Final steps for the day", "=====================" + total);
            }

            final int finalTotal = total;

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    progressSteps.setProgressMax(Float.parseFloat(Application.getPrefranceData(Constants.max_calories)));
                    progressSteps.setProgressWithAnimation(finalTotal);

                    txtSteps.setText(finalTotal + "");

                }
            });

        } else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressSteps.setProgressMax(Float.parseFloat(Application.getPrefranceData(Constants.max_calories)));
                    progressSteps.setProgressWithAnimation(0);

                    txtSteps.setText(0 + "");
                }
            });
        }
    }


    private void displayCaloriesDataForSelectedDay() {

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(Utils.getStartingMillisOfTheDay(selectedDate), Utils.getEndingMillisOfTheDay(selectedDate), TimeUnit.MILLISECONDS)
                .build();

        DataReadResult dataReadResult = Fitness.HistoryApi.readData(mGoogleAPIClient, readRequest).await(1, TimeUnit.MINUTES);

        //Used for aggregated data
        if (dataReadResult.getBuckets().size() > 0) {
            Log.e("Caloris", "Number of buckets: " + dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    showDataSet(dataSet);
                }
            }
        }
    }

    private void showDataSet(DataSet dataSet) {
        Log.e("History", "Data returned for Data type: " + dataSet.getDataType().getName());
        float total = 0;
        for (final DataPoint dp : dataSet.getDataPoints()) {
            Log.e("History", "Data point:");
            Log.e("History", "\tType: " + dp.getDataType().getName());
            Log.e("History", "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.e("History", "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            for (final Field field : dp.getDataType().getFields()) {
                Log.e("History", "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field));

                total = total + dp.getValue(field).asFloat();
            }
        }

        final float finalCalories = total;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressCalories.setProgressMax(Float.parseFloat(Application.getPrefranceData(Constants.max_calories)));
                progressCalories.setProgressWithAnimation(finalCalories);

                txtCalories.setText((int) finalCalories + "");
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    /***
     * Fetch calories for today only
     */
    private class FetchCalorieForTodayAsync extends AsyncTask<Object, Object, Double> {
        protected Double doInBackground(Object... params) {
            double total = 0;
            PendingResult<DailyTotalResult> result = Fitness.HistoryApi.readDailyTotal(mGoogleAPIClient, DataType.TYPE_CALORIES_EXPENDED);
            DailyTotalResult totalResult = result.await(30, TimeUnit.SECONDS);
            if (totalResult.getStatus().isSuccess()) {
                final DataSet totalSet = totalResult.getTotal();
                if (totalSet != null) {
                    total = totalSet.isEmpty()
                            ? 0
                            : totalSet.getDataPoints().get(0).getValue(Field.FIELD_CALORIES).asFloat();


                }
            } else {
                Log.w("Calories", "There was a problem getting the calories.");
            }

            final double finalTotal = total;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressCalories.setProgressMax(Float.parseFloat(Application.getPrefranceData(Constants.max_calories)));
                    progressCalories.setProgressWithAnimation((float) finalTotal);

                    txtCalories.setText((int) finalTotal + "");
                }
            });
            return total;
        }

    }


    /**
     * Fetch steps for today only
     */
    private class FetchStepsForTodayAsync extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            DailyTotalResult result = Fitness.HistoryApi.readDailyTotal(mGoogleAPIClient, DataType.TYPE_STEP_COUNT_DELTA).await(1, TimeUnit.MINUTES);

            int total = 0;

            DataSet dataSet = result.getTotal();
            for (final DataPoint dp : dataSet.getDataPoints()) {
                Log.e("History", "Data point:");
                Log.e("History", "\tType: " + dp.getDataType().getName());
                for (final Field field : dp.getDataType().getFields()) {
                    Log.e("History", "\tField: " + field.getName() +
                            " Value: " + dp.getValue(field));

                    total = total + dp.getValue(field).asInt();

                }
            }

            final int finalTotal = total;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressSteps.setProgressMax(Float.parseFloat(Application.getPrefranceData(Constants.max_steps)));
                    progressSteps.setProgressWithAnimation(finalTotal);

                    txtSteps.setText(finalTotal + "");
                }
            });
            return null;
        }
    }

}
