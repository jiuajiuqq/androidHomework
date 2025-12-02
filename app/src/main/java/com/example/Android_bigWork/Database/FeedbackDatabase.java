package com.example.Android_bigWork.Database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.Android_bigWork.Entity.Feedback; // 确保导入 Feedback 实体

@Database(entities = {Feedback.class}, version = 2, exportSchema = false)
public abstract class FeedbackDatabase extends RoomDatabase {

    public abstract FeedbackDao feedbackDao();

    private static volatile FeedbackDatabase INSTANCE;
    private static final String Database_NAME = "feedback_Database";

    public static FeedbackDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (FeedbackDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    FeedbackDatabase.class,
                                    Database_NAME
                            )
                            // 在开发阶段，允许版本升级时破坏性重建数据库
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}