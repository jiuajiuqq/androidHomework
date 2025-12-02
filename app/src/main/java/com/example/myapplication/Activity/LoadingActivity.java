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

    // 假设 gid, CID 都是 0，isAvailable 为 true, remainingStock 为 10
    final int DEFAULT_GID = 0;
    final int DEFAULT_CID = 0;
    final String DEFAULT_IMAGE_URL = "";
    final boolean DEFAULT_IS_AVAILABLE = true;
    final int DEFAULT_REMAINING_STOCK = 10;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        // TODO 【临时调试代码】：强制重置初始化标志
//        SharedPreferences prefs = getSharedPreferences("SystemPrefs", Context.MODE_PRIVATE);
//        prefs.edit().putBoolean("isInitDone", false).apply();

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
        DishDao dishDao = DishDatabase.getDatabase(this).getDishDao();


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
                // Dish(gid, name, description, price, category, CID, spicy, sweet, windowId, imageUrl, isAvailable, remainingStock)
                dishes.add(new Dish(DEFAULT_GID, "红烧刘高翔", "经典牛肉面，面条劲道", 15.00, "主食", DEFAULT_CID, false, false,
                        windowId, DEFAULT_IMAGE_URL, DEFAULT_IS_AVAILABLE, DEFAULT_REMAINING_STOCK));
                dishes.add(new Dish(DEFAULT_GID, "麻辣小面", "重庆风味，麻辣鲜香", 10.00, "主食", DEFAULT_CID, true, false,
                        windowId, DEFAULT_IMAGE_URL, DEFAULT_IS_AVAILABLE, DEFAULT_REMAINING_STOCK));
                dishes.add(new Dish(DEFAULT_GID, "酸菜肉丝面", "酸爽开胃", 13.00, "主食", DEFAULT_CID, false, false,
                        windowId, DEFAULT_IMAGE_URL, DEFAULT_IS_AVAILABLE, DEFAULT_REMAINING_STOCK));
                dishes.add(new Dish(DEFAULT_GID, "葱油拌面", "简单美味", 8.00, "主食", DEFAULT_CID, false, false,
                        windowId, DEFAULT_IMAGE_URL, DEFAULT_IS_AVAILABLE, DEFAULT_REMAINING_STOCK));
                break;
            case "自选快餐":
                dishes.add(new Dish(DEFAULT_GID, "鱼香肉丝饭", "甜酸微辣的经典套餐", 14.50, "正餐", DEFAULT_CID, true, true,
                        windowId, DEFAULT_IMAGE_URL, DEFAULT_IS_AVAILABLE, DEFAULT_REMAINING_STOCK));
                dishes.add(new Dish(DEFAULT_GID, "红烧狮子头", "大肉丸，软烂入味", 18.00, "正餐", DEFAULT_CID, false, false,
                        windowId, DEFAULT_IMAGE_URL, DEFAULT_IS_AVAILABLE, DEFAULT_REMAINING_STOCK));
                dishes.add(new Dish(DEFAULT_GID, "土豆丝套餐", "经济实惠", 11.00, "正餐", DEFAULT_CID, false, false,
                        windowId, DEFAULT_IMAGE_URL, DEFAULT_IS_AVAILABLE, DEFAULT_REMAINING_STOCK));
                break;
            case "风味小吃":
                dishes.add(new Dish(DEFAULT_GID, "煎饼果子", "北京特色早餐/小吃", 7.00, "小吃", DEFAULT_CID, false, false,
                        windowId, DEFAULT_IMAGE_URL, DEFAULT_IS_AVAILABLE, DEFAULT_REMAINING_STOCK));
                dishes.add(new Dish(DEFAULT_GID, "肉夹馍", "老潼关风味", 10.50, "小吃", DEFAULT_CID, false, false,
                        windowId, DEFAULT_IMAGE_URL, DEFAULT_IS_AVAILABLE, DEFAULT_REMAINING_STOCK));
                dishes.add(new Dish(DEFAULT_GID, "烤冷面", "东北街头小吃", 12.00, "小吃", DEFAULT_CID, false, true,
                        windowId, DEFAULT_IMAGE_URL, DEFAULT_IS_AVAILABLE, DEFAULT_REMAINING_STOCK));
                dishes.add(new Dish(DEFAULT_GID, "酸辣粉", "地道酸辣粉", 11.50, "小吃", DEFAULT_CID, true, false,
                        windowId, DEFAULT_IMAGE_URL, DEFAULT_IS_AVAILABLE, DEFAULT_REMAINING_STOCK));
                dishes.add(new Dish(DEFAULT_GID, "茶叶蛋", "香浓入味", 2.00, "小吃", DEFAULT_CID, false, false,
                        windowId, DEFAULT_IMAGE_URL, DEFAULT_IS_AVAILABLE, DEFAULT_REMAINING_STOCK));
                break;
            case "饮料甜点":
                dishes.add(new Dish(DEFAULT_GID, "原味奶茶", "经典台式奶茶", 9.00, "饮品", DEFAULT_CID, false, true,
                        windowId, DEFAULT_IMAGE_URL, DEFAULT_IS_AVAILABLE, DEFAULT_REMAINING_STOCK));
                dishes.add(new Dish(DEFAULT_GID, "双皮奶", "清凉甜品", 8.50, "甜点", DEFAULT_CID, false, true,
                        windowId, DEFAULT_IMAGE_URL, DEFAULT_IS_AVAILABLE, DEFAULT_REMAINING_STOCK));
                dishes.add(new Dish(DEFAULT_GID, "柠檬水", "解渴必备", 5.00, "饮品", DEFAULT_CID, false, false,
                        windowId, DEFAULT_IMAGE_URL, DEFAULT_IS_AVAILABLE, DEFAULT_REMAINING_STOCK));
                break;
            case "川湘风味":
                dishes.add(new Dish(DEFAULT_GID, "麻婆豆腐", "麻辣鲜香下饭菜", 13.00, "正餐", DEFAULT_CID, true, false,
                        windowId, DEFAULT_IMAGE_URL, DEFAULT_IS_AVAILABLE, DEFAULT_REMAINING_STOCK));
                dishes.add(new Dish(DEFAULT_GID, "毛血旺", "川渝经典大菜", 25.00, "正餐", DEFAULT_CID, true, false,
                        windowId, DEFAULT_IMAGE_URL, DEFAULT_IS_AVAILABLE, DEFAULT_REMAINING_STOCK));
                dishes.add(new Dish(DEFAULT_GID, "干锅花菜", "香辣可口", 16.00, "正餐", DEFAULT_CID, true, false,
                        windowId, DEFAULT_IMAGE_URL, DEFAULT_IS_AVAILABLE, DEFAULT_REMAINING_STOCK));
                dishes.add(new Dish(DEFAULT_GID, "辣椒炒肉", "湖南特色", 18.00, "正餐", DEFAULT_CID, true, false,
                        windowId, DEFAULT_IMAGE_URL, DEFAULT_IS_AVAILABLE, DEFAULT_REMAINING_STOCK));
                break;
            case "西式简餐":
                dishes.add(new Dish(DEFAULT_GID, "黑椒牛柳意面", "浓郁黑椒汁", 22.00, "西餐", DEFAULT_CID, false, false,
                        windowId, DEFAULT_IMAGE_URL, DEFAULT_IS_AVAILABLE, DEFAULT_REMAINING_STOCK));
                dishes.add(new Dish(DEFAULT_GID, "鸡肉沙拉", "健康轻食", 18.00, "西餐", DEFAULT_CID, false, false,
                        windowId, DEFAULT_IMAGE_URL, DEFAULT_IS_AVAILABLE, DEFAULT_REMAINING_STOCK));
                dishes.add(new Dish(DEFAULT_GID, "奶油蘑菇汤", "暖胃好汤", 9.00, "西餐", DEFAULT_CID, false, false,
                        windowId, DEFAULT_IMAGE_URL, DEFAULT_IS_AVAILABLE, DEFAULT_REMAINING_STOCK));
                break;
            case "清真窗口":
                dishes.add(new Dish(DEFAULT_GID, "羊肉泡馍", "西北风味", 20.00, "清真", DEFAULT_CID, false, false,
                        windowId, DEFAULT_IMAGE_URL, DEFAULT_IS_AVAILABLE, DEFAULT_REMAINING_STOCK));
                dishes.add(new Dish(DEFAULT_GID, "大盘鸡", "新疆特色（小份）", 30.00, "清真", DEFAULT_CID, true, false,
                        windowId, DEFAULT_IMAGE_URL, DEFAULT_IS_AVAILABLE, DEFAULT_REMAINING_STOCK));
                dishes.add(new Dish(DEFAULT_GID, "牛肉水饺", "皮薄馅大", 15.00, "清真", DEFAULT_CID, false, false,
                        windowId, DEFAULT_IMAGE_URL, DEFAULT_IS_AVAILABLE, DEFAULT_REMAINING_STOCK));
                dishes.add(new Dish(DEFAULT_GID, "葱爆羊肉", "家常清真菜", 24.00, "清真", DEFAULT_CID, false, false,
                        windowId, DEFAULT_IMAGE_URL, DEFAULT_IS_AVAILABLE, DEFAULT_REMAINING_STOCK));
                break;
            case "港式烧腊":
                dishes.add(new Dish(DEFAULT_GID, "蜜汁叉烧饭", "经典港式烧腊", 19.00, "烧腊", DEFAULT_CID, false, true,
                        windowId, DEFAULT_IMAGE_URL, DEFAULT_IS_AVAILABLE, DEFAULT_REMAINING_STOCK));
                dishes.add(new Dish(DEFAULT_GID, "深井烧鹅饭", "广式烧鹅", 28.00, "烧腊", DEFAULT_CID, false, false,
                        windowId, DEFAULT_IMAGE_URL, DEFAULT_IS_AVAILABLE, DEFAULT_REMAINING_STOCK));
                dishes.add(new Dish(DEFAULT_GID, "豉油鸡", "粤式名菜", 17.00, "烧腊", DEFAULT_CID, false, false,
                        windowId, DEFAULT_IMAGE_URL, DEFAULT_IS_AVAILABLE, DEFAULT_REMAINING_STOCK));
                break;
            default:
                // 默认菜品
                dishes.add(new Dish(DEFAULT_GID, "今日特价菜", "每日更换的特价菜", 10.00, "特价", DEFAULT_CID, false, false,
                        windowId, DEFAULT_IMAGE_URL, DEFAULT_IS_AVAILABLE, DEFAULT_REMAINING_STOCK));
                dishes.add(new Dish(DEFAULT_GID, "白米饭", "优质东北米", 1.00, "主食", DEFAULT_CID, false, false,
                        windowId, DEFAULT_IMAGE_URL, DEFAULT_IS_AVAILABLE, DEFAULT_REMAINING_STOCK));
                break;
        }
        return dishes;
    }
}