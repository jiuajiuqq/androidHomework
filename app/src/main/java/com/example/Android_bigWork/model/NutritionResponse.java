// NutritionResponse.java
package com.example.Android_bigWork.model;

public class NutritionResponse {
    // 分析是否成功
    private boolean success;
    // AI生成的营养分析文本和建议
    private String analysisResult;
    // 可能返回的错误信息
    private String errorMessage;

    public boolean isSuccess() {
        return success;
    }

    public String getAnalysisResult() {
        return analysisResult;
    }

    // 省略 Getter/Setter...
}