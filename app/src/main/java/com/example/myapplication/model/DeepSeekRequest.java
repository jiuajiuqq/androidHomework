// DeepSeekRequest.java
package com.example.myapplication.model;

import java.util.List;

public class DeepSeekRequest {
    private String model;
    private List<Message> messages;
    private float temperature = 0.7f; // 可选参数

    public DeepSeekRequest(String model, List<Message> messages) {
        this.model = model;
        this.messages = messages;
    }

    // 内部类用于表示对话消息
    public static class Message {
        private String role; // "user" 或 "system"
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}