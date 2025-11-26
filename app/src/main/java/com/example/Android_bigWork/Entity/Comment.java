package com.example.Android_bigWork.Entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * @author YourName
 * @Type Comment
 * @Desc 菜品评论实体类
 * @date 2025/11/26
 */
@Entity(tableName = "comment_table")
public class Comment implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;              // 评论ID
    private int dishId;          // 【外键】评论的菜品 GID (与 Dish.GID 对应)
    private String username;     // 评论人 (与 Person.username 对应)
    private String content;      // 评论内容
    private float rating;        // 评分 (例如：3.5, 4.0)
    private long timestamp;      // 评论时间 (毫秒级时间戳)

    // 构造函数 (用于 Room 插入数据)
    public Comment(int dishId, String username, String content, float rating, long timestamp) {
        this.dishId = dishId;
        this.username = username;
        this.content = content;
        this.rating = rating;
        this.timestamp = timestamp;
    }

    // Room 需要一个无参构造函数，尽管这里我们使用了有参构造函数，但最好保留一个
    @Ignore
    public Comment() {}

    // 必须有 Getter 和 Setter 方法 (Room 库需要)

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getDishId() { return dishId; }
    public void setDishId(int dishId) { this.dishId = dishId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
