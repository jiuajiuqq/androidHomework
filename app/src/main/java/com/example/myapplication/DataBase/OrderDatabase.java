package com.example.myapplication.DataBase;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.myapplication.Entity.Order;

@Database(entities = {Order.class}, version = 1, exportSchema = false)
public abstract class OrderDatabase extends RoomDatabase {
    private static final String DB_NAME = "order_transaction.db";
    private static OrderDatabase INSTANCE;

    /**
     * 获取订单数据库实例 (单例模式)
     *
     * @param context 上下文环境
     * @return OrderDatabase
     */
    public static synchronized OrderDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            OrderDatabase.class,
                            DB_NAME)
                    .fallbackToDestructiveMigration() // 允许破坏性迁移
                    .allowMainThreadQueries()         // 允许在主线程进行查询 (开发时方便)
                    .build();
        }
        return INSTANCE;
    }

    public abstract OrderDao orderDao(); // 对应前面定义的 OrderDao 接口
}
