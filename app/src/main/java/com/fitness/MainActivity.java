package com.fitness;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DailyTotalResult;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 101;
    private static final String LOG_FITNESS = "Fitness";
    private GoogleSignInAccount lastSigninAcc;
    private FitnessOptions fitnessOptions;
    private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupLoginWithGoogle();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
                accessGoogleFit();
            }
        }

    }

    private void setupLoginWithGoogle() {
        fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .build();


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        MainActivity.this /* OnConnectionFailedListener */)
                .addApi(Fitness.HISTORY_API)
                .build();


        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this, // your activity
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    fitnessOptions);

            lastSigninAcc = GoogleSignIn.getLastSignedInAccount(this);


        } else {
            lastSigninAcc = GoogleSignIn.getLastSignedInAccount(this);

            accessGoogleFit();


            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    Calendar calendar = Calendar.getInstance();
                    Calendar calendar1 = Calendar.getInstance();

                    calendar.add(Calendar.DATE, -1);

                    final Task<DataReadResponse> response = Fitness.getHistoryClient(MainActivity.this, lastSigninAcc)
                            .readData(new DataReadRequest.Builder()
                                    .read(DataType.AGGREGATE_STEP_COUNT_DELTA)
                                    .setTimeRange(calendar.getTimeInMillis(), calendar1.getTimeInMillis(), TimeUnit.MILLISECONDS)
                                    .build());


                    response.addOnCompleteListener(new OnCompleteListener<DataReadResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<DataReadResponse> task) {
                            List<DataSet> dataSets = task.getResult().getDataSets();


                        }
                    });

                }
            });


//            MainActivity.this.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//
//                    DataReadResponse readDataResult = null;
//                    try {
//                        readDataResult = Tasks.await(response);
//                    } catch (ExecutionException e) {
//                        e.printStackTrace();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    DataSet dataSet = readDataResult.getDataSet(DataType.TYPE_STEP_COUNT_DELTA);
//
//                }
//            });
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    private class VerifyDataTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {

            long total = 0;


            PendingResult<DailyTotalResult> result = Fitness.HistoryApi.readDailyTotal(mGoogleApiClient, DataType.TYPE_STEP_COUNT_DELTA);
            DailyTotalResult totalResult = result.await(30, TimeUnit.DAYS);
            if (totalResult.getStatus().isSuccess()) {
                DataSet totalSet = totalResult.getTotal();
                total = totalSet.isEmpty()
                        ? 0
                        : totalSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
            } else {
                Log.w("VerifyData", "There was a problem getting the step count.");
            }

            Log.i("VerifyData", "Total steps: " + total);

            return null;
        }
    }

    private void accessGoogleFit() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.DATE, -1);
        long startTime = cal.getTimeInMillis();

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();




        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readData(readRequest)
                .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse dataReadResponse) {
                        Log.e("Data", dataReadResponse.getDataSets().toString());

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(LOG_FITNESS, "onFailure()", e);
                    }
                })
                .addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {

                        Log.d(LOG_FITNESS, "onComplete() " + task.getResult().toString());
                    }
                });

        new VerifyDataTask().execute();
    }
}
