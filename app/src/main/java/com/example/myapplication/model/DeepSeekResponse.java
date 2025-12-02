// DeepSeekResponse.java
package com.example.myapplication.model;

import java.util.List;

public class DeepSeekResponse {
    private List<Choice> choices;

    public List<Choice> getChoices() {
        return choices;
    }

    // 获取分析结果的简便方法
    public String getAnalysisResult() {
        if (choices != null && !choices.isEmpty()) {
            Choice firstChoice = choices.get(0);
            if (firstChoice != null && firstChoice.getMessage() != null) {
                return firstChoice.getMessage().getContent();
            }
        }
        return "未能从 DeepSeek API 获取有效结果。";
    }

    // 内部类：选择
    public static class Choice {
        private Message message;

        public Message getMessage() {
            return message;
        }
    }

    // 内部类：消息（与请求体中的 Message 结构相同，用于解析返回内容）
    public static class Message {
        private String content;

        public String getContent() {
            return content;
        }
    }
}