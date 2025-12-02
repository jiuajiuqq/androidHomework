// NutritionRequest.java
package com.example.Android_bigWork.model;

public class NutritionRequest {
    // 文本输入 (如 "fish")
    private String dishName;
    // 图片数据，通常是 Base64 编码的字符串
    private String imageBase64;

    public NutritionRequest(String dishName, String imageBase64) {
        this.dishName = dishName;
        this.imageBase64 = imageBase64;
    }

    // 省略 Getter/Setter...
}