package com.example.Android_bigWork.Activity;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;

import com.airbnb.lottie.LottieAnimationView;
import com.example.Android_bigWork.Database.DishDao;
import com.example.Android_bigWork.Database.DishDatabase;
import com.example.Android_bigWork.Database.PersonDao;
import com.example.Android_bigWork.Database.PersonDatabase;
import com.example.Android_bigWork.Entity.Dish;
import com.example.Android_bigWork.R;
import com.example.Android_bigWork.Utils.StringUtil;
import com.example.Android_bigWork.action.HandlerAction;

/**
 * @author Anduin9527
 * @Type LoadingActivity
 * @Desc 欢迎界面
 * @date 2022/10/10 20:09
 */
public class LoadingActivity extends AppCompatActivity
        implements HandlerAction {
    private static final String TAG = "LoadingActivity";
    private LottieAnimationView loadingAnimationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        //去掉标题栏
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        loadingAnimationView = findViewById(R.id.animationView);
        loadingAnimationView.setAnimation(R.raw.animation_loading);//设置动画文件
        loadingAnimationView.playAnimation();//开始播放
        loadingAnimationView.setRepeatCount(12);//设置重复次数
        //连接数据库dish
        //获取数据库
        DishDatabase dishDatabase = DishDatabase.getDatabase(this);
        DishDao dishDao = dishDatabase.getDishDao();
        PersonDatabase personDatabase = PersonDatabase.getDatabase(this);
        PersonDao personDao = personDatabase.getPersonDao();
        //检测数据库是否为空
        if (dishDao.getDishCount() == 0) {
            //初始化数据库
            initDishDatabase(dishDao);
        }
        //延迟2400ms跳转到登录界面
        postDelayed(() -> {
            Intent intent = new Intent(this, LoginActivity.class).
                    setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }, 2400);
        Log.d(TAG, "onCreate: " + StringUtil.getCurrentDateAndTime());
        Log.d(TAG, "onCreate: " + StringUtil.getCurrentTime());
    }

    private void initDishDatabase(DishDao dishDao) {
        //插入数据
        dishDao.insert(new Dish(1, getRString(R.string.dish_1), getRString(R.string.desc_1), Double.parseDouble(getRString(R.string.price_1)), getRString(R.string.cate_1), 1, true, false,0,null,false,0));
        dishDao.insert(new Dish(5, getRString(R.string.dish_5), getRString(R.string.desc_5), Double.parseDouble(getRString(R.string.price_5)), getRString(R.string.cate_1), 1, true, false,0,null,false,0));
        dishDao.insert(new Dish(12, getRString(R.string.dish_12), getRString(R.string.desc_12), Double.parseDouble(getRString(R.string.price_12)), getRString(R.string.cate_1), 1, true, false,0,null,false,0));
        dishDao.insert(new Dish(24, getRString(R.string.dish_24), getRString(R.string.desc_24), Double.parseDouble(getRString(R.string.price_24)), getRString(R.string.cate_1), 1, false, false,0,null,false,0));
        dishDao.insert(new Dish(27, getRString(R.string.dish_27), getRString(R.string.desc_27), Double.parseDouble(getRString(R.string.price_27)), getRString(R.string.cate_1), 1, true, false,0,null,false,0));
        dishDao.insert(new Dish(31, getRString(R.string.dish_31), getRString(R.string.desc_31), Double.parseDouble(getRString(R.string.price_31)), getRString(R.string.cate_1), 1, true, false,0,null,false,0));
        dishDao.insert(new Dish(35, getRString(R.string.dish_35), getRString(R.string.desc_35), Double.parseDouble(getRString(R.string.price_35)), getRString(R.string.cate_1), 1, true, false,0,null,false,0));
        dishDao.insert(new Dish(36, getRString(R.string.dish_36), getRString(R.string.desc_36), Double.parseDouble(getRString(R.string.price_36)), getRString(R.string.cate_1), 1, true, false,0,null,false,0));
        dishDao.insert(new Dish(43, getRString(R.string.dish_43), getRString(R.string.desc_43), Double.parseDouble(getRString(R.string.price_43)), getRString(R.string.cate_1), 1, true, false,0,null,false,0));
        dishDao.insert(new Dish(48, getRString(R.string.dish_48), getRString(R.string.desc_48), Double.parseDouble(getRString(R.string.price_48)), getRString(R.string.cate_1), 1, true, false,0,null,false,0));

    }

    private String getRString(@StringRes int id) {
        return getResources().getString(id);
    }

}