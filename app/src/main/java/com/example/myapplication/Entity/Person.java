package com.example.myapplication.Entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

// 对应表名 AppUser
@Entity(tableName = "app_user_table")
public class Person implements Serializable {
    // 用户唯一ID（主键，自增）
    @PrimaryKey(autoGenerate = true)
    public int userId;

    // 用户名/学号
    public String username;
    // 密码哈希（实际应用中应存储哈希值，而不是明文密码）
    public String password;
    // 用户角色（如：学生、管理员）[cite: 6]
    public String role;
    // 注册日期
    public long registrationDate; // 使用 long 存储时间戳

    // 角色常量
    public static final String ROLE_STUDENT = "student";
    public static final String ROLE_ADMIN = "admin";

    public Person(String username, String password, String role, long registrationDate) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.registrationDate = registrationDate;
    }

    // 省略 getter/setter 和 toString() 方法...
}
