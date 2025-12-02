package com.example.myapplication.Entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

// 对应表名 AppUser
@Entity(tableName = "app_user_table")
public class Person implements Serializable {
    // 用户唯一ID（主键，自增）
    @PrimaryKey(autoGenerate = true)
    public int UID;

    // 用户名/学号
    public String username;
    // 密码哈希（实际应用中应存储哈希值，而不是明文密码）
    public String password;

    public long phoneNumber;

    public int gender;

    public int payPassword;

    public static final int GENDER_MALE = 0;
    public static final int GENDER_FEMALE = 1;
    public static final long serialVersionUID = 1L;//这是序列化的版本号
    // 用户角色（如：学生、管理员）[cite: 6]
    public String role;
    // 注册日期
    public long registrationDate; // 使用 long 存储时间戳

    public boolean isDisabled; // 【新增字段】用户禁用状态，默认为 false
    // 角色常量
    public static final String ROLE_STUDENT = "student";
    public static final String ROLE_ADMIN = "admin";

    public Person(String username, String password, String role, long registrationDate, long phoneNumber, int gender, int payPassword) {
        this.username = username;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.payPassword = payPassword;
        this.role = role;
        this.registrationDate = registrationDate;
    }

    @Override
    public String toString() {
        return "PersonEntity{" +
                "UID=" + UID +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", phoneNumber=" + phoneNumber +
                ", gender=" + gender +
                ", payPassword=" + payPassword +
                '}';
    }

    // 省略 getter/setter 和 toString() 方法...
}
