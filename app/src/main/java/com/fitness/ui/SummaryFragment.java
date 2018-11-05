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
        getActivity().setTitle("Summary");

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

        if(googleAPIHelper.isConnected()){
            new FetchCalorieForTodayAsync().execute();
            new FetchStepsForTodayAsync().execute();
        }else{
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

        if(googleAPIHelper.isConnected()){
            new ViewSelectedDateData().execute();
        }else {

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

      Log.e("selected date,",  selectedDate.getTime().toString());

        Calendar cal = selectedDate;

//        Date now = new Date();
//        cal.setTime(now);
//        cal.add(Calendar.DAY_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();
        cal.add(Calendar.HOUR_OF_DAY, 23);
        cal.add(Calendar.MINUTE, 59);
        cal.add(Calendar.SECOND, 59);
        long endTime = cal.getTimeInMillis();



        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        DataReadResult dataReadResult = Fitness.HistoryApi.readData(mGoogleAPIClient, readRequest).await(1, TimeUnit.MINUTES);

        //Used for aggregated data
        if (dataReadResult.getBuckets().size() > 0) {
            Log.e("History", "Number of buckets: " + dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    showDataSetForSteps(dataSet);


                }
            }
        }
    }


    private void displayCaloriesDataForSelectedDay() {

        Calendar cal = selectedDate;
//        Date now = new Date();
//        cal.setTime(now);
//        cal.add(Calendar.DAY_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();
        cal.add(Calendar.HOUR_OF_DAY, 23);
        cal.add(Calendar.MINUTE, 59);
        cal.add(Calendar.SECOND, 59);
        long endTime = cal.getTimeInMillis();


        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
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
        DateFormat dateFormat = DateFormat.getDateInstance();
        DateFormat timeFormat = DateFormat.getTimeInstance();

        for (final DataPoint dp : dataSet.getDataPoints()) {
            Log.e("History", "Data point:");
            Log.e("History", "\tType: " + dp.getDataType().getName());
            Log.e("History", "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.e("History", "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            for (final Field field : dp.getDataType().getFields()) {
                Log.e("History", "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field));

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressCalories.setProgressMax(Float.parseFloat(Application.getPrefranceData(Constants.max_calories)));
                        progressCalories.setProgressWithAnimation(dp.getValue(field).asFloat());

                        txtCalories.setText((int) dp.getValue(field).asFloat() + "");
                    }
                });

            }
        }
    }

    private void showDataSetForSteps(DataSet dataSet) {
        Log.e("History", "Data returned for Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = DateFormat.getDateInstance();
        DateFormat timeFormat = DateFormat.getTimeInstance();

        for (final DataPoint dp : dataSet.getDataPoints()) {
            Log.e("History", "Data point:");
            Log.e("History", "\tType: " + dp.getDataType().getName());
            Log.e("History", "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.e("History", "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            for (final Field field : dp.getDataType().getFields()) {
                Log.e("History", "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field));

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressSteps.setProgressMax(Float.parseFloat(Application.getPrefranceData(Constants.max_steps)));
                        progressSteps.setProgressWithAnimation(dp.getValue(field).asInt());

                        txtSteps.setText((int) dp.getValue(field).asInt() + "");
                    }
                });

            }
        }
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

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressCalories.setProgressMax(Float.parseFloat(Application.getPrefranceData(Constants.max_calories)));
                            progressCalories.setProgressWithAnimation(totalSet.getDataPoints().get(0).getValue(Field.FIELD_CALORIES).asFloat());

                            txtCalories.setText((int) totalSet.getDataPoints().get(0).getValue(Field.FIELD_CALORIES).asFloat() + "");
                        }
                    });

                }
            } else {
                Log.w("Calories", "There was a problem getting the calories.");
            }
            return total;
        }

    }


    /**
     * Fetch steps for today only
     */
    private class FetchStepsForTodayAsync extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            DailyTotalResult result = Fitness.HistoryApi.readDailyTotal(mGoogleAPIClient, DataType.TYPE_STEP_COUNT_DELTA).await(1, TimeUnit.MINUTES);


            DataSet dataSet = result.getTotal();
            for (final DataPoint dp : dataSet.getDataPoints()) {
                Log.e("History", "Data point:");
                Log.e("History", "\tType: " + dp.getDataType().getName());
                for (final Field field : dp.getDataType().getFields()) {
                    Log.e("History", "\tField: " + field.getName() +
                            " Value: " + dp.getValue(field));

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressSteps.setProgressMax(Float.parseFloat(Application.getPrefranceData(Constants.max_steps)));
                            progressSteps.setProgressWithAnimation(dp.getValue(field).asInt());

                            txtSteps.setText(dp.getValue(field).asInt() + "");
                        }
                    });

                }
            }
            return null;
        }
    }

}
