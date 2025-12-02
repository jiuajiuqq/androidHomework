package com.example.Android_bigWork.Activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
// 导入 FloatingActionButton 所需的类 (新增)
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.Android_bigWork.Entity.Person;
import com.example.Android_bigWork.Fragments.DishMenuFragment;
import com.example.Android_bigWork.Fragments.OrderFragment;
import com.example.Android_bigWork.Fragments.SettingFragment;
import com.example.Android_bigWork.R;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private BottomNavigationBar bottomNavigationBar;
    private ArrayList<Fragment> fragmentArrayList;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    private Person user;//从登录界面传来的用户信息

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. 获取并设置用户数据 (必须先执行)
        Intent initIntent = getIntent();
        if (initIntent.getExtras() != null) {
            Bundle bundle = initIntent.getExtras();
            // 🌟 关键调整：先从 Intent 获取 user 数据 🌟
            user = (Person) bundle.getSerializable("user");

            if (user == null) {
                // 如果用户数据为空，可能是 Intent 传递错误，应退出
                Toast.makeText(this, "用户数据加载失败，请重新登录。", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        } else {
            // 如果没有 Intent 数据，也应该退出或跳转回登录页
            Toast.makeText(this, "未检测到登录信息，请重新登录。", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 2. 初始化 Fragment ArrayList，此时 user 已经有值了
        initFragmentArrayList();

        // 3. 获取到 Fragment 的管理对象
        fragmentManager = getSupportFragmentManager();

        // 4. 初始化 BottomNavigationBar
        initBottomNavigationBar();

        // 5. 初始化 FragmentTransaction 并显示第一个 Fragment
        initFragmentTransaction();

        // 6. 隐藏标题栏
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // 🌟 新增：悬浮按钮 (FAB) 的逻辑 🌟
        FloatingActionButton fab = findViewById(R.id.fab_selection_tasks);

        // 设置 FAB 的点击事件监听器
        // 假设您的 TaskSelectionBottomSheet 已经包含在项目中
        fab.setOnClickListener(v -> {
            showTaskSelectionSheet();
        });
    }

    /**
     * 显示 AI 任务选择底部的弹窗
     */
    private void showTaskSelectionSheet() {
        // 确保 TaskSelectionBottomSheet 已经被正确定义和导入
        TaskSelectionBottomSheet bottomSheet = new TaskSelectionBottomSheet();
        // 使用 getSupportFragmentManager() 来显示 BottomSheetDialogFragment
        bottomSheet.show(getSupportFragmentManager(), TaskSelectionBottomSheet.TAG);
    }

    /**
     * 初始化页面、
     *
     * @return void
     * @Author Bubu
     * @date 2022/11/4 20:29
     * @commit
     */
    private void initFragmentTransaction() {
        //开启事务
        fragmentTransaction = fragmentManager.beginTransaction();
        for (int i = 0; i < fragmentArrayList.size(); i++) {
            fragmentTransaction.add(R.id.fragmentContainer, fragmentArrayList.get(i));
            fragmentTransaction.hide(fragmentArrayList.get(i));
        }
        fragmentTransaction.show(fragmentArrayList.get(0));
        // commit FragmentTransaction to apply changes
        fragmentTransaction.commit();
    }

    /**
     * 初始化fragment，并向其中传递user信息
     *
     * @param
     * @return
     * @Author Anduin9527
     * @date 2022/10/16 11:39
     * @commit
     */
    private void initFragmentArrayList() {

        fragmentArrayList = new ArrayList<>();
        Bundle bundle = new Bundle();
        bundle.putSerializable("user", user);

        DishMenuFragment dishMenuFragment = new DishMenuFragment();
        dishMenuFragment.setArguments(bundle);
        fragmentArrayList.add(dishMenuFragment);

        OrderFragment orderFragment = new OrderFragment();
        orderFragment.setArguments(bundle);
        fragmentArrayList.add(orderFragment);

        SettingFragment settingFragment = new SettingFragment();
        settingFragment.setArguments(bundle);
        fragmentArrayList.add(settingFragment);

    }

    /**
     * 初始化底部导航按钮
     *
     * @return void
     * @Author Bubu
     * @date 2022/10/4 23:10
     * @commit
     */
    private void initBottomNavigationBar() {
        bottomNavigationBar = (BottomNavigationBar) findViewById(R.id.bottomNavigationBar);
        bottomNavigationBar
                .addItem(new BottomNavigationItem(R.drawable.icon_home, R.string.menu))
                .addItem(new BottomNavigationItem(R.drawable.icon_order, R.string.orders))
                .addItem(new BottomNavigationItem(R.drawable.icon_setting, R.string.settings))
                .setFirstSelectedPosition(0)
                .initialise();

        // BottomNavigationBar的点击监听器
        bottomNavigationBar.setTabSelectedListener(new BottomNavigationBar.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position) {
                Log.d(TAG, "onTabSelected: " + position);
                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.show(fragmentArrayList.get(position));
                fragmentTransaction.commit();
            }

            @Override
            public void onTabUnselected(int position) {
                Log.d(TAG, "onTabUnselected: " + position);
                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.hide(fragmentArrayList.get(position));
                fragmentTransaction.commit();
            }

            @Override
            public void onTabReselected(int position) {
                Log.d(TAG, "onTabReselected: " + position);
            }
        });

        // 监听BottomNavigationBar的宽高
        bottomNavigationBar.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {

                int width = bottomNavigationBar.getMeasuredWidth();
                int height = bottomNavigationBar.getMeasuredHeight();
                Log.d(TAG, "onLayoutChange: BottomNavigationBar (width,height)=(" + width + "," + height + ")");
//                ((DishMenuFragment)fragmentArrayList.get(0)).setBottomNavigationBarHeight(height);

            }
        });
    }
    public Person getUser() {
        return user;
    }

}