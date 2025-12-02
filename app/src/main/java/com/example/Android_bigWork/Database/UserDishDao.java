package com.example.Android_bigWork.Database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RoomWarnings;

import com.example.Android_bigWork.Entity.Dish;

import com.example.Android_bigWork.Entity.PopularDish;
import com.example.Android_bigWork.Entity.UserDish;

import java.util.List;

@Dao
public interface UserDishDao {
    @Insert
    void insert(UserDish userDish);
    @Query("SELECT * FROM order_table WHERE userName= :userName ORDER BY createdTime DESC")
    LiveData<List<UserDish>> getUserDishesForUser(String userName);

    @Query("SELECT * FROM order_table ORDER BY createdTime DESC")
    LiveData<List<UserDish>> getUserDishesFromAllUsers();

    @Query("SELECT * FROM order_table WHERE userName= :userName AND createdTime= :time")
    LiveData<List<UserDish>> getUserDishesForUserByTime(String userName,long time);
    // 【最终修正】: 添加 @SuppressWarnings 和正确的 SELECT 语句
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT T1.GID, T1.name, T1.description, T1.price, T1.category, T1.CID, T1.customizable, T1.spicy, T1.sweet, " +
            "T1.windowId, T1.imageUrl, T1.isAvailable, T1.remainingStock, " + // 🔴 关键新增：SELECT 子句中添加新字段
            "COALESCE(SUM(T2.count), 0) AS totalSales " +
            "FROM dish_table AS T1 " +
            "LEFT JOIN order_table AS T2 ON T1.GID = T2.GID " +
            "GROUP BY T1.GID, T1.name, T1.description, T1.price, T1.category, T1.CID, T1.customizable, T1.spicy, T1.sweet, " +
            "T1.windowId, T1.imageUrl, T1.isAvailable, T1.remainingStock " + // 🔴 关键新增：GROUP BY 子句中添加新字段
            "ORDER BY totalSales DESC LIMIT 10")
    List<PopularDish> getPopularDishes();
}
