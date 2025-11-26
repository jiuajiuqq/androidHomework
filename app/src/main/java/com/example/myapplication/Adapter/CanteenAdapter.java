package com.example.myapplication.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Entity.Canteen;
import com.example.myapplication.R;

import java.util.List;

public class CanteenAdapter extends RecyclerView.Adapter<CanteenAdapter.CanteenViewHolder> {

    private final List<Canteen> canteenList;
    // 定义点击回调接口，用于将点击事件传回 Fragment
    private final OnCanteenClickListener clickListener;

    /**
     * 定义回调接口
     */
    public interface OnCanteenClickListener {
        void onCanteenClick(Canteen canteen);
    }

    public CanteenAdapter(List<Canteen> canteenList, OnCanteenClickListener clickListener) {
        this.canteenList = canteenList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public CanteenViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_canteen_config, parent, false);
        return new CanteenViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CanteenViewHolder holder, int position) {
        Canteen canteen = canteenList.get(position);
        holder.tvCanteenName.setText(canteen.name);

        // 组合食堂信息，例如：地点 | 营业时间
        String locationInfo = canteen.location + " | 营业时间: " + canteen.openTime;
        holder.tvCanteenLocation.setText(locationInfo);

        holder.tvCanteenStatus.setText(canteen.status);
        // 根据状态设置颜色（例如：营业中 - 绿色，休息中 - 红色）
        if ("营业中".equals(canteen.status)) {
            holder.tvCanteenStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.green_500));
        } else {
            holder.tvCanteenStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.red_500));
        }

        // 设置点击监听器，点击时触发 Fragment 的回调
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onCanteenClick(canteen);
            }
        });
    }

    @Override
    public int getItemCount() {
        return canteenList.size();
    }

    /**
     * 更新数据列表
     */
    public void updateData(List<Canteen> newCanteenList) {
        canteenList.clear();
        canteenList.addAll(newCanteenList);
        notifyDataSetChanged();
    }

    static class CanteenViewHolder extends RecyclerView.ViewHolder {
        TextView tvCanteenName;
        TextView tvCanteenLocation;
        TextView tvCanteenStatus;

        public CanteenViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCanteenName = itemView.findViewById(R.id.tv_canteen_name);
            tvCanteenLocation = itemView.findViewById(R.id.tv_canteen_location);
            tvCanteenStatus = itemView.findViewById(R.id.tv_canteen_status);
        }
    }
}