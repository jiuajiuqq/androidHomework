package com.example.myapplication.DataBase;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.myapplication.Entity.Comment;

@Database(entities = {Comment.class}, version = 1, exportSchema = false)
public abstract class CommentDatabase extends RoomDatabase {
    private static final String DB_NAME = "comment.db";
    private static CommentDatabase INSTANCE;

    public static synchronized CommentDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            CommentDatabase.class,
                            DB_NAME)
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return INSTANCE;
    }

    public abstract CommentDao commentDao();
}
