package com.example.Android_bigWork.network; // 建议新建一个 network 包

import com.example.Android_bigWork.model.NutritionRequest; // 稍后创建
import com.example.Android_bigWork.model.NutritionResponse; // 稍后创建

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface NutritionService {

    /**
     * 调用 AI 大模型进行营养分析
     * @param request 包含文本输入和/或图片数据的请求体
     * @return Retrofit Call 对象，异步返回 NutritionResponse
     */
    @POST("analyze/nutrition")
    Call<NutritionResponse> getNutritionAnalysis(@Body NutritionRequest request);
}