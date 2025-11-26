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

import com.example.myapplication.DataBase.DishDao;
import com.example.myapplication.DataBase.DishDatabase;
import com.example.myapplication.Entity.Dish;
import com.example.myapplication.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OperationAdjustFragment extends Fragment {

    private RecyclerView recyclerView;
    private DishDao dishDao;

    // 用于操作 Stock 表的关键信息
    private final String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 假设布局文件名为 fragment_operation_adjust
        View view = inflater.inflate(R.layout.fragment_operation_adjust, container, false);

        recyclerView = view.findViewById(R.id.rv_dish_adjust_list);

        // 实例化 DAO
        dishDao = DishDatabase.getDatabase(getContext()).dishDao();
        // stockDao = ... // 假设 StockDao 的实例化方式

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadDishAndStockData();

        return view;
    }

    /**
     * 加载菜品列表，并获取或创建今日的库存记录
     */
    private void loadDishAndStockData() {
        List<Dish> allDishes = dishDao.getAllDish();

        // TODO: 创建一个 DishWithStockModel，包含菜品信息和当日库存信息
        // List<DishWithStock> dataList = new ArrayList<>();
        // for (Dish dish : allDishes) { /* 获取 Stock 数据并添加到 dataList */ }

        // TODO: 设置 AdjustAdapter，Adapter 中的 ViewHolder 应该包含 EditText 和监听器
        // recyclerView.setAdapter(new AdjustAdapter(dataList, this::handleAdjustmentSave));
        Toast.makeText(getContext(), "加载了 " + allDishes.size() + " 个菜品进行调整", Toast.LENGTH_SHORT).show();
    }

    /**
     * 处理价格或余量调整保存操作
     */
    public void handleAdjustmentSave(Dish dish, double newPrice, int newRemainingQuantity) {
        // 1. 更新 Dish 的基础价格
        dish.price = newPrice;
        dishDao.update(dish);

        // 2. 更新 Stock 的余量和价格
        // Stock stock = stockDao.getStockByDishAndTime(dish.dishId, todayDate, "午餐"); // 假设时间段为午餐
        // if (stock != null) {
        //     stock.price = newPrice;
        //     stock.remainingQuantity = newRemainingQuantity;
        //     stockDao.update(stock);
        // } else { /* 创建新的 Stock 记录 */ }

        Toast.makeText(getContext(), "已保存 " + dish.name + " 的调整", Toast.LENGTH_SHORT).show();
    }
}