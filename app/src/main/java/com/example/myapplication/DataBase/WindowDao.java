package com.example.myapplication.DataBase;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.Entity.Windows;
import java.util.List;

@Dao
public interface WindowDao {
    /**
     * 添加窗口 (管理端)
     */
    @Insert
    void insert(Windows window);

    /**
     * 更新窗口信息 (管理端)
     */
    @Update
    void update(Windows window);

    /**
     * 删除窗口 (管理端)
     */
    @Delete
    void delete(Windows window);

    /**
     * 获取某一食堂下的所有窗口 (用户端浏览)
     *
     * @param canteenId 食堂ID
     * @return List<Window>
     */
    @Query("SELECT * FROM window_table WHERE canteenId = :canteenId ORDER BY windowId ASC")
    List<Windows> getWindowsByCanteenId(int canteenId);

    /**
     * 根据ID获取特定窗口
     *
     * @param windowId 窗口ID
     * @return Window
     */
    @Query("SELECT * FROM window_table WHERE windowId = :windowId LIMIT 1")
    Windows getWindowById(int windowId);
}
