package com.example.myapplication.DataBase;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.myapplication.Entity.Windows;

@Database(entities = {Windows.class}, version = 1, exportSchema = false)
public abstract class WindowDatabase extends RoomDatabase {
    private static final String DB_NAME = "structure.db";
    private static WindowDatabase INSTANCE;

    public static synchronized WindowDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            WindowDatabase.class,
                            DB_NAME)
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return INSTANCE;
    }

    public abstract WindowDao windowDao();
}
