// DeepSeekService.java
package com.example.myapplication.network;

import com.example.myapplication.model.DeepSeekRequest;
import com.example.myapplication.model.DeepSeekResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface DeepSeekService {
    String BASE_URL = "https://api.deepseek.com/v1/";

    /**
     * 调用 DeepSeek API 进行聊天/文本分析
     */
    @POST("chat/completions")
    Call<DeepSeekResponse> getNutritionAnalysis(@Body DeepSeekRequest request);
}