package com.example.myapplication.Fragment;

import android.content.res.ColorStateList; // 导入 ColorStateList
import android.graphics.Color; // 导入 Color
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.example.myapplication.Adapter.UserAdapter; // 【确保 UserAdapter 已创建】
import com.example.myapplication.DataBase.PersonDao;
import com.example.myapplication.DataBase.PersonDatabase;
import com.example.myapplication.Entity.Person;
import com.example.myapplication.R;

import java.util.List;

// 实现 UserAdapter 的监听器接口，用于处理用户操作
public class UserFeedbackFragment extends Fragment implements UserAdapter.OnUserActionListener {

    private TabLayout tabLayout;
    private RecyclerView recyclerView;

    private PersonDao personDao;
    // private CommentDao commentDao; // 如果存在评论管理

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 假设布局文件名为 fragment_user_feedback
        View view = inflater.inflate(R.layout.fragment_user_feedback, container, false);

        tabLayout = view.findViewById(R.id.tab_layout_admin);
        recyclerView = view.findViewById(R.id.rv_admin_list);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 实例化 DAO
        personDao = PersonDatabase.getDatabase(getContext()).personDao();
        // commentDao = ...

        setupTabs();

        return view;
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("用户账号管理"));
        tabLayout.addTab(tabLayout.newTab().setText("投诉反馈处理"));

        // 默认加载第一个 Tab
        loadUserManagementList();

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
                // 可选：在切换 Tab 时清空列表数据，避免视觉残留
                recyclerView.setAdapter(null);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // 可选：重复点击当前 Tab 时滚动到列表顶部
                recyclerView.scrollToPosition(0);
            }
        });
    }

    /**
     * 加载用户列表和管理逻辑
     */
    private void loadUserManagementList() {
        new Thread(() -> {
            try {
                List<Person> allUsers = personDao.getAllUsers();

                // 切换到主线程更新 UI
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (isAdded()) {
                        // 使用 UserAdapter，传入 this 作为监听器
                        recyclerView.setAdapter(new UserAdapter(allUsers, this));
                        Toast.makeText(getContext(), "加载所有用户 (" + allUsers.size() + ")", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e("UserFeedbackFragment", "加载用户失败: " + e.getMessage());
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "加载用户列表失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }

    // 【实现 UserAdapter.OnUserActionListener 接口方法】

    // 1. 禁用/启用用户
    @Override
    public void onToggleDisable(Person user) {
        // 确保管理员不能禁用自己（可选的安全检查）
        if (user.role.equals("admin") && user.isDisabled == false) {
            Toast.makeText(getContext(), "管理员账号无法在此操作中禁用", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                // 切换禁用状态
                user.isDisabled = !user.isDisabled;
                personDao.update(user);

                new Handler(Looper.getMainLooper()).post(() -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), user.username + (user.isDisabled ? " 已禁用" : " 已启用"), Toast.LENGTH_SHORT).show();
                        loadUserManagementList(); // 刷新列表
                    }
                });
            } catch (Exception e) {
                Log.e("UserFeedbackFragment", "禁用/启用失败", e);
            }
        }).start();
    }

    // 2. 删除用户
    @Override
    public void onDelete(Person user) {
        // 确保不能删除管理员账号（可选的安全检查）
        if (user.role.equals("admin")) {
            Toast.makeText(getContext(), "管理员账号无法删除", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                personDao.delete(user);

                new Handler(Looper.getMainLooper()).post(() -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "已删除用户: " + user.username, Toast.LENGTH_SHORT).show();
                        loadUserManagementList(); // 刷新列表
                    }
                });
            } catch (Exception e) {
                Log.e("UserFeedbackFragment", "删除用户失败", e);
            }
        }).start();
    }

    /**
     * 处理用户账号操作 (此方法已部分被 Adapter 回调取代，但保留角色切换逻辑)
     */
    public void handleUserAction(Person person, String action) {
        // 这里可以实现修改角色的逻辑，例如弹出一个 Dialog 来选择新角色
        if ("changeRole".equals(action)) {
            // 示例：简单地切换管理员和学生角色（需要一个 Dialog 来选择）
            String newRole = person.role.equals(Person.ROLE_ADMIN) ? "student" : "admin";
            person.role = newRole;

            new Thread(() -> {
                personDao.update(person);
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), person.username + " 角色已变更为 " + newRole, Toast.LENGTH_SHORT).show();
                        loadUserManagementList();
                    }
                });
            }).start();
        }
        // 删除和禁用操作已转移到 onToggleDisable 和 onDelete 中
    }

    /**
     * 加载投诉反馈列表和处理逻辑 (此处仅为占位符)
     */
    private void loadFeedbackList() {
        // TODO: 实际应用中，这里应该加载 FeedbackAdapter
        // List<Comment> feedbackList = commentDao.getUnprocessedComments();
        // recyclerView.setAdapter(new FeedbackAdapter(feedbackList, this));
        Toast.makeText(getContext(), "加载投诉反馈列表...", Toast.LENGTH_SHORT).show();
        recyclerView.setAdapter(null); // 清空 Adapter，防止显示用户数据
    }
}