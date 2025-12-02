package com.example.Android_bigWork.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.Android_bigWork.Entity.Comment;
import com.example.Android_bigWork.Entity.Dish;
import com.example.Android_bigWork.Entity.Favorite; // 【新增】导入 Favorite 实体
import com.example.Android_bigWork.Entity.UserDish;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors; // 确保导入

@Database(entities = {Dish.class, Comment.class, Favorite.class, UserDish.class}, version = 11, exportSchema = false)
public abstract class DishDatabase extends RoomDatabase {
    private static final String DB_NAME = "dish.db";
    private static DishDatabase INSTANCE;
    // 【新增】: 线程池执行器 (与 UserDishDatabase 中的定义一致)
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService DatabaseExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    /**
     * 获取数据库实例
     *
     * @param context 上下文环境
     * @return DishDatabase
     * @Author Anduin9527
     * @date 2022/10/9 19:27
     * @commit
     */
    public static synchronized DishDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            DishDatabase.class,
                            DB_NAME)
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return INSTANCE;
    }

    public abstract DishDao getDishDao();
    public abstract CommentDao getCommentDao();
    public abstract FavoriteDao getFavoriteDao(); // 【新增】

    public abstract UserDishDao userDishDao(); // 【新增/确认】: 必须有这个方法，供 Repository 调用

}


