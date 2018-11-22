package com.fitnessapp.ui;


import android.arch.lifecycle.Observer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fitnessapp.Application;
import com.fitnessapp.database.DailyActivity;
import com.fitnessapp.database.DailyActivityViewModel;
import com.fitnessapp.database.InsertDBTask;
import com.fitnessapp.util.Constants;
import com.fitnessapp.util.GoogleApiHelper;
import com.fitnessapp.R;
import com.fitnessapp.util.Utils;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.result.DailyTotalResult;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class DailyStepsFragment extends Fragment {


    @BindView(R.id.progress_steps)
    CircularProgressBar progressSteps;
    @BindView(R.id.progress_calories)
    CircularProgressBar progressCalories;
    Unbinder unbinder;
    @BindView(R.id.txt_steps)
    TextView txtSteps;
    @BindView(R.id.txt_calories)
    TextView txtCalories;
    @BindView(R.id.ll_main)
    public LinearLayout llMain;
    private GoogleApiClient mGoogleAPIClient;

    int todayCalories = 0;
    int todaySteps = 0;




    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_daily_steps, container, false);
        unbinder = ButterKnife.bind(this, view);

        mGoogleAPIClient = new GoogleApiHelper(getActivity()).getGoogleApiClient();
        initProgress();
        getStepData();
        ((MainActivity) getActivity()).showMenu(true);
        getActivity().setTitle(getResources().getString(R.string.today));
        setDatabaseChangeListner();
        return view;
    }

    /**
     * Set max values to the progressbar according to the goal set in profile
     */
    private void initProgress() {
        progressSteps.setProgressMax(Float.parseFloat(Application.getPrefaceData(Constants.max_steps)));
        progressCalories.setProgressMax(Float.parseFloat(Application.getPrefaceData(Constants.max_calories)));
    }

    public void getStepData() {
        new ViewTodayStepCountTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    /**
     * this async task fetch data from fit api and then in it's post execution it start calories async task
     */
    private class ViewTodayStepCountTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            displayStepDataForToday();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            new FetchCalorieAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void displayStepDataForToday() {
        DailyTotalResult result = Fitness.HistoryApi.readDailyTotal(mGoogleAPIClient, DataType.TYPE_STEP_COUNT_DELTA).await(1, TimeUnit.MINUTES);
        showDataSet(result.getTotal());
    }

    private void showDataSet(DataSet dataSet) {
//        Log.e("History", "Data returned for Data type: " + dataSet.getDataType().getName());
        for (final DataPoint dp : dataSet.getDataPoints()) {
            for (final Field field : dp.getDataType().getFields()) {
//                Log.e("History", "\tField: " + field.getName() +
//                        " Value: " + dp.getValue(field));

                todaySteps = dp.getValue(field).asInt();

            }
        }


    }

    private class FetchCalorieAsync extends AsyncTask<Object, Object, Double> {
        protected Double doInBackground(Object... params) {
            displayStepDataForToday();
            double total = 0;
            PendingResult<DailyTotalResult> result = Fitness.HistoryApi.readDailyTotal(mGoogleAPIClient, DataType.TYPE_CALORIES_EXPENDED);
            DailyTotalResult totalResult = result.await(30, TimeUnit.SECONDS);
            if (totalResult.getStatus().isSuccess()) {
                final DataSet totalSet = totalResult.getTotal();
                if (totalSet != null) {
                    total = totalSet.isEmpty()
                            ? 0
                            : totalSet.getDataPoints().get(0).getValue(Field.FIELD_CALORIES).asFloat();

                    todayCalories = (int) total;

                }
            }
            return total;
        }

        @Override
        protected void onPostExecute(Double aDouble) {
            super.onPostExecute(aDouble);
            new InsertDBTask(getActivity(), new DailyActivity(Utils.getTodayDate(), String.valueOf(todaySteps), String.valueOf(todayCalories))).execute();
        }
    }

    private void setDatabaseChangeListner() {
        DailyActivityViewModel mWordViewModel = new DailyActivityViewModel(Application.get(), Utils.getTodayDate());
        mWordViewModel.getAllTodaysActivity().observe(this, new Observer<DailyActivity>() {
            @Override
            public void onChanged(@Nullable final DailyActivity dailyActivities) {

                if (dailyActivities != null) {
                    Log.e("Database", "change listner");
                    txtCalories.setText(dailyActivities.getCalories());
                    txtSteps.setText(dailyActivities.getSteps());

                    progressCalories.setProgressWithAnimation(Float.parseFloat(dailyActivities.getCalories()));
                    progressSteps.setProgressWithAnimation(Float.parseFloat(dailyActivities.getCalories()));


                }
                ((MainActivity) getActivity()).updateWidget();
            }
        });
    }

}
