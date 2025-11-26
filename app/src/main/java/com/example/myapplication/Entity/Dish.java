package com.example.myapplication.Entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

// 对应表名 Dish
@Entity(tableName = "dish_table")
public class Dish implements Serializable {
    // 菜品唯一ID（主键，自增）
    @PrimaryKey(autoGenerate = true)
    public int dishId;

    // 所属窗口ID（外键，简化起见这里直接存储ID，实际可能需要建立关系）
    public int windowId;
    // 菜品名称
    public String name;
    // 价格
    public double price;
    // 菜品描述
    public String description;
    // 菜品分类（如：正餐、早餐、饮品）[cite: 7]
    public String category;
    // 菜品图片路径
    public String imageUrl;
    // 是否在售
    public boolean isAvailable;

    public Dish(int windowId, String name, double price, String description, String category, String imageUrl, boolean isAvailable) {
        this.windowId = windowId;
        this.name = name;
        this.price = price;
        this.description = description;
        this.category = category;
        this.imageUrl = imageUrl;
        this.isAvailable = isAvailable;
    }

    // 省略 getter/setter 和 toString() 方法...
}
