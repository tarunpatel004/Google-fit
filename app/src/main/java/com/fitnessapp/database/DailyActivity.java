package com.fitnessapp.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.fitnessapp.util.Constants;

/**
 * Created by Dell on 11/18/2018.
 */
@Entity(tableName = Constants.TABLE_NAME_DAILYACTIVITY)
public class DailyActivity {

    @PrimaryKey
    @NonNull
    private String record_date;
    private String steps;
    private String calories;

    public DailyActivity() {

    }

    public DailyActivity(@NonNull String record_date, String steps, String calories) {
        this.record_date = record_date;
        this.steps = steps;
        this.calories = calories;
    }

    public String getRecord_date() {
        return record_date;
    }

    public void setRecord_date(String record_date) {
        this.record_date = record_date;
    }

    public String getSteps() {
        return steps;
    }

    public void setSteps(String steps) {
        this.steps = steps;
    }

    public String getCalories() {
        return calories;
    }

    public void setCalories(String calories) {
        this.calories = calories;
    }
}
