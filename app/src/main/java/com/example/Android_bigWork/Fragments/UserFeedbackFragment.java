package com.example.Android_bigWork.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.example.Android_bigWork.Adapters.FeedbackAdapter;
import com.example.Android_bigWork.Adapters.UserAdapter;
import com.example.Android_bigWork.Database.FeedbackDao; // 导入 FeedbackDao
import com.example.Android_bigWork.Database.FeedbackDatabase; // 导入 FeedbackDatabase
import com.example.Android_bigWork.Database.PersonDao;
import com.example.Android_bigWork.Database.PersonDatabase;
import com.example.Android_bigWork.Entity.Feedback; // 导入 Feedback 实体
import com.example.Android_bigWork.Entity.Person;
import com.example.Android_bigWork.R;

import java.util.List;

// 实现 UserAdapter 和 FeedbackAdapter 的监听器接口
public class UserFeedbackFragment extends Fragment implements
        UserAdapter.OnUserActionListener,
        FeedbackAdapter.OnFeedbackActionListener { // 【新增接口】

    private TabLayout tabLayout;
    private RecyclerView recyclerView;

    private PersonDao personDao;
    private FeedbackDao feedbackDao; // 【新增 FeedbackDao】

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 假设布局文件名为 fragment_user_feedback
        View view = inflater.inflate(R.layout.fragment_user_feedback, container, false);

        tabLayout = view.findViewById(R.id.tab_layout_admin);
        recyclerView = view.findViewById(R.id.rv_admin_list);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 实例化 DAO
        personDao = PersonDatabase.getDatabase(getContext()).getPersonDao();
        feedbackDao = FeedbackDatabase.getDatabase(getContext()).feedbackDao(); // 【实例化 FeedbackDao】

        setupTabs();

        return view;
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("用户账号管理"));
        tabLayout.addTab(tabLayout.newTab().setText("投诉反馈处理"));

        loadUserManagementList(); // 默认加载用户管理

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    loadUserManagementList(); // Tab 0: 用户管理
                } else {
                    loadFeedbackList();       // Tab 1: 反馈处理
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                recyclerView.setAdapter(null);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                recyclerView.scrollToPosition(0);
            }
        });
    }

    // --- 用户管理逻辑 ---

    private void loadUserManagementList() {
        new Thread(() -> {
            try {
                List<Person> allUsers = personDao.getAllUsers();

                new Handler(Looper.getMainLooper()).post(() -> {
                    if (isAdded()) {
                        recyclerView.setAdapter(new UserAdapter(allUsers, this));
                        Toast.makeText(getContext(), "加载所有用户 (" + allUsers.size() + ")", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e("UserFeedbackFragment", "加载用户失败", e);
            }
        }).start();
    }

    @Override
    public void onToggleDisable(Person user) {
        if (user.role.equals(Person.ROLE_ADMIN)) { // 假设 Person.ROLE_ADMIN 是常量
            Toast.makeText(getContext(), "管理员账号不能禁用", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                user.isDisabled = !user.isDisabled;
                personDao.update(user);

                new Handler(Looper.getMainLooper()).post(() -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), user.username + (user.isDisabled ? " 已禁用" : " 已启用"), Toast.LENGTH_SHORT).show();
                        loadUserManagementList();
                    }
                });
            } catch (Exception e) {
                Log.e("UserFeedbackFragment", "禁用/启用失败", e);
            }
        }).start();
    }

    @Override
    public void onDelete(Person user) {
        if (user.role.equals(Person.ROLE_ADMIN)) {
            Toast.makeText(getContext(), "管理员账号无法删除", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                personDao.delete(user);

                new Handler(Looper.getMainLooper()).post(() -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "已删除用户: " + user.username, Toast.LENGTH_SHORT).show();
                        loadUserManagementList();
                    }
                });
            } catch (Exception e) {
                Log.e("UserFeedbackFragment", "删除用户失败", e);
            }
        }).start();
    }

    // --- 投诉反馈处理逻辑 ---

    /**
     * 加载投诉反馈列表和处理逻辑
     */
    private void loadFeedbackList() {
        new Thread(() -> {
            try {
                // 使用 FeedbackDao 获取所有反馈，并按处理状态排序
                List<Feedback> feedbackList = feedbackDao.getAllFeedback();

                new Handler(Looper.getMainLooper()).post(() -> {
                    if (isAdded()) {
                        // 设置 FeedbackAdapter，传入 this 作为监听器
                        recyclerView.setAdapter(new FeedbackAdapter(feedbackList, this));
                        Toast.makeText(getContext(), "加载投诉反馈 (" + feedbackList.size() + ")", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e("UserFeedbackFragment", "加载反馈列表失败", e);
            }
        }).start();
    }

    /**
     * 实现 FeedbackAdapter 的回调，切换反馈的处理状态
     */
    @Override
    public void onToggleProcessed(Feedback feedback) {
        new Thread(() -> {
            try {
                // 切换处理状态
                feedback.isProcessed = !feedback.isProcessed;
                feedbackDao.update(feedback);

                new Handler(Looper.getMainLooper()).post(() -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), feedback.title + (feedback.isProcessed ? " 已标记处理" : " 已恢复未处理"), Toast.LENGTH_SHORT).show();
                        loadFeedbackList(); // 刷新列表，重新排序
                    }
                });
            } catch (Exception e) {
                Log.e("UserFeedbackFragment", "处理反馈状态失败", e);
            }
        }).start();
    }

    // 角色切换方法 (如果需要)
    public void handleUserAction(Person person, String action) {
        if ("changeRole".equals(action)) {
            // ... 角色切换逻辑 (建议使用 Dialog 实现)
        }
    }
}