package com.example.myapplication.DataBase;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.Entity.Canteen;
import java.util.List;

@Dao
public interface CanteenDao {
    /**
     * 添加食堂 (管理端)
     */
    @Insert
    void insert(Canteen canteen);

    /**
     * 更新食堂信息 (管理端)
     */
    @Update
    void update(Canteen canteen);

    /**
     * 删除食堂 (管理端)
     */
    @Delete
    void delete(Canteen canteen);

    /**
     * 获取所有食堂信息 (用于用户端首页展示)
     *
     * @return List<Canteen>
     */
    @Query("SELECT * FROM canteen_table ORDER BY canteenId ASC")
    List<Canteen> getAllCanteens();

    /**
     * 根据ID获取特定食堂
     *
     * @param id 食堂ID
     * @return Canteen
     */
    @Query("SELECT * FROM canteen_table WHERE canteenId = :id LIMIT 1")
    Canteen getCanteenById(int id);
}
