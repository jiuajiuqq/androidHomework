package com.example.myapplication.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.DataBase.CanteenDao;
import com.example.myapplication.DataBase.CarteenDatabase;
import com.example.myapplication.DataBase.DishDatabase;
import com.example.myapplication.DataBase.PersonDao;
import com.example.myapplication.DataBase.DishDao;
import com.example.myapplication.DataBase.PersonDatabase; // 假设 UserDatabase 包含 PersonDao
import com.example.myapplication.DataBase.WindowDao;
import com.example.myapplication.DataBase.WindowDatabase;
import com.example.myapplication.Entity.Canteen;
import com.example.myapplication.Entity.Person;
import com.example.myapplication.Entity.Dish; // 引用 Dish 实体
import com.example.myapplication.Entity.Windows;
import com.example.myapplication.R;

import java.util.List;

public class LoadingActivity extends AppCompatActivity {

    private static final String PREF_NAME = "SystemPrefs";
    private static final String KEY_INIT_DONE = "isInitDone";
    private static final long SPLASH_DISPLAY_LENGTH = 2000; // 停留时间：2秒

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        // TODO 【临时调试代码】：强制重置初始化标志
        SharedPreferences prefs = getSharedPreferences("SystemPrefs", Context.MODE_PRIVATE);
        prefs.edit().putBoolean("isInitDone", false).apply();

