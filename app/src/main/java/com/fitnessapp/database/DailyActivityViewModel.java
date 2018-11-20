package com.fitnessapp.database;

import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import com.fitnessapp.Application;

/**
 * Created by Dell on 11/19/2018.
 */

public class DailyActivityViewModel extends AndroidViewModel {

    private final LiveData<DailyActivity> liveData;

    public DailyActivityViewModel(Application application, String date) {
        super(application);
        DailyActivityRepo mRepository = new DailyActivityRepo(application, date);
        liveData = mRepository.getTodaysActivity();
    }

    public LiveData<DailyActivity> getAllTodaysActivity() {
        return liveData;
    }

}