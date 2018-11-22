package com.fitnessapp.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.fitnessapp.util.Constants;

/**
 * Created by Dell on 11/18/2018.
 */
@Dao
public interface MyDao {

    @Insert
    void addDailyRecord(DailyActivity dailyActivity);

    @Update
    void updateDailyRecord(DailyActivity dailyActivity);

    @Query("SELECT * FROM dailyactivity WHERE record_date = :date LIMIT 1")
    DailyActivity getTodayRecord(String date);

    @Query("DELETE FROM dailyactivity")
    void deleteAll();

    @Query("SELECT * FROM " + Constants.TABLE_NAME_DAILYACTIVITY +" WHERE record_date = :date LIMIT 1")
    LiveData<DailyActivity> getTodaysLiveData(String date);
}
