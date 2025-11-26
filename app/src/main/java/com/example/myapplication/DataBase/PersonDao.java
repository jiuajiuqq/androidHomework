package com.example.myapplication.DataBase;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.Entity.Person;
import java.util.List;

@Dao
public interface PersonDao {
    /**
     * 添加新用户 (注册)
     */
    @Insert
    void insert(Person user);

    /**
     * 更新用户信息
     */
    @Update
    void update(Person user);

    /**
     * 删除用户
     */
    @Delete
    void delete(Person user);

    /**
     * 根据用户名查询用户（用于登录或检查用户名重复）
     *
     * @param username 用户名
     * @return AppUser
     */
    @Query("SELECT * FROM app_user_table WHERE username = :username LIMIT 1")
    Person getUserByUsername(String username);

    /**
     * 根据用户ID查询用户
     *
     * @param userId 用户ID
     * @return AppUser
     */
    @Query("SELECT * FROM app_user_table WHERE userId = :userId LIMIT 1")
    Person getUserByUserId(int userId);

    /**
     * 获取所有用户列表 (管理端功能)
     *
     * @return List<AppUser>
     */
    @Query("SELECT * FROM app_user_table ORDER BY userId DESC")
    List<Person> getAllUsers();
}
