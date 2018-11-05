package com.fitness;

import android.content.Intent;
import android.icu.util.ValueIterator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Goal;
import com.google.android.gms.fitness.request.GoalsReadRequest;
import com.google.android.gms.fitness.result.DailyTotalResult;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.text.DateFormat;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Dell on 11/2/2018.
 */

public class BaseActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public GoogleApiClient mGoogleApiClient;
    public GoogleSignInAccount googleSigninAccount;


    public static final int GOOGLE_PROFILE_REQ = 107;

    public interface ConnectionSuccess{
        void onConnection();
    }

    ConnectionSuccess connectionSuccess;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    public void replaceFragment(Fragment fr, int id) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(id, fr);

//        if (fr instanceof MainFragment){
//            for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
//                fm.popBackStack();
//            }
//        }else {
//            fragmentTransaction.addToBackStack("abc");
//        }
        fragmentTransaction.commit();
    }

    public GoogleApiClient initGoogleApiClient(ConnectionSuccess connectionSuccess) {
        this.connectionSuccess = connectionSuccess;


        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            return mGoogleApiClient;
        }

        return mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.HISTORY_API)
                .addApi(Fitness.GOALS_API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addScope(new Scope(Scopes.PLUS_ME))
                .addScope(new Scope(Scopes.FITNESS_BODY_READ))
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .build();
    }public GoogleApiClient initGoogleApiClient() {


        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            return mGoogleApiClient;
        }

        return mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.HISTORY_API)
                .addApi(Fitness.GOALS_API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addScope(new Scope(Scopes.PLUS_ME))
                .addScope(new Scope(Scopes.FITNESS_BODY_READ))
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .build();
    }


    private void signIn() {

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestProfile()
                .build();

        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_PROFILE_REQ);

    }

    public class ViewTodaysStepCountTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            displayStepDataForToday();
            return null;
        }
    }

    public void displayStepDataForToday() {
        DailyTotalResult result = Fitness.HistoryApi.readDailyTotal(mGoogleApiClient, DataType.TYPE_STEP_COUNT_DELTA).await(1, TimeUnit.MINUTES);
        showDataSet(result.getTotal());
    }

    private void showDataSet(DataSet dataSet) {
        Log.e("History", "Data returned for Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = DateFormat.getDateInstance();
        DateFormat timeFormat = DateFormat.getTimeInstance();

        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.e("History", "Data point:");
            Log.e("History", "\tType: " + dp.getDataType().getName());
            Log.e("History", "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.e("History", "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            for (Field field : dp.getDataType().getFields()) {
                Log.e("History", "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field));
            }


        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

        Log.d("onConnected", "GoogleAPIClient is now connected for use");

        /**
         * This is we are calling
         */
//        signIn();

        if(connectionSuccess != null){
            connectionSuccess.onConnection();
        }
//        Intent i = new Intent("com.conneced");
//        sendBroadcast(i);


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//        googleSigninAccount = task.getResult();

//        new GetGoal().execute();
    }

    private class GetGoal extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            getUserGoal();
            return null;
        }
    }


    private void getUserGoal() {

//        Task<List<Goal>> response = Fitness.getGoalsClient(this, googleSigninAccount)
//                .readCurrentGoals(new GoalsReadRequest.Builder()
//                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
//                        .addDataType(DataType.TYPE_DISTANCE_DELTA)
//                        .build());

        Task<List<Goal>> response = Fitness.getGoalsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readCurrentGoals(
                        new GoalsReadRequest.Builder()
                                .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                                .addDataType(DataType.TYPE_DISTANCE_DELTA)
                                .build());

        try {
            List<Goal> goals = Tasks.await(response);
            Log.e("", "");

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