        // 在后台线程执行初始化任务
        new Thread(new Runnable() {
            @Override
            public void run() {
                initializeSystem();

                // 确保在UI线程进行跳转
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // 初始化完成后跳转到登录界面
                        Intent mainIntent = new Intent(LoadingActivity.this, LoginActivity.class);
                        startActivity(mainIntent);
                        finish();
                    }
                }, SPLASH_DISPLAY_LENGTH);
            }
        }).start();
    }

    /**
     * 检查并执行系统初始化任务（管理员账户、菜单数据）
     */
    private void initializeSystem() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean isInitDone = prefs.getBoolean(KEY_INIT_DONE, false);

        if (isInitDone) {
            Log.i("Init", "System already initialized. Skipping setup.");
            return;
        }

        Log.i("Init", "System initialization started...");

        // 实例化所有需要的 DAO
        PersonDao personDao = PersonDatabase.getDatabase(this).getPersonDao();
        CanteenDao canteenDao = CarteenDatabase.getDatabase(this).canteenDao();
        WindowDao windowDao = WindowDatabase.getDatabase(this).windowDao();
        DishDao dishDao = DishDatabase.getDatabase(this).dishDao();


        // --- 任务 A: 初始化默认管理员账户 ---
        if (personDao.getUserByUsername("admin") == null) {
            try {
                Person admin = new Person(
                        "admin",
                        "123",
                        Person.ROLE_ADMIN,
                        System.currentTimeMillis(),
                        110,
                        Person.GENDER_MALE,
                        123
                );
                personDao.insert(admin);
                Log.d("Init", "Default Admin account created.");
            } catch (Exception e) {
                Log.e("Init", "Error creating Admin account: " + e.getMessage());
            }
        } else {
            Log.d("Init", "Admin account already exists.");
        }


        // --- 任务 B: 初始化食堂、窗口和菜品数据 ---

        // 1. 创建两个食堂
        Canteen c1 = new Canteen("学一食堂", "一公寓附近", "6:30-20:00", "营业中");
        Canteen c2 = new Canteen("新主楼食堂", "新主楼B座一层", "7:00-21:00", "营业中");

        canteenDao.insert(c1);
        canteenDao.insert(c2);
        Log.d("Init", "Two Canteens created.");

        // 由于 Room 插入时会返回 rowId，但 DAO 接口目前是 void，
        // 我们需要重新查询以获取它们自增的 ID (canteenId)，
        // 实际开发中，建议 DAO insert 方法返回 long (rowId)
        List<Canteen> canteens = canteenDao.getAllCanteens();
        int canteen1Id = canteens.get(0).canteenId;
        int canteen2Id = canteens.get(1).canteenId;

        // 2. 为每个食堂创建 4 个窗口

        // --- 学一食堂 (C1) 的窗口 ---
        String[] c1WindowNames = {"特色面食", "自选快餐", "风味小吃", "饮料甜点"};
        for (String name : c1WindowNames) {
            Windows w = new Windows(canteen1Id, name, name + "窗口，口味偏大众", "营业中");
            windowDao.insert(w);
        }

        // --- 新主楼食堂 (C2) 的窗口 ---
        String[] c2WindowNames = {"川湘风味", "西式简餐", "清真窗口", "港式烧腊"};
        for (String name : c2WindowNames) {
            Windows w = new Windows(canteen2Id, name, name + "窗口，口味独特", "营业中");
            windowDao.insert(w);
        }
        Log.d("Init", "Eight Windows created.");

        // 3. 为每个窗口添加 3-5 个菜品

        // 获取所有窗口的 ID
        List<Windows> allWindows = windowDao.getWindowsByCanteenId(canteen1Id);
        allWindows.addAll(windowDao.getWindowsByCanteenId(canteen2Id));

        int dishCount = 0;
        for (Windows window : allWindows) {
            List<Dish> dishes = createSampleDishes(window.windowId, window.name);
            for (Dish dish : dishes) {
                dishDao.insert(dish);
                dishCount++;
            }
        }
        Log.d("Init", dishCount + " Dishes inserted into database.");

        // 4. 设置初始化完成标志
        prefs.edit().putBoolean(KEY_INIT_DONE, true).apply();
        Log.i("Init", "System initialization completed successfully.");
    }

    /**
     * 根据窗口名称创建示例菜品列表
     */
    private List<Dish> createSampleDishes(int windowId, String windowName) {
        List<Dish> dishes = new java.util.ArrayList<>();
        switch (windowName) {
            case "特色面食":
                dishes.add(new Dish(windowId, "红烧刘高翔", 0.15, "经典牛肉面，面条劲道", "主食", "", true, 10));
                dishes.add(new Dish(windowId, "麻辣小面", 10.00, "重庆风味，麻辣鲜香", "主食", "", true, 10));
                dishes.add(new Dish(windowId, "酸菜肉丝面", 13.00, "酸爽开胃", "主食", "", true, 10));
                dishes.add(new Dish(windowId, "葱油拌面", 8.00, "简单美味", "主食", "", true, 10));
                break;
            case "自选快餐":
                dishes.add(new Dish(windowId, "鱼香肉丝饭", 14.50, "甜酸微辣的经典套餐", "正餐", "", true, 10));
                dishes.add(new Dish(windowId, "红烧狮子头", 18.00, "大肉丸，软烂入味", "正餐", "", true, 10));
                dishes.add(new Dish(windowId, "土豆丝套餐", 11.00, "经济实惠", "正餐", "", true, 10));
                break;
            case "风味小吃":
                dishes.add(new Dish(windowId, "煎饼果子", 7.00, "北京特色早餐/小吃", "小吃", "", true, 10));
                dishes.add(new Dish(windowId, "肉夹馍", 10.50, "老潼关风味", "小吃", "", true, 10));
                dishes.add(new Dish(windowId, "烤冷面", 12.00, "东北街头小吃", "小吃", "", true, 10));
                dishes.add(new Dish(windowId, "酸辣粉", 11.50, "地道酸辣粉", "小吃", "", true, 10));
                dishes.add(new Dish(windowId, "茶叶蛋", 2.00, "香浓入味", "小吃", "", true, 10));
                break;
            case "饮料甜点":
                dishes.add(new Dish(windowId, "原味奶茶", 9.00, "经典台式奶茶", "饮品", "", true, 10));
                dishes.add(new Dish(windowId, "双皮奶", 8.50, "清凉甜品", "甜点", "", true, 10));
                dishes.add(new Dish(windowId, "柠檬水", 5.00, "解渴必备", "饮品", "", true, 10));
                break;
            case "川湘风味":
                dishes.add(new Dish(windowId, "麻婆豆腐", 13.00, "麻辣鲜香下饭菜", "正餐", "", true, 10));
                dishes.add(new Dish(windowId, "毛血旺", 25.00, "川渝经典大菜", "正餐", "", true, 10));
                dishes.add(new Dish(windowId, "干锅花菜", 16.00, "香辣可口", "正餐", "", true, 10));
                dishes.add(new Dish(windowId, "辣椒炒肉", 18.00, "湖南特色", "正餐", "", true, 10));
                break;
            case "西式简餐":
                dishes.add(new Dish(windowId, "黑椒牛柳意面", 22.00, "浓郁黑椒汁", "西餐", "", true, 10));
                dishes.add(new Dish(windowId, "鸡肉沙拉", 18.00, "健康轻食", "西餐", "", true, 10));
                dishes.add(new Dish(windowId, "奶油蘑菇汤", 9.00, "暖胃好汤", "西餐", "", true, 10));
                break;
            case "清真窗口":
                dishes.add(new Dish(windowId, "羊肉泡馍", 20.00, "西北风味", "清真", "", true, 10));
                dishes.add(new Dish(windowId, "大盘鸡", 30.00, "新疆特色（小份）", "清真", "", true, 10));
                dishes.add(new Dish(windowId, "牛肉水饺", 15.00, "皮薄馅大", "清真", "", true, 10));
                dishes.add(new Dish(windowId, "葱爆羊肉", 24.00, "家常清真菜", "清真", "", true, 10));
                break;
            case "港式烧腊":
                dishes.add(new Dish(windowId, "蜜汁叉烧饭", 19.00, "经典港式烧腊", "烧腊", "", true, 10));
                dishes.add(new Dish(windowId, "深井烧鹅饭", 28.00, "广式烧鹅", "烧腊", "", true, 10));
                dishes.add(new Dish(windowId, "豉油鸡", 17.00, "粤式名菜", "烧腊", "", true, 10));
                break;
            default:
                // 默认菜品
                dishes.add(new Dish(windowId, "今日特价菜", 10.00, "每日更换的特价菜", "特价", "", true,10));
                dishes.add(new Dish(windowId, "白米饭", 1.00, "优质东北米", "主食", "", true, 10));
                break;
        }
        return dishes;
    }
}