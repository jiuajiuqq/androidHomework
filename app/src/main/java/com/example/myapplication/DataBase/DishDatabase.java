package com.example.myapplication.DataBase;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.myapplication.Entity.Dish;

@Database(entities = {Dish.class}, version = 1, exportSchema = false)
public abstract class DishDatabase extends RoomDatabase {
    private static final String DB_NAME = "dish.db";
    private static DishDatabase INSTANCE;

    /**
     * 获取菜品数据库实例 (单例模式)
     *
     * @param context 上下文环境
     * @return DishDatabase
     */
    public static synchronized DishDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            DishDatabase.class,
                            DB_NAME)
                    .fallbackToDestructiveMigration() // 允许破坏性迁移
                    .allowMainThreadQueries()         // 允许在主线程进行查询 (开发时方便)
                    .build();
        }
        return INSTANCE;
    }

    public abstract DishDao dishDao(); // 对应前面定义的 DishDao 接口
}
