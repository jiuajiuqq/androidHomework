package com.example.myapplication.DataBase;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.Entity.Dish;

import java.util.List;

@Dao
public interface DishDao {
    // --- 基本 CRUD 操作 ---

    /**
     * 添加菜品 (管理端功能)
     */
    @Insert
    void insert(Dish dish);

    /**
     * 更新菜品信息 (管理端功能)
     */
    @Update
    void update(Dish dish);

    /**
     * 删除指定菜品
     */
    @Delete
    void delete(Dish dish);

    /**
     * 清除所有菜品 (谨慎使用)
     */
    @Query("DELETE FROM dish_table")
    void deleteAllDish();

    // --- 查询与统计 ---

    /**
     * 获取所有菜品，按ID升序排列
     */
    @Query("SELECT * FROM dish_table ORDER BY dishId ASC")
    List<Dish> getAllDish();

    /**
     * 根据菜品ID获取单个菜品信息
     */
    @Query("SELECT * FROM dish_table WHERE dishId = :dishId LIMIT 1")
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
    @Query("SELECT * FROM dish_table WHERE category = :category")
    List<Dish> getDishByCategory(String category);

    /**
     * 返回某个窗口下的所有菜品信息
     *
     * @param windowId 窗口ID
     * @return 该窗口下的所有菜品
     */
    @Query("SELECT * FROM dish_table WHERE windowId = :windowId AND isAvailable = 1")
    List<Dish> getAvailableDishByWindow(int windowId);

    /**
     * 返回菜品总数
     */
    @Query("SELECT COUNT(*) FROM dish_table")
    int getDishCount();

    @Query("SELECT * FROM dish_table")
    List<Dish> getAllDishes();

    @Query("SELECT * FROM dish_table WHERE name LIKE :query OR description LIKE :query OR category LIKE :query ORDER BY dishId ASC")
    List<Dish> searchDishes(String query);
}
