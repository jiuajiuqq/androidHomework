package com.example.Android_bigWork.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.Android_bigWork.Entity.Person;

import java.util.List;

@Dao //Dao的声明
public interface PersonDao {
    @Query("SELECT * FROM person_table")
    List<Person> getAll();

    @Insert
    void insert(Person person);

    @Delete
    void delete(Person person);

    /**
     * 登录检测（使用用户名和密码的组合）
     *
     * @param username 用户名 password 密码
     * @return Query
     * @Author Anduin9527
     * @date 2022/10/12 9:14
     * @commit
     */
    @Query("SELECT * FROM person_table WHERE username = :username and password = :password ")
    Person checkLogin(String username, String password);

    /**
     * 登录检测（使用电话号码和密码的组合）
     *
     * @param phoneNumber 电话号码 password 密码
     * @return
     * @Author Anduin9527
     * @date 2022/10/12 9:15
     * @commit
     */
    @Query("SELECT * FROM person_table WHERE phoneNumber = :phoneNumber and password = :password ")
    Person checkLoginByPhoneNumber(long phoneNumber, String password);

    /**
     * 检测用户名是否存在
     *
     * @param username 用户名
     * @return Query
     * @Author Anduin9527
     * @date 2022/10/12 9:14
     * @commit
     */
    @Query("SELECT * FROM person_table WHERE username = :username")
    Person checkUsername(String username);

    /**
     * 检测电话号码是否存在
     *
     * @param phoneNumber 电话号码
     * @return Query
     * @Author Anduin9527
     * @date 2022/10/12 9:14
     * @commit
     */
    @Query("SELECT * FROM person_table WHERE phoneNumber = :phoneNumber")
    Person checkPhoneNumber(long phoneNumber);

    /**
     * 修改密码
     *
     * @param newPassword 新密码 oldPassword 旧密码 username 用户名
     * @return Update
     * @Author Anduin9527
     * @date 2022/10/12 9:14
     * @commit
     */
    @Query("UPDATE person_table SET password = :newPassword WHERE password  = :oldPassword and username = :username")
    void changePassword(String oldPassword, String newPassword, String username);

    //查询用户

    /**
     * 查询用户
     *
     * @param username
     * @return
     * @Author Anduin9527
     * @date 2022/10/12 13:34
     * @commit
     */
    @Query("SELECT * FROM person_table WHERE username = :username")
    Person queryPerson(String username);

    //查询用户的支付密码

    @Query("SELECT payPassword FROM person_table WHERE username = :username")
    int queryPayPassword(String username);

    @Update
    void update(Person user);

    /**
     * 根据用户名查询用户（用于登录或检查用户名重复）
     *
     * @param username 用户名
     * @return AppUser
     */
    @Query("SELECT * FROM person_table WHERE username = :username LIMIT 1")
    Person getUserByUsername(String username);

    /**
     * 根据用户ID查询用户
     *
     * @param userId 用户ID
     * @return AppUser
     */
    @Query("SELECT * FROM person_table WHERE UID = :userId LIMIT 1")
    Person getUserByUserId(int userId);

    /**
     * 获取所有用户列表 (管理端功能)
     *
     * @return List<AppUser>
     */
    @Query("SELECT * FROM person_table ORDER BY role DESC, username ASC")
    List<Person> getAllUsers();
}
