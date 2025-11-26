package com.example.myapplication.Adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Entity.Person;
import com.example.myapplication.R;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final List<Person> userList;
    private final OnUserActionListener listener;

    // 定义回调接口
    public interface OnUserActionListener {
        void onToggleDisable(Person user);
        void onDelete(Person user);
        // void onChangeRole(Person user); // 如果需要直接在列表中提供修改角色按钮
    }

    public UserAdapter(List<Person> userList, OnUserActionListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        Person user = userList.get(position);

        holder.tvUsername.setText(user.username);
        holder.tvRole.setText("角色: " + user.role); // 假设 role 是 "admin", "student" 等字符串

        // 1. 禁用/启用按钮逻辑
        boolean isDisabled = user.isDisabled; // 假设 Person 中有此字段
        holder.btnToggleDisable.setText(isDisabled ? "启用" : "禁用");
        // 禁用按钮的背景颜色：已禁用为绿色 (启用操作)，未禁用为黄色/橙色 (禁用操作)
        holder.btnToggleDisable.setBackgroundTintList(
                ColorStateList.valueOf(isDisabled ? Color.parseColor("#4CAF50") : Color.parseColor("#FF9800"))
        );

        holder.btnToggleDisable.setOnClickListener(v -> listener.onToggleDisable(user));

        // 2. 删除按钮逻辑
        holder.btnDeleteUser.setOnClickListener(v -> listener.onDelete(user));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        final TextView tvUsername, tvRole;
        final Button btnToggleDisable, btnDeleteUser;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvRole = itemView.findViewById(R.id.tv_user_role);
            btnToggleDisable = itemView.findViewById(R.id.btn_toggle_disable);
            btnDeleteUser = itemView.findViewById(R.id.btn_delete_user);
        }
    }
}