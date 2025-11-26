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

import com.example.myapplication.Entity.Feedback;
import com.example.myapplication.R;

import java.util.List;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder> {

    private final List<Feedback> feedbackList;
    private final OnFeedbackActionListener listener;

    public interface OnFeedbackActionListener {
        void onToggleProcessed(Feedback feedback);
        // 可选：void onViewDetails(Feedback feedback);
    }

    public FeedbackAdapter(List<Feedback> feedbackList, OnFeedbackActionListener listener) {
        this.feedbackList = feedbackList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feedback, parent, false);
        return new FeedbackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackViewHolder holder, int position) {
        Feedback feedback = feedbackList.get(position);

        holder.tvTitle.setText(feedback.title);
        holder.tvContent.setText(feedback.content);
        holder.tvDate.setText("提交于: " + feedback.submitDate);

        // 根据处理状态更新UI
        if (feedback.isProcessed) {
            holder.tvStatus.setText("已处理");
            holder.tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50"))); // 绿色
            holder.btnToggleProcessed.setText("恢复为未处理");
            holder.btnToggleProcessed.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#9E9E9E"))); // 灰色
        } else {
            holder.tvStatus.setText("未处理");
            holder.tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF9800"))); // 橙色/警告色
            holder.btnToggleProcessed.setText("标记为已处理");
            holder.btnToggleProcessed.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#C00000"))); // 红色/强调色
        }

        // 按钮点击事件
        holder.btnToggleProcessed.setOnClickListener(v -> listener.onToggleProcessed(feedback));
    }

    @Override
    public int getItemCount() {
        return feedbackList.size();
    }

    static class FeedbackViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTitle, tvContent, tvDate, tvStatus;
        final Button btnToggleProcessed;

        public FeedbackViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_feedback_title);
            tvContent = itemView.findViewById(R.id.tv_feedback_content);
            tvDate = itemView.findViewById(R.id.tv_feedback_date);
            tvStatus = itemView.findViewById(R.id.tv_feedback_status);
            btnToggleProcessed = itemView.findViewById(R.id.btn_toggle_processed);
        }
    }
}