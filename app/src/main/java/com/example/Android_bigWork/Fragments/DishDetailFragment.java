package com.example.Android_bigWork.Fragments;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Android_bigWork.Activity.MainActivity;
import com.example.Android_bigWork.Adapters.CommentAdapter;
import com.example.Android_bigWork.Database.CommentDao;
import com.example.Android_bigWork.Database.DishDatabase;
import com.example.Android_bigWork.Entity.Comment;
import com.example.Android_bigWork.Entity.Dish;
import com.example.Android_bigWork.R;
import com.example.Android_bigWork.Utils.StringUtil; // 假设你需要这个工具类

import java.util.List;
import java.util.Objects;
import java.util.ArrayList;

// 【重要！】你需要将这个 Fragment 转换为 BottomSheetDialogFragment 才能实现底部弹出效果
// 但为了保持简单和兼容性，我们暂时使用 DialogFragment

public class DishDetailFragment extends DialogFragment {

    private static final String TAG = "DishDetailFragment";
    private static final String ARG_DISH = "dish_object";
    private Dish currentDish;
    private CommentDao commentDao;
    private CommentAdapter commentAdapter;
    private List<Comment> commentsList;

    // ************* 【新增/移植的 UI 控件】 *************
    private TextView tvDishName, tvDishPrice, tvDishDescription, tvDishCount;
    private ImageView ivDishImg;
    private ImageButton btnAdd, btnSub;

    // ************* 【新增的评论 UI 控件】 *************
    private EditText etCommentInput;
    private Button btnSubmitComment;
    private RecyclerView rvComments;

    // ************* 【新增的用户数据】 *************
    private String currentUsername;
    private DishMenuFragment dishMenuFragment; // 用于调用 updateShoppingCarAccount()

    // 静态工厂方法
    public static DishDetailFragment newInstance(Dish dish) {
        DishDetailFragment fragment = new DishDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DISH, dish);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 【设置样式】：将弹窗设置为全屏或底部弹出（可选）
        // getTheme() 返回的是默认主题，这里只是展示，你可能需要在 styles.xml 中定义更合适的 Dialog 主题
        // setStyle(STYLE_NORMAL, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);

        if (getArguments() != null) {
            currentDish = (Dish) getArguments().getSerializable(ARG_DISH);
        }

        // 初始化 CommentDao
        commentDao = DishDatabase.getDatabase(requireContext()).getCommentDao();

