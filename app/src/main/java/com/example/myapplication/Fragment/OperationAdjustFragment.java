package com.example.myapplication.Fragment;

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

import com.example.myapplication.Adapter.AdjustAdapter;
import com.example.myapplication.DataBase.DishDao;
import com.example.myapplication.DataBase.DishDatabase;
import com.example.myapplication.Entity.Dish;
import com.example.myapplication.R;

import java.util.List;

// 实现简化的 Adapter 接口
public class OperationAdjustFragment extends Fragment implements AdjustAdapter.AdjustmentSaveListener {

    private RecyclerView recyclerView;
    private DishDao dishDao;

    // 由于不再使用 Stock，可以移除 todayDate 和 timeSlot 相关的字段

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 假设布局文件名为 fragment_operation_adjust
        View view = inflater.inflate(R.layout.fragment_operation_adjust, container, false);

        recyclerView = view.findViewById(R.id.rv_dish_adjust_list);

        // 实例化 DAO
        dishDao = DishDatabase.getDatabase(getContext()).dishDao();
        // 移除 StockDao 实例化

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadDishData(); // 方法名改为 loadDishData

        return view;
    }

    /**
     * 加载菜品列表 (后台线程)
     */
    private void loadDishData() {
        new Thread(() -> {
            // 使用 DishDao 获取所有菜品
            List<Dish> allDishes = dishDao.getAllDishes();

            // 切换回主线程更新 UI
            new Handler(Looper.getMainLooper()).post(() -> {
                if (isAdded()) {
                    // 直接使用 Dish 列表实例化 Adapter，并传入 this 作为回调监听器
                    recyclerView.setAdapter(new AdjustAdapter(allDishes, this));
                    Toast.makeText(getContext(), "加载了 " + allDishes.size() + " 个菜品进行调整", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    /**
     * 处理价格或余量调整保存操作（在后台线程执行数据库更新）
     */
    @Override // 实现 Adapter 的接口方法
    public void onSave(Dish dish, double newPrice, int newRemainingQuantity) {
        // 数据库操作必须在后台线程执行
        new Thread(() -> {
            try {
                // 1. 更新 Dish 对象的字段
                dish.price = newPrice;
                dish.remainingStock = newRemainingQuantity;

                // 2. 执行数据库更新
                dishDao.update(dish);

                // 切换回主线程显示成功信息
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "已保存 " + dish.name + " 的调整", Toast.LENGTH_SHORT).show();
                        // 由于更新的是 Dish 对象的引用，理论上列表会立即刷新（如果 RecyclerView 观察到对象变化）。
                        // 为保险起见，可以重新加载数据以确保列表数据是最新的。
                        loadDishData();
                    }
                });

            } catch (Exception e) {
                Log.e("AdjustFragment", "保存调整失败", e);
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "保存失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).start();
    }
}