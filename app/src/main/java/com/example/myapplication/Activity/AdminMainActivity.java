package com.example.myapplication.Activity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.myapplication.Fragment.MenuConfigFragment;
import com.example.myapplication.Fragment.OperationAdjustFragment;
import com.example.myapplication.Fragment.UserFeedbackFragment;
import com.example.myapplication.R;

public class AdminMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main); // 沿用上一个activity_admin_main.xml布局

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
    }

    /**
     * 替换 Fragment 到容器
     */
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.admin_fragment_container, fragment)
                .commit();
    }
}