package com.example.myapplication.Entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

// 对应表名 Dish
@Entity(tableName = "dish_table")
public class Dish implements Serializable {
    // 菜品唯一ID（主键，自增）
    @PrimaryKey(autoGenerate = true)
    public int GID;
    // 菜品名称
    public String name;
    // 菜品描述
    public String description;
    // 价格
    public double price;
    // 菜品分类（如：正餐、早餐、饮品）
    public String category;

    // 私有字段需要 Getter/Setter
    private int CID;
    private boolean spicy;
    private boolean sweet;

    // 购物车中菜品的数量 (通常不需要存储到数据库，但这里没有 Room @Ignore 注解)
    private int count;

    // 所属窗口ID
    public int windowId;
    // 菜品图片路径
    public String imageUrl;
    // 是否在售
    public boolean isAvailable;
    // 菜品余量/库存
    public int remainingStock;

    // 🌟 修复 1：为 Room 和序列化添加必须的公开无参构造函数 🌟
    public Dish() {
        // 默认构造函数
    }

    // 主构造函数
    public Dish(int gid, String name, String description, double price, String category, int CID, boolean spicy, boolean sweet,
                int windowId, String imageUrl, boolean isAvailable, int remainingStock) {
        this.GID = gid;
        this.name = name;
        this.description = description;
        this.price = price; // 修正：只赋值一次
        this.category = category;
        this.CID = CID;
        this.spicy = spicy;
        this.sweet = sweet;
        this.count = 0; // 默认初始化为 0
        this.windowId = windowId;
        this.imageUrl = imageUrl;
        this.isAvailable = isAvailable;
        this.remainingStock = remainingStock;
    }

    // ==========================================================
    // 🌟 修复 2：为所有私有字段添加 Getter 和 Setter 🌟
    // ==========================================================

    // CID
    public int getCID() {
        return CID;
    }

    public void setCID(int CID) {
        this.CID = CID;
    }

    // Spicy (使用 is-getter)
    public boolean isSpicy() {
        return spicy;
    }

    public void setSpicy(boolean spicy) {
        this.spicy = spicy;
    }

    // Sweet (使用 is-getter)
    public boolean isSweet() {
        return sweet;
    }

    public void setSweet(boolean sweet) {
        this.sweet = sweet;
    }

    // Count
    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    // ==========================================================
    // 可选：为所有公有字段添加 Getter (推荐)
    // ==========================================================

    public int getGID() {
        return GID;
    }

    public void setGID(int GID) {
        this.GID = GID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    // ... 其他公有字段的 Getter/Setter 请按需补充 (如 getDescription, getCategory, etc.)
}