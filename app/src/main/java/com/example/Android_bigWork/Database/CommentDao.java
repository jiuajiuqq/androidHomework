package com.example.Android_bigWork.Database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.Android_bigWork.Entity.Comment;

import java.util.List;

/**
 * @author YourName
 * @Type CommentDao
 * @Desc 评论数据访问对象接口
 * @date 2025/11/26
 */
@Dao
public interface CommentDao {

    /**
     * 插入一条新评论
     *
     * @param comment 要插入的评论对象
     * @Author YourName
     */
    @Insert
    void insertComment(Comment comment);

    /**
     * 根据菜品ID查询所有评论，并按时间倒序排列
     *
     * @param dishId 菜品 GID
     * @return 评论列表
     * @Author YourName
     */
    @Query("SELECT * FROM comment_table WHERE dishId = :dishId ORDER BY timestamp DESC")
    List<Comment> getCommentsForDish(int dishId);

    // 【可选功能】: 查询平均评分
    /**
     * 根据菜品ID查询平均评分
     *
     * @param dishId 菜品 GID
     * @return 平均评分
     */
    @Query("SELECT AVG(rating) FROM comment_table WHERE dishId = :dishId")
    float getAverageRatingForDish(int dishId);

    // 【可选功能】: 删除一条评论
    // @Delete
    // void deleteComment(Comment comment);
}
