package com.example.Android_bigWork.Activity;

import android.os.Bundle;
import android.util.Log; // 导入 Log 类用于日志输出
import android.view.MenuItem;
import android.widget.Toast; // 导入 Toast 类用于用户反馈

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

// 导入 FloatingActionButton 所需的类
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.Android_bigWork.Fragments.MenuConfigFragment;
import com.example.Android_bigWork.Fragments.OperationAdjustFragment;
import com.example.Android_bigWork.Fragments.UserFeedbackFragment;
import com.example.Android_bigWork.R;

public class AdminMainActivity extends AppCompatActivity {

    // 定义日志标签，方便 Logcat 过滤
    private static final String TAG = "AdminMainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        // 1. 底部导航栏逻辑 (保留原有代码)
        BottomNavigationView bottomNavigationView = findViewById(R.id.admin_bottom_navigation);

        // 默认加载第一个 Fragment
        if (savedInstanceState == null) {
            loadFragment(new MenuConfigFragment());
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_menu_config) {
                    selectedFragment = new MenuConfigFragment();
                } else if (itemId == R.id.nav_operation_adjust) {
                    selectedFragment = new OperationAdjustFragment();
                } else if (itemId == R.id.nav_user_feedback) {
                    selectedFragment = new UserFeedbackFragment();
                }

                if (selectedFragment != null) {
                    loadFragment(selectedFragment);
                    return true;
                }
                return false;
            }
        });

        // 🌟 修改：悬浮按钮 (FAB) 的逻辑 🌟
        FloatingActionButton fab = findViewById(R.id.fab_selection_tasks);

        // 设置 FAB 的点击事件监听器
        fab.setOnClickListener(v -> {
            // 替换之前的 executeOptionalTasks()
            showTaskSelectionSheet();
        });
    }
    private void showTaskSelectionSheet() {
        TaskSelectionBottomSheet bottomSheet = new TaskSelectionBottomSheet();
        // 使用 getSupportFragmentManager() 来显示 BottomSheetDialogFragment
        bottomSheet.show(getSupportFragmentManager(), TaskSelectionBottomSheet.TAG);
    }

    /**
     * 替换 Fragment 到容器 (保留原有代码)
     */
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.admin_fragment_container, fragment)
                .commit();
    }

}