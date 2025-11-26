package com.example.myapplication.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Entity.Dish; // 假设 Dish 实体
import com.example.myapplication.R;

import java.util.List;
import java.util.Locale;

public class DishAdapter extends RecyclerView.Adapter<DishAdapter.DishViewHolder> {

    private final List<Dish> dishList;
    private final OnDishClickListener clickListener;

    /**
     * 定义点击回调接口
     */
    public interface OnDishClickListener {
        void onDishClick(Dish dish);
    }

    public DishAdapter(List<Dish> dishList, OnDishClickListener clickListener) {
        this.dishList = dishList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public DishViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dish_config, parent, false);
        return new DishViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DishViewHolder holder, int position) {
        Dish dish = dishList.get(position);

        holder.tvDishName.setText(dish.name);

        // 菜品详情：所属窗口 | 类型
        String details = String.format(Locale.getDefault(), "窗口: %s | 类型: %s",
                dish.windowId, dish.category);
        holder.tvDishDetails.setText(details);

        // 价格格式化
        String priceText = String.format(Locale.getDefault(), "¥%.2f", dish.price);
        holder.tvDishPrice.setText(priceText);

        // 库存/状态信息
        holder.tvDishStock.setText("库存: " + dish.remainingStock);

        // 设置点击监听器
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onDishClick(dish);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dishList.size();
    }

    /**
     * 更新数据列表
     */
    public void updateData(List<Dish> newDishList) {
        dishList.clear();
        dishList.addAll(newDishList);
        notifyDataSetChanged();
    }

    static class DishViewHolder extends RecyclerView.ViewHolder {
        TextView tvDishName;
        TextView tvDishDetails;
        TextView tvDishPrice;
        TextView tvDishStock;

        public DishViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDishName = itemView.findViewById(R.id.tv_dish_name);
            tvDishDetails = itemView.findViewById(R.id.tv_dish_details);
            tvDishPrice = itemView.findViewById(R.id.tv_dish_price);
            tvDishStock = itemView.findViewById(R.id.tv_dish_stock);
        }
    }
}