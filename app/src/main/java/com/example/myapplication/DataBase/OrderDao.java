package com.example.myapplication.DataBase;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.Entity.Order;
import java.util.List;

@Dao
public interface OrderDao {
    /**
     * 创建新订单
     *
     * @param order 订单实体
     * @return long 插入的订单ID
     */
    @Insert
    long insert(Order order); // 返回 long 以获取新插入的 orderId

    /**
     * 更新订单状态 (如：支付完成、取消订单)
     */
    @Update
    void update(Order order);

    /**
     * 根据用户ID获取所有历史订单
     *
     * @param userId 用户ID
     * @return List<Order>
     */
    @Query("SELECT * FROM order_table WHERE userId = :userId ORDER BY orderTime DESC")
    List<Order> getOrdersByUserId(int userId);

    /**
     * 根据状态获取订单 (管理端用于查看待处理或已完成订单)
     *
     * @param status 订单状态
     * @return List<Order>
     */
    @Query("SELECT * FROM order_table WHERE status = :status ORDER BY orderTime ASC")
    List<Order> getOrdersByStatus(String status);

    /**
     * 获取单个订单详情
     *
     * @param orderId 订单ID
     * @return Order
     */
    @Query("SELECT * FROM order_table WHERE orderId = :orderId LIMIT 1")
    Order getOrderById(int orderId);
}
