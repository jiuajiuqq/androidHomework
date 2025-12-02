package com.example.Android_bigWork.Entity;

// Room 联表查询 DTO 不需要 @Entity 注解，只需要包含要接收的字段
public class PopularDish {

    // 复制 Dish.java 中所有需要返回的字段
    // 字段名必须和 Dish.java 中的成员变量名一致，Room 才能自动填充
    public int GID;
    public String name;
    public String description;
    public double price;
    public String category;
    public int CID;
    public boolean customizable;
    public boolean spicy;
    public boolean sweet;
    public int windowId;
    // 菜品图片路径
    public String imageUrl;
    // 是否在售
    public boolean isAvailable;
    // 菜品余量/库存
    public int remainingStock;

    // 【新增】: 接收 SQL 语句中的 AS totalSales
    public int totalSales;

    // 【可选】: 添加一个 toString 或其他辅助方法
    @Override
    public String toString() {
        return "PopularDish{" + name + ", Sales=" + totalSales + '}';
    }
}