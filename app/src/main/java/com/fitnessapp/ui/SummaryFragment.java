package com.fitnessapp.ui;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fitnessapp.Application;
import com.fitnessapp.R;
import com.fitnessapp.database.DailyActivity;
import com.fitnessapp.database.InsertDBTask;
import com.fitnessapp.util.Constants;
import com.fitnessapp.util.GoogleApiHelper;
import com.fitnessapp.util.Utils;
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


    private int steps;
    private int calories;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_summary, container, false);
        unbinder = ButterKnife.bind(this, view);
        initProgress();

        googleAPIHelper = new GoogleApiHelper(getActivity());
        mGoogleAPIClient = googleAPIHelper.getGoogleApiClient();

        initCalenderView(view);

        ((MainActivity) getActivity()).showMenu(false);
        getActivity().setTitle(getResources().getString(R.string.summary));

        return view;
    }

    /**
     * clear local variable before add into db
     */
    private void clearValues() {
        steps = 0;
        calories = 0;
    }


    /**
     * Set max values to the progressbar according to the goal set in profile
     */
    private void initProgress() {
        progressSteps.setProgressMax(Float.parseFloat(Application.getPrefaceData(Constants.max_steps)));
        progressCalories.setProgressMax(Float.parseFloat(Application.getPrefaceData(Constants.max_calories)));
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

        getStepsAndCaloriesForTodayFromDB();

        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {

                selectedDate = date;
                getStepsAndCaloriesForTodayFromDB();

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
     * This function check if records are already in the database then directly display from database
     * or else it will call api and then store into db
     */
    private void getStepsAndCaloriesForTodayFromDB() {

        try {
            DailyActivity dailyActivity = ((MainActivity) getActivity()).databaseHelper.myDao().getTodayRecord(Utils.getFormattedDate(selectedDate.getTime()));

            if (dailyActivity != null) {
                updateViews(dailyActivity);
            } else if (Utils.getTodayDate().equals(Utils.getFormattedDate(selectedDate.getTime()))) {
                getStepsAndCaloriesForToday();
            } else {
                checkClientNGetData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void updateViews(DailyActivity dailyActivity) {
        progressCalories.setProgressWithAnimation(Float.parseFloat(dailyActivity.getCalories()));
        progressSteps.setProgressWithAnimation(Float.parseFloat(dailyActivity.getSteps()));

        txtCalories.setText(dailyActivity.getCalories());
        txtSteps.setText(dailyActivity.getSteps());
    }

    /**
     * This method is used for call method of today's steps from live api
     */
    private void getStepsAndCaloriesForToday() {

        if (googleAPIHelper.isConnected()) {
            new FetchCalorieForTodayAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            new FetchStepsForTodayAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
                    new FetchCalorieForTodayAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    new FetchStepsForTodayAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            });
        }

    }

    private void checkClientNGetData() {

        clearValues();
        if (googleAPIHelper.isConnected()) {
            new ViewSelectedDateData().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
                    new ViewSelectedDateData().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

//        Log.e("selected date,", selectedDate.getTime().toString());

        DataSource ESTIMATED_STEP_DELTAS = new DataSource.Builder()
                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .setType(DataSource.TYPE_DERIVED)
                .setStreamName(getResources().getString(R.string.res))
                .setAppPackageName(getResources().getString(R.string.package_name))
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
                    if (dataSet.getDataType().getName().equals(getResources().getString(R.string.get_res))) {
                        if (dataSet.getDataPoints().size() > 0) {
                            // total steps
                            total = total + dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();

                        }
                    }
                }
//                Log.e("Final steps for the day", "=====================" + total);
            }

            final int finalTotal = total;
            steps = finalTotal;

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    progressSteps.setProgressWithAnimation(finalTotal);

                    txtSteps.setText(finalTotal + "");


                }
            });

        } else {
            steps = 0;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
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
//            Log.e("Calories", "Number of buckets: " + dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    showDataSet(dataSet);
                }
            }
        }
    }

    private void showDataSet(DataSet dataSet) {
//        Log.e("History", "Data returned for Data type: " + dataSet.getDataType().getName());
        float total = 0;
        for (final DataPoint dp : dataSet.getDataPoints()) {
//            Log.e("History", "Data point:");
//            Log.e("History", "\tType: " + dp.getDataType().getName());
//            Log.e("History", "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
//            Log.e("History", "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            for (final Field field : dp.getDataType().getFields()) {
//                Log.e("History", "\tField: " + field.getName() +
//                        " Value: " + dp.getValue(field));

                total = total + dp.getValue(field).asFloat();
            }
        }

        calories = (int) total;

        final float finalCalories = total;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressCalories.setProgressWithAnimation(finalCalories);

                txtCalories.setText((int) finalCalories + "");
            }
        });

        new InsertDBTask(getActivity(), new DailyActivity(Utils.getFormattedDate(selectedDate.getTime()), String.valueOf(steps), String.valueOf(calories))).execute();


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
            }

            final double finalTotal = total;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
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
//                Log.e("History", "Data point:");
//                Log.e("History", "\tType: " + dp.getDataType().getName());
                for (final Field field : dp.getDataType().getFields()) {
//                    Log.e("History", "\tField: " + field.getName() +
//                            " Value: " + dp.getValue(field));

                    total = total + dp.getValue(field).asInt();

                }
            }

            final int finalTotal = total;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressSteps.setProgressWithAnimation(finalTotal);

                    txtSteps.setText(finalTotal + "");
                }
            });
            return null;
        }
    }


}
