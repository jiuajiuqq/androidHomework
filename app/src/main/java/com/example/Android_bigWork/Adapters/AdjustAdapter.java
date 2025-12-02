// AdjustAdapter.java (修改后的版本)
package com.example.Android_bigWork.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Android_bigWork.Entity.Dish;
import com.example.Android_bigWork.R;

import java.util.List;

public class AdjustAdapter extends RecyclerView.Adapter<AdjustAdapter.AdjustViewHolder> {

    // 数据源直接使用 Dish 列表
    private final List<Dish> dishList;
    private final AdjustmentSaveListener listener;

    // 简化回调接口
    public interface AdjustmentSaveListener {
        void onSave(Dish dish, double newPrice, int newRemainingQuantity);
    }

    public AdjustAdapter(List<Dish> dishList, AdjustmentSaveListener listener) {
        this.dishList = dishList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdjustViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 使用您之前定义的布局文件
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dish_adjustment, parent, false);
        return new AdjustViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdjustViewHolder holder, int position) {
        Dish dish = dishList.get(position);

        holder.tvDishName.setText(dish.name);
        // 假设您能从某个地方获取窗口信息，这里使用 windowId 占位
        holder.tvWindowInfo.setText("所属窗口ID: " + dish.windowId);

        // 显示 Dish 实体中的当前价格和余量
        holder.etPrice.setText(String.valueOf(dish.price));
        holder.etStock.setText(String.valueOf(dish.remainingStock));

        holder.btnSave.setOnClickListener(v -> {
            try {
                // 读取用户输入
                double newPrice = Double.parseDouble(holder.etPrice.getText().toString());
                int newStock = Integer.parseInt(holder.etStock.getText().toString());

                // 触发 Fragment 中的保存方法
                listener.onSave(dish, newPrice, newStock);

            } catch (NumberFormatException e) {
                Toast.makeText(holder.itemView.getContext(), "请输入有效的价格和余量数字", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return dishList.size();
    }

    static class AdjustViewHolder extends RecyclerView.ViewHolder {
        final TextView tvDishName, tvWindowInfo;
        final EditText etPrice, etStock;
        final Button btnSave;

        public AdjustViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDishName = itemView.findViewById(R.id.tv_dish_name);
            tvWindowInfo = itemView.findViewById(R.id.tv_window_info);
            etPrice = itemView.findViewById(R.id.et_adjust_price);
            etStock = itemView.findViewById(R.id.et_adjust_stock);
            btnSave = itemView.findViewById(R.id.btn_save_adjustment);
        }
    }
}