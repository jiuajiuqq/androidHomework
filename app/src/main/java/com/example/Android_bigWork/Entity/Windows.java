package com.example.Android_bigWork.Entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

// 对应表名 Window
@Entity(tableName = "window_table")
public class Windows implements Serializable {
    // 窗口唯一ID（主键，自增）
    @PrimaryKey(autoGenerate = true)
    public int windowId;

    // 所属食堂ID（外键）
    public int canteenId;
    // 窗口名称（如：川湘风味、面食档口）
    public String name;
    // 窗口特色或简介
    public String description;
    // 状态（如：营业中）
    public String status;

    public Windows(int canteenId, String name, String description, String status) {
        this.canteenId = canteenId;
        this.name = name;
        this.description = description;
        this.status = status;
    }

    // 省略 getter/setter 和 toString() 方法...
}
