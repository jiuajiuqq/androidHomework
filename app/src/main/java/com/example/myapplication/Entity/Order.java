package com.example.myapplication.Entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

// 对应表名 Order
@Entity(tableName = "order_table")
public class Order implements Serializable {
    // 订单唯一ID（主键，自增）
    @PrimaryKey(autoGenerate = true)
    public int orderId;

    // 下单用户ID（外键）
    public int userId;
    // 下单时间（使用 long 存储时间戳）
    public long orderTime;
    // 订单总金额
    public double totalAmount;
    // 订单状态（如：待制作、已完成、已取消）
    public String status;

    // 状态常量
    public static final String STATUS_PENDING = "待制作";
    public static final String STATUS_COMPLETED = "已完成";
    public static final String STATUS_CANCELLED = "已取消";

    public Order(int userId, long orderTime, double totalAmount, String status) {
        this.userId = userId;
        this.orderTime = orderTime;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    // 省略 getter/setter 和 toString() 方法...
}
