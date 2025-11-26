package com.example.myapplication.DataBase;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.Entity.Comment;
import java.util.List;

@Dao
public interface CommentDao {
    /**
     * 添加评论
     */
    @Insert
    void insert(Comment comment);

    /**
     * 删除指定评论
     */
    @Query("DELETE FROM comment_table WHERE commentId = :commentId")
    void deleteCommentById(int commentId);

    /**
     * 获取某菜品的所有评论 (用于菜品详情页展示)
     *
     * @param dishId 菜品ID
     * @return List<Comment>
     */
    @Query("SELECT * FROM comment_table WHERE dishId = :dishId ORDER BY commentTime DESC")
    List<Comment> getCommentsByDishId(int dishId);

    /**
     * 计算某菜品的平均评分
     *
     * @param dishId 菜品ID
     * @return double
     */
    @Query("SELECT AVG(rating) FROM comment_table WHERE dishId = :dishId")
    double getAverageRatingForDish(int dishId);

    /**
     * 获取用户对某菜品的评论 (用于防止重复评论)
     *
     * @param dishId 菜品ID
     * @param userId 用户ID
     * @return Comment
     */
    @Query("SELECT * FROM comment_table WHERE dishId = :dishId AND userId = :userId LIMIT 1")
    Comment getUserCommentForDish(int dishId, int userId);
}
