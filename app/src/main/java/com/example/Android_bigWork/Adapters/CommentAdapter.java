package com.example.Android_bigWork.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Android_bigWork.Entity.Comment;
import com.example.Android_bigWork.R;
import com.example.Android_bigWork.Utils.StringUtil; // 假设 StringUtil 有格式化时间的方法

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author YourName
 * @Type CommentAdapter
 * @Desc 评论列表适配器
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private Context context;
    private List<Comment> commentsList;

    public CommentAdapter(Context context, List<Comment> commentsList) {
        this.context = context;
        this.commentsList = commentsList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 加载评论列表项的布局文件
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentsList.get(position);

        // 绑定数据到 UI 控件
        holder.tvUsername.setText(comment.getUsername());
        holder.tvContent.setText(comment.getContent());
        // 【注意】: 格式化时间戳 (假设你的 StringUtil 或其他地方有这个功能)
        holder.tvTime.setText(formatTimestamp(comment.getTimestamp()));
        // 评分显示逻辑可以根据你的 item_comment.xml 进行添加
    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }

    // 更新数据列表（用于刷新评论）
    public void setCommentsList(List<Comment> newComments) {
        this.commentsList = newComments;
        notifyDataSetChanged();
    }

    // ViewHolder 内部类
    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername;
        TextView tvContent;
        TextView tvTime;
        // RatingBar ratingBar; // 如果 item_comment.xml 中有评分控件

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            // 绑定 item_comment.xml 中的 ID
            tvUsername = itemView.findViewById(R.id.tv_comment_username);
            tvContent = itemView.findViewById(R.id.tv_comment_content);
            tvTime = itemView.findViewById(R.id.tv_comment_time);
        }
    }

    // 【辅助方法】：格式化时间戳，保证时间可读
    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}