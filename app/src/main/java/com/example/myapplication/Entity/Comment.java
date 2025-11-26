package com.example.myapplication.Entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

// 对应表名 Comment
@Entity(tableName = "comment_table")
public class Comment implements Serializable {
    // 评论唯一ID（主键，自增）
    @PrimaryKey(autoGenerate = true)
    public int commentId;

    // 被评论菜品ID（外键）
    public int dishId;
    // 评论用户ID（外键）
    public int userId;
    // 评分（1-5星）
    public int rating;
    // 评论内容
    public String content;
    // 评论时间（使用 long 存储时间戳）
    public long commentTime;

    public Comment(int dishId, int userId, int rating, String content, long commentTime) {
        this.dishId = dishId;
        this.userId = userId;
        this.rating = rating;
        this.content = content;
        this.commentTime = commentTime;
    }

    // 省略 getter/setter 和 toString() 方法...
}
