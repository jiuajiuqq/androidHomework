package com.example.Android_bigWork.Entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "person_table")//实体类的声明
public class Person implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int UID;

    public String username;
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
    //序列化方式


}
