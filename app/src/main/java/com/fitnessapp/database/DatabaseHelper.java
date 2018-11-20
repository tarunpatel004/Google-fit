package com.fitnessapp.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.fitnessapp.util.Constants;

/**
 * Created by Dell on 11/18/2018.
 */

@Database(entities = {DailyActivity.class}, version = 1)
public abstract class DatabaseHelper extends RoomDatabase {
    public abstract MyDao myDao();


    private static DatabaseHelper databaseHelper;

    public static DatabaseHelper getInstance(Context context) {
        if (null == databaseHelper) {
            databaseHelper = buildDatabaseInstance(context);
        }
        return databaseHelper;
    }

    private static DatabaseHelper buildDatabaseInstance(Context context) {
        return Room.databaseBuilder(context,
                DatabaseHelper.class,
                Constants.DB_NAME)
                .allowMainThreadQueries().build();
    }

    public void cleanUp(){
        databaseHelper = null;
    }
}
