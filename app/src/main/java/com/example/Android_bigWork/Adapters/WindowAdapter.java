package com.example.Android_bigWork.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Android_bigWork.Entity.Windows; // 假设 Window 实体
import com.example.Android_bigWork.R;

import java.util.List;

public class WindowAdapter extends RecyclerView.Adapter<WindowAdapter.WindowViewHolder> {

    private final List<Windows> windowList;
    private final OnWindowClickListener clickListener;

    /**
     * 定义点击回调接口
     */
    public interface OnWindowClickListener {
        void onWindowClick(Windows window);
    }

    public WindowAdapter(List<Windows> windowList, OnWindowClickListener clickListener) {
        this.windowList = windowList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public WindowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_window_config, parent, false);
        return new WindowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WindowViewHolder holder, int position) {
        Windows window = windowList.get(position);

        holder.tvWindowName.setText(window.name);

        // 假设 window.canteenName 或您能通过其他方式获取食堂名称
        holder.tvWindowCanteen.setText("所属食堂: " + window.canteenId);

        holder.tvWindowStatus.setText(window.status);

        // 根据状态设置颜色
        int colorResId;
        if ("营业中".equals(window.status)) {
            colorResId = R.color.green_500;
        } else {
            colorResId = R.color.red_500;
        }

        holder.tvWindowStatus.setTextColor(
                ContextCompat.getColor(holder.itemView.getContext(), colorResId)
        );

        // 设置点击监听器
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onWindowClick(window);
            }
        });
    }

    @Override
    public int getItemCount() {
        return windowList.size();
    }

    /**
     * 更新数据列表
     */
    public void updateData(List<Windows> newWindowList) {
        windowList.clear();
        windowList.addAll(newWindowList);
        notifyDataSetChanged();
    }

    static class WindowViewHolder extends RecyclerView.ViewHolder {
        TextView tvWindowName;
        TextView tvWindowCanteen;
        TextView tvWindowStatus;

        public WindowViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWindowName = itemView.findViewById(R.id.tv_window_name);
            tvWindowCanteen = itemView.findViewById(R.id.tv_window_canteen);
            tvWindowStatus = itemView.findViewById(R.id.tv_window_status);
        }
    }
}