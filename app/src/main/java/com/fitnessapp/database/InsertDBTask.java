package com.fitnessapp.database;

import android.content.Context;
import android.os.AsyncTask;


/**
 * This async task store the values of calories and steps into database according to the selected date
 */

public class InsertDBTask extends AsyncTask<Void, Void, Boolean> {
    private Context context;
    private DailyActivity dailyActivity;

    public InsertDBTask(Context context, DailyActivity dailyActivity) {
        this.context = context;
        this.dailyActivity = dailyActivity;

    }

    @Override
    protected Boolean doInBackground(Void... objs) {

        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);

        try {
            databaseHelper.myDao().addDailyRecord(dailyActivity);
        } catch (Exception ex) {
            databaseHelper.myDao().updateDailyRecord(dailyActivity);

        }
        return true;
    }

    // onPostExecute runs on main thread
    @Override
    protected void onPostExecute(Boolean bool) {
        /**
         * if true then success othewise it fails,
         * we can handle this from here
         * Update ui and all the stuffs goes to here
         */
    }

}
