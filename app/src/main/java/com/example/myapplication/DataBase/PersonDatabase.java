package com.example.myapplication.DataBase;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.myapplication.Entity.Person;

@Database(entities = {Person.class}, version = 1, exportSchema = false)
public abstract class PersonDatabase extends RoomDatabase {
    private static final String DB_NAME = "user.db";
    private static PersonDatabase INSTANCE;

    public static synchronized PersonDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            PersonDatabase.class,
                            DB_NAME)
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return INSTANCE;
    }

    public abstract PersonDao personDao();
}
