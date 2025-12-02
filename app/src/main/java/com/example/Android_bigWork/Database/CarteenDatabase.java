package com.example.Android_bigWork.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.Android_bigWork.Entity.Canteen;

@Database(entities = {Canteen.class}, version = 2, exportSchema = false)
public abstract class CarteenDatabase extends RoomDatabase {
    private static final String DB_NAME = "carteen.db";
    private static CarteenDatabase INSTANCE;

    public static synchronized CarteenDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            CarteenDatabase.class,
                            DB_NAME)
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return INSTANCE;
    }

    public abstract CanteenDao canteenDao();
}
