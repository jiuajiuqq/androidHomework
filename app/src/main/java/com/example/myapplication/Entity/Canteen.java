package com.example.myapplication.Entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

// 对应表名 Canteen
@Entity(tableName = "canteen_table")
public class Canteen implements Serializable {
    // 食堂唯一ID（主键，自增）
    @PrimaryKey(autoGenerate = true)
    public int canteenId;

    // 食堂名称（如：学一食堂、学四食堂）
    public String name;
    // 食堂位置描述
    public String location;
    // 营业时间
    public String openTime;
    // 状态（如：营业中、休息）
    public String status;

    public Canteen(String name, String location, String openTime, String status) {
        this.name = name;
        this.location = location;
        this.openTime = openTime;
        this.status = status;
    }

    // 省略 getter/setter 和 toString() 方法...
}
