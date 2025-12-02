package com.example.Android_bigWork.Database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.Android_bigWork.Entity.Feedback;

import java.util.List;

@Dao
public interface FeedbackDao {
    @Insert
    void insert(Feedback feedback);

    @Update
    void update(Feedback feedback);

    // 查询所有反馈，未处理的排在前面，按提交时间降序
    @Query("SELECT * FROM feedback_table ORDER BY isProcessed ASC, submitDate DESC")
    List<Feedback> getAllFeedback();

}