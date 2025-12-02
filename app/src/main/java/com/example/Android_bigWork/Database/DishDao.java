package com.example.Android_bigWork.Database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.Android_bigWork.Entity.Dish;
import com.example.Android_bigWork.Entity.PopularDish;
import java.util.ArrayList;
import java.util.List;

@Dao //Dao的声明
public interface DishDao {
    /**
     * 获取所有菜品
     *
     * @param
     * @return Query
     * @Author Anduin9527
     * @date 2022/10/12 9:14
     * @commit
     */
    @Query("SELECT * FROM dish_table ORDER BY CID")
    List<Dish> getAllDish();
    @Query("SELECT * FROM dish_table WHERE GID IN (1, 5, 10)") // 【新增】
    List<Dish> getRecommendedDishes();
    /**
     * 清除所有菜品
     *
     * @param
     * @return Query
     * @Author Anduin9527
     * @date 2022/10/12 9:14
     * @commit
     */
    @Query("DELETE FROM dish_table")
    void deleteAllDish();

    /**
     * 添加菜品
     *
     * @param dish
     * @return void
     * @Author Bubu
     * @date 2022/10/15 21:51
     * @commit
     */
    @Insert
    void insert(Dish dish);

    @Update
    void update(Dish dish);

    @Delete
    void delete(Dish Dish);

    /**
     * 返回菜品信息
     *
     * @param name 菜品名
     * @return Query
     * @Author Anduin9527
     * @date 2022/10/12 9:14
     * @commit
     */
    @Query("SELECT * FROM dish_table WHERE name = :name")
    Dish getDishByName(String name);

    /**
     * 返回该分类下所有菜品信息
     *
     * @param category 菜品分类
     * @return Query
     * @Author Anduin9527
     * @date 2022/10/12 9:47
     * @commit
     */
    @Query("SELECT * FROM dish_table WHERE category = :category")
    List<Dish> getDishByCategory(String category);

    @Query("SELECT COUNT(*) FROM dish_table")
    int getDishCount();
    // 【新增】: 确保 SQL 语句是正确的单行
    /**
     * 根据菜品ID获取单个菜品信息
     */
    @Query("SELECT * FROM dish_table WHERE GID = :dishId LIMIT 1")
    Dish getDishById(int dishId);

    /**
     * 根据菜品名模糊搜索菜品（支持用户端搜索）
     *
     * @param queryName 搜索关键词
     * @return 匹配的菜品列表
     */
    @Query("SELECT * FROM dish_table WHERE name LIKE '%' || :queryName || '%'")
    List<Dish> searchDishByName(String queryName);

    /**
     * 返回该分类下所有菜品信息
     *
     * @param category 菜品分类 (如：早餐、正餐)
     * @return 某分类下的所有菜品
     */

    /**
     * 返回某个窗口下的所有菜品信息
     *
     * @param windowId 窗口ID
     * @return 该窗口下的所有菜品
     */
    @Query("SELECT * FROM dish_table WHERE windowId = :windowId AND isAvailable = 1")
    List<Dish> getAvailableDishByWindow(int windowId);

    @Query("SELECT * FROM dish_table")
    List<Dish> getAllDishes();

    @Query("SELECT * FROM dish_table WHERE name LIKE :query OR description LIKE :query OR category LIKE :query ORDER BY GID ASC")
    List<Dish> searchDishes(String query);
}
