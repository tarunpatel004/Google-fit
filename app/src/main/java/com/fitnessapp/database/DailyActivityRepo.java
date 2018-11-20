package com.fitnessapp.database;

import android.arch.lifecycle.LiveData;

import com.fitnessapp.Application;

/**
 * Created by Dell on 11/19/2018.
 */

public class DailyActivityRepo {


    private final LiveData<DailyActivity> liveData;


    public DailyActivityRepo(Application application, String date) {
        DatabaseHelper db = DatabaseHelper.getInstance(application);
        MyDao myDao = db.myDao();
        liveData = myDao.getTodaysLiveData(date);
    }


    LiveData<DailyActivity> getTodaysActivity() {
        return liveData;
    }
}
