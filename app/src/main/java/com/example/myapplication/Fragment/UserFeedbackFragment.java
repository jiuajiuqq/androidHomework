package com.example.myapplication.Fragment;

import android.os.Bundle;
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
import com.example.myapplication.DataBase.PersonDao;
import com.example.myapplication.DataBase.PersonDatabase;
import com.example.myapplication.Entity.Person;
import com.example.myapplication.R;

import java.util.List;

public class UserFeedbackFragment extends Fragment {

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
        // commentDao = ... // 假设 CommentDao 实例化方式

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
                // 必须实现此方法，即使内容为空
                // 可以在这里清空当前列表或执行其他清理操作
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // 必须实现此方法，即使内容为空
                // 通常用于滚动到列表顶部等操作
            }
        });
    }

    /**
     * 加载用户列表和管理逻辑
     */
    private void loadUserManagementList() {
        // 假设 PersonDao 中有 getAllUsers() 方法
        List<Person> allUsers = personDao.getAllUsers();

        // TODO: 设置 UserAdapter (列表项点击可弹出 Dialog 进行操作)
        // recyclerView.setAdapter(new UserAdapter(allUsers, this::handleUserAction));
        Toast.makeText(getContext(), "加载所有用户 (" + allUsers.size() + ")", Toast.LENGTH_SHORT).show();
    }

    /**
     * 处理用户账号操作 (禁用、改角色、删除)
     */
    public void handleUserAction(Person person, String action) {
        if ("delete".equals(action)) {
            personDao.delete(person);
            Toast.makeText(getContext(), "已删除用户: " + person.username, Toast.LENGTH_SHORT).show();
        } else if ("changeRole".equals(action)) {
            person.role = person.role.equals(Person.ROLE_ADMIN) ? Person.ROLE_STUDENT : Person.ROLE_ADMIN;
            personDao.update(person);
            Toast.makeText(getContext(), person.username + " 角色已变更为 " + person.role, Toast.LENGTH_SHORT).show();
        }
        loadUserManagementList(); // 刷新列表
    }

    /**
     * 加载投诉反馈列表和处理逻辑
     */
    private void loadFeedbackList() {
        // List<Comment> feedbackList = commentDao.getUnprocessedComments(); // 假设从 CommentDao 获取数据

        // TODO: 设置 FeedbackAdapter (列表项点击可查看详情)
        Toast.makeText(getContext(), "加载投诉反馈列表...", Toast.LENGTH_SHORT).show();
    }
}