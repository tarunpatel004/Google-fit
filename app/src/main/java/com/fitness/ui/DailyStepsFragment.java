package com.fitness.ui;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fitness.Application;
import com.fitness.util.Constants;
import com.fitness.util.GoogleApiHelper;
import com.fitness.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.result.DailyTotalResult;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.text.DateFormat;
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

    public DailyStepsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_daily_steps, container, false);
        unbinder = ButterKnife.bind(this, view);

        mGoogleAPIClient = new GoogleApiHelper(getActivity()).getGoogleApiClient();

        new ViewTodaysStepCountTask().execute();
        new FetchCalorieAsync().execute();


        ((MainActivity) getActivity()).showMenu(true);

        getActivity().setTitle(getResources().getString(R.string.today));
        return view;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    private class ViewTodaysStepCountTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            displayStepDataForToday();
            return null;
        }
    }

    //In use, call this every 30 seconds in active mode, 60 in ambient on watch faces
    private void displayStepDataForToday() {
        DailyTotalResult result = Fitness.HistoryApi.readDailyTotal(mGoogleAPIClient, DataType.TYPE_STEP_COUNT_DELTA).await(1, TimeUnit.MINUTES);
        showDataSet(result.getTotal());
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
                        progressSteps.setProgressMax(Float.parseFloat(Application.getPrefranceData(Constants.max_steps)));
                        progressSteps.setProgressWithAnimation(dp.getValue(field).asInt());

                        txtSteps.setText(dp.getValue(field).asInt() + "");
                    }
                });

            }
        }
    }

    private class FetchCalorieAsync extends AsyncTask<Object, Object, Double> {
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

}