        // 获取当前登录的用户名 (假设 MainActivity 有 public Person getUser() 方法)
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null && activity.getUser() != null) {
            currentUsername = activity.getUser().username;
        } else {
            currentUsername = "Guest";
        }

        // 获取 DishMenuFragment 实例，用于更新购物车总价
        // 【注意】：你需要确保 this.getTargetFragment() 返回的是 DishMenuFragment
        // 简单方法：通过 FragmentManager 查找宿主 Fragment (更复杂，这里简化处理)
        if (getTargetFragment() instanceof DishMenuFragment) {
            dishMenuFragment = (DishMenuFragment) getTargetFragment();
        }

        // 如果宿主是 Activity，直接通过 Activity 查找 Fragment（不推荐，但可能在你的项目中可行）
        if (activity != null) {
            // 这里为了简化，我们暂时使用 Activity 内部的更新方法，或者直接在 DishMenuFragment 中调用 updateShoppingCarAccount()
        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 使用我们修改后的布局文件
        return inflater.inflate(R.layout.dialog_dish_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ************* 【绑定 UI 控件】 *************
        // 菜品信息
        tvDishName = view.findViewById(R.id.tv_detail_dish_name);
        tvDishPrice = view.findViewById(R.id.tv_detail_dish_price);
        tvDishDescription = view.findViewById(R.id.dish_desctiption); // 原来的 desc 描述
        tvDishCount = view.findViewById(R.id.dish_count);
        ivDishImg = view.findViewById(R.id.dish_img);
        btnAdd = view.findViewById(R.id.dish_add);
        btnSub = view.findViewById(R.id.dish_sub);

        // 评论区
        etCommentInput = view.findViewById(R.id.et_comment_input);
        btnSubmitComment = view.findViewById(R.id.btn_submit_comment);
        rvComments = view.findViewById(R.id.rv_comments);

        // ************* 【填充菜品信息】 *************
        if (currentDish != null) {
            tvDishName.setText(currentDish.getName());
            tvDishPrice.setText(String.valueOf(currentDish.getPrice()));
            tvDishDescription.setText(currentDish.getDescription());
            tvDishCount.setText(String.valueOf(currentDish.getCount()));
            // 图片资源获取 (需要一个 resources 对象，这里简化为直接通过 ID 查找)
            // ❗ 注意：你需要实现一个方法来获取 resources.getIdentifier()
            // ivDishImg.setImageResource(resources.getIdentifier("dish_" + currentDish.getGID(), "drawable", requireContext().getPackageName()));
            // 【新增/修正代码】：加载菜品图片
            try {
                // 1. 获取 Resources 对象
                Resources resources = requireContext().getResources();
                // 2. 构造资源名 (例如: "dish_1", "dish_2")
                String resourceName = "dish_" + currentDish.getGID();
                // 3. 查找资源的 ID
                int resourceId = resources.getIdentifier(
                        resourceName,
                        "drawable",
                        requireContext().getPackageName() // 获取包名
                );

                // 4. 设置图片，如果找到 ID 且 ID 有效
                if (resourceId != 0) {
                    ivDishImg.setImageResource(resourceId);
                } else {
                    // 如果没找到图片，使用一个默认图标
                    ivDishImg.setImageResource(android.R.drawable.ic_dialog_alert); // 假设你有一个默认图标
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to load dish image: " + e.getMessage());
                // 捕获异常，防止应用崩溃
            }
        }

        // ************* 【点餐逻辑迁移】 *************
        // 复制原 showDishDetail 中加号点击事件的逻辑
        btnAdd.setOnClickListener(v -> {
            Log.d(TAG, "Detail: Add clicked");

            // --- ⚠️ 注意：原代码的口味定制逻辑已被删除 ---

            // 数据层上，将菜加入购物车 (你需要把原 Adapter 中的 addDishToShoppingCar 逻辑复制过来或调用)
            // ❗ 这里需要调用 DishMenuFragment 的方法，但我们还没添加，先假设可以调用
            // dishMenuFragment.addDishToShoppingCar(currentDish, 0, 0, "");

            currentDish.setCount(currentDish.getCount() + 1);
            if (dishMenuFragment != null) {
                dishMenuFragment.addDishToShoppingCar(currentDish, 0, 0, ""); // ❗ 口味选择用 0/0/"" 占位
                // 3. 调用回调通知刷新
                dishMenuFragment.onDishCountChanged();
            }

            tvDishCount.setText(String.valueOf(currentDish.getCount()));

            // 刷新购物车总价 (❗ 假设 DishMenuFragment 有这个方法)
            // dishMenuFragment.updateShoppingCarAccount();
            Fragment targetFragment = getTargetFragment();
            if (targetFragment instanceof DishMenuFragment.OnDishCountChangeListener) {
                ((DishMenuFragment.OnDishCountChangeListener) targetFragment).onDishCountChanged(currentDish);
            }

            // 简单处理：提示并关闭弹窗（实际应该刷新父 Fragment 的 Adapter）
            Toast.makeText(requireContext(), "已添加 " + currentDish.getName(), Toast.LENGTH_SHORT).show();
        });

        // 复制原 showDishDetail 中减号点击事件的逻辑
        btnSub.setOnClickListener(v -> {
            if (currentDish.getCount() > 0) {
                // 确保有 item_dish.xml 中的 removeSingleDishFromShoppingCar(dish) 方法可用
                // dishMenuFragment.removeSingleDishFromShoppingCar(currentDish);

                currentDish.setCount(currentDish.getCount() - 1);

                // 2. 【核心】：调用宿主 Fragment 的移除方法
                if (dishMenuFragment != null) {
                    dishMenuFragment.removeSingleDishFromShoppingCar(currentDish);
                    // 3. 调用回调通知刷新
                    dishMenuFragment.onDishCountChanged();
                }

                tvDishCount.setText(String.valueOf(currentDish.getCount()));
                // dishMenuFragment.updateShoppingCarAccount();
                Fragment targetFragment = getTargetFragment();
                if (targetFragment instanceof DishMenuFragment.OnDishCountChangeListener) {
                    ((DishMenuFragment.OnDishCountChangeListener) targetFragment).onDishCountChanged(currentDish);
                }
                // 简单处理：提示并关闭弹窗
                Toast.makeText(requireContext(), "已移除 " + currentDish.getName(), Toast.LENGTH_SHORT).show();
            }
        });


        // ************* 【评论功能初始化】 *************
        // 评论列表初始化
        rvComments.setLayoutManager(new LinearLayoutManager(requireContext()));
        loadComments(); // 加载已有评论

        // 提交按钮监听器
        btnSubmitComment.setOnClickListener(v -> submitComment());
    }

    // 【核心方法】：加载并显示评论
    private void loadComments() {
        // ... (保持不变)
        if (currentDish == null) return;

        // ❗ 注意：数据库查询必须在后台线程。这里只是为了学习简化。
        List<Comment> loadedComments = commentDao.getCommentsForDish(currentDish.getGID());

        // 初始化或更新 Adapter
        if (commentAdapter == null) {
            commentAdapter = new CommentAdapter(requireContext(), loadedComments);
            rvComments.setAdapter(commentAdapter);
        } else {
            commentAdapter.setCommentsList(loadedComments); // 假设 CommentAdapter 有更新数据的方法
        }
    }

    // 【核心方法】：提交评论
    private void submitComment() {
        String content = etCommentInput.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(requireContext(), "评论内容不能为空！", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. 创建新的 Comment 对象 (假设评分固定为 5.0，可自行添加 RatingBar 控件)
        Comment newComment = new Comment(
                currentDish.getGID(),
                currentUsername,
                content,
                5.0f, // 默认评分 5.0f
                System.currentTimeMillis() // 当前时间戳
        );

        // 2. 插入数据库 (❗ 同样需要后台线程，这里简化处理)
        // 建议使用线程池执行数据库操作
        new Thread(() -> {
            commentDao.insertComment(newComment);
            // 插入完成后回到主线程更新 UI
            requireActivity().runOnUiThread(() -> {
                etCommentInput.setText(""); // 清空输入框
                loadComments(); // 重新加载评论列表
                Toast.makeText(requireContext(), "评论发表成功！", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    @Override
    public void onStart() {
        super.onStart();
        // 确保 DialogFragment 存在
        if (getDialog() != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT; // 宽度全屏
            int height = ViewGroup.LayoutParams.WRAP_CONTENT; // 高度包裹内容

            // 设置 Dialog 的宽高
            getDialog().getWindow().setLayout(width, height);

            // 可以在这里设置底部弹出效果（可选）
            // getDialog().getWindow().setGravity(Gravity.BOTTOM);
        }
    }
}