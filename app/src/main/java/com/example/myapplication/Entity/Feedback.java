package com.example.myapplication.Entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "feedback_table")
public class Feedback implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;

    // 提交反馈的用户ID (可选，但推荐用于追溯)
    public int userId;

    public String title;
    public String content;
    public String submitDate; // 提交时间 (格式如: yyyy-MM-dd HH:mm:ss)
    public boolean isProcessed; // 【核心字段】是否已处理，默认 false

    // 构造函数 (请根据您的实际需求定义)
//    public Feedback(int userId, String title, String content, String submitDate, boolean isProcessed) {
//        this.userId = userId;
//        this.title = title;
//        this.content = content;
//        this.submitDate = submitDate;
//        this.isProcessed = isProcessed;
//    }

    // Room 需要的无参构造函数 (如果您的 Room 配置需要)
    public Feedback() {}
}
