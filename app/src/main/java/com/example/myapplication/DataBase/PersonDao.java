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
     * 删除用户
     */
    @Delete
    void delete(Person user);
    /**
     * 更新用户信息
     */
    @Query("SELECT * FROM app_user_table WHERE username = :username and password = :password ")
    Person checkLogin(String username, String password);

    @Query("SELECT * FROM app_user_table WHERE phoneNumber = :phoneNumber and password = :password ")
    Person checkLoginByPhoneNumber(long phoneNumber, String password);

    @Query("SELECT * FROM app_user_table WHERE username = :username")
    Person checkUsername(String username);

    @Query("SELECT * FROM app_user_table WHERE phoneNumber = :phoneNumber")
    Person checkPhoneNumber(long phoneNumber);

    @Query("UPDATE app_user_table SET password = :newPassword WHERE password  = :oldPassword and username = :username")
    void changePassword(String oldPassword, String newPassword, String username);

    @Query("SELECT * FROM app_user_table WHERE username = :username")
    Person queryPerson(String username);

    @Query("SELECT payPassword FROM app_user_table WHERE username = :username")
    int queryPayPassword(String username);
    @Update
    void update(Person user);

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
    @Query("SELECT * FROM app_user_table WHERE UID = :userId LIMIT 1")
    Person getUserByUserId(int userId);

    /**
     * 获取所有用户列表 (管理端功能)
     *
     * @return List<AppUser>
     */
    @Query("SELECT * FROM app_user_table ORDER BY role DESC, username ASC")
    List<Person> getAllUsers();
}
