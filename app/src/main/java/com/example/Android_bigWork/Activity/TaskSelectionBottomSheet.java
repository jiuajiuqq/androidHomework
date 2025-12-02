package com.example.Android_bigWork.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.example.Android_bigWork.R;

// 引入 DeepSeek 和 Retrofit 相关的导入
import com.example.Android_bigWork.network.DeepSeekService;
import com.example.Android_bigWork.model.DeepSeekRequest;
import com.example.Android_bigWork.model.DeepSeekRequest.Message;
import com.example.Android_bigWork.model.DeepSeekResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class TaskSelectionBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "TaskSelectionSheet";
    // ⚠️ 快速测试：在此处硬编码 API Key
    private static final String DEEPSEEK_API_KEY = "sk-dfbb419e72094dd1ae3b7912644b3f3d";

    private DeepSeekService deepSeekService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_tasks, container, false);

        // 🌟 初始化 DeepSeek 客户端 🌟
        initializeDeepSeekService();

        Button btnTask1 = view.findViewById(R.id.btn_task_1);
        Button btnTask2 = view.findViewById(R.id.btn_task_2);
        Button btnTask3 = view.findViewById(R.id.btn_task_3);

        // 绑定点击事件到新的 executeTask 方法
        btnTask1.setOnClickListener(v -> executeTask(1));
        btnTask2.setOnClickListener(v -> executeTask(2));
        btnTask3.setOnClickListener(v -> executeTask(3));

        return view;
    }

    /**
     * 根据选择执行相应的任务逻辑
     * @param taskNumber 任务编号 (1, 2, 或 3)
     */
    private void executeTask(int taskNumber) {
        String taskMessage = "";

        switch (taskNumber) {
            case 1:
                // 🌟 选做任务一：启动 AI 营养师 Activity 🌟
                startAiNutritionistActivity();
                break;
            case 2:
                // 🌟 选做任务二：显示菜品输入对话框 🌟
                showDishInputDialog();
                // 注意：任务 2 的 dismiss() 在回调中调用
                return; // 阻止此处默认的 dismiss
            case 3:
                showKeywordInputDialog();
                return;
            default:
                // Do nothing
                break;
        }

        dismiss();
    }


    private void showKeywordInputDialog() {
        Context context = getContext();
        if (context == null) {
            dismiss();
            return;
        }

        final EditText input = new EditText(context);
        input.setHint("例如：温暖 / 酸甜 / 辣");

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("输入菜品联想关键词")
                .setMessage("请输入一个描述口味或感受的关键词，AI 将推荐相关菜品。")
                .setView(input);

        // 设置确认按钮
        builder.setPositiveButton("联想菜品", (dialog, which) -> {
            String keyword = input.getText().toString().trim();
            if (keyword.isEmpty()) {
                Toast.makeText(context, "关键词不能为空。", Toast.LENGTH_SHORT).show();
                showKeywordInputDialog();
            } else {
                // 捕获输入，并调用 API 进行菜品联想
                generateDishRecommendation(keyword);
            }
        });

        // 设置取消按钮
        builder.setNegativeButton("取消", (dialog, which) -> {
            dialog.cancel();
            dismiss(); // 关闭底部的 TaskSelectionBottomSheet
        });

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void generateDishRecommendation(String keyword) {
        if (deepSeekService == null || getContext() == null) {
            Toast.makeText(getContext(), "服务初始化失败，无法生成文案。", Toast.LENGTH_LONG).show();
            dismiss();
            return;
        }

        Toast.makeText(getContext(), "正在根据 [" + keyword + "] 联想菜品...", Toast.LENGTH_LONG).show();

        // 1. 构建 Prompt
        String prompt = String.format(
                "请根据关键词“%s”，联想并推荐3到5道符合该关键词的中式菜品。只列出菜品名称，每道菜名占一行，不要添加任何序号、描述或额外文字。",
                keyword
        );

        // 2. 构建 DeepSeek 请求体 (使用相同的 DeepSeekRequest/Message 类)
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("system", "You are an experienced Chinese chef, skilled at matching moods and flavors with dishes. Respond only with the dish names, one per line."));
        messages.add(new Message("user", prompt));

        DeepSeekRequest request = new DeepSeekRequest("deepseek-chat", messages);

        // 3. 发起异步网络请求 (使用相同的 deepSeekService)
        deepSeekService.getNutritionAnalysis(request).enqueue(new Callback<DeepSeekResponse>() {
            @Override
            public void onResponse(@NonNull Call<DeepSeekResponse> call, @NonNull Response<DeepSeekResponse> response) {
                Context context = getContext();
                if (context == null) return;

                String displayTitle;
                String displayContent;

                if (response.isSuccessful() && response.body() != null) {
                    String generatedDishes = response.body().getAnalysisResult();
                    displayTitle = "✅ 菜品联想成功 - 关键词: " + keyword;

                    // 将菜品列表从原始文本格式化，使其更易读
                    displayContent = generatedDishes
                            .replace("\n", "\n- ")
                            .trim();
                    if(displayContent.startsWith("- ")) {
                        displayContent = displayContent.substring(2); // 移除第一个 "- "
                    }
                    displayContent = "推荐菜品：\n- " + displayContent;

                } else {
                    displayTitle = "❌ 菜品联想失败";
                    displayContent = "HTTP Code: " + response.code();
                    // ... (省略错误日志打印) ...
                }

                // 使用 AlertDialog 显示结果
                showResultDialog(context, displayTitle, displayContent);
            }

            @Override
            public void onFailure(@NonNull Call<DeepSeekResponse> call, @NonNull Throwable t) {
                Context context = getContext();
                if (context == null) return;

                String errorMessage = "网络连接错误或 DeepSeek 服务不可达。";
                Log.e(TAG, errorMessage, t);

                // 使用 AlertDialog 显示连接错误
                showResultDialog(context, "❌ 连接错误", errorMessage);
            }
        });
    }
    /**
     * 🌟 启动 AI 营养师 Activity 🌟
     */
    private void startAiNutritionistActivity() {
        if (getContext() != null) {
            Intent intent = new Intent(getContext(), AINutritionistActivity.class);
            startActivity(intent);
            Toast.makeText(getContext(), "AI 营养师模块启动中...", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 🌟 步骤一：显示菜品输入对话框 🌟
     */
    private void showDishInputDialog() {
        Context context = getContext();
        if (context == null) {
            dismiss();
            return;
        }

        final EditText input = new EditText(context);
        input.setHint("例如：麻婆豆腐");

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("输入菜品名称")
                .setMessage("请输入您想生成宣传文案的菜品名称。")
                .setView(input);

        // 设置确认按钮
        builder.setPositiveButton("生成文案", (dialog, which) -> {
            String dishName = input.getText().toString().trim();
            if (dishName.isEmpty()) {
                Toast.makeText(context, "菜品名称不能为空。", Toast.LENGTH_SHORT).show();
                // 重新显示输入框，但不关闭底部的 Sheet
                showDishInputDialog();
            } else {
                // 捕获输入，并调用 API
                generateDishMarketingCopy(dishName);
            }
        });

        // 设置取消按钮
        builder.setNegativeButton("取消", (dialog, which) -> {
            dialog.cancel();
            dismiss(); // 关闭底部的 TaskSelectionBottomSheet
        });

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    /**
     * 🌟 步骤二：初始化 Retrofit 和 OkHttpClient 🌟
     */
    private void initializeDeepSeekService() {
        if (DEEPSEEK_API_KEY.startsWith("YOUR_")) {
            Log.e(TAG, "API Key 未配置！");
            return;
        }

        // 创建 OkHttpClient 并配置超时和拦截器 (Key Header)
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .header("Authorization", "Bearer " + DEEPSEEK_API_KEY)
                            .header("Content-Type", "application/json")
                            .method(original.method(), original.body())
                            .build();
                    return chain.proceed(request);
                })
                .build();

        // 初始化 Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(DeepSeekService.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        deepSeekService = retrofit.create(DeepSeekService.class);
    }

    /**
     * 🌟 步骤三：调用 DeepSeek API 生成宣传文案 🌟
     * @param dishName 要宣传的菜品名称
     */
    private void generateDishMarketingCopy(String dishName) {
        if (deepSeekService == null || getContext() == null) {
            Toast.makeText(getContext(), "服务初始化失败，无法生成文案。", Toast.LENGTH_LONG).show();
            dismiss();
            return;
        }

        Toast.makeText(getContext(), "正在为 [" + dishName + "] 调用 AI 生成文案...", Toast.LENGTH_LONG).show();

        // 1. 构建 Prompt (保持不变)
        String prompt = String.format(
                "请为今日推荐菜【%s】写一段吸引顾客的宣传文案。文案需突出其美味、特色或健康优势，并以“今日推荐菜”开头。文案长度控制在50字以内。",
                dishName
        );

        // 2. 构建 DeepSeek 请求体 (保持不变)
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("system", "You are a creative marketing copywriter for a Chinese restaurant. Your response must be in Chinese."));
        messages.add(new Message("user", prompt));

        DeepSeekRequest request = new DeepSeekRequest("deepseek-chat", messages);

        // 3. 发起异步网络请求
        deepSeekService.getNutritionAnalysis(request).enqueue(new Callback<DeepSeekResponse>() {
            @Override
            public void onResponse(@NonNull Call<DeepSeekResponse> call, @NonNull Response<DeepSeekResponse> response) {
                Context context = getContext();
                if (context == null) return;

                String displayTitle;
                String displayContent;

                if (response.isSuccessful() && response.body() != null) {
                    // 成功获取文案
                    displayTitle = "✅ 文案生成成功: " + dishName;
                    displayContent = response.body().getAnalysisResult();
                } else {
                    // 失败情况
                    displayTitle = "❌ 文案生成失败";
                    displayContent = "HTTP Code: " + response.code();
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "未知错误";
                        displayContent += "\n详情: " + errorBody;
                        Log.e(TAG, "DeepSeek API 错误: " + response.code() + " 详情: " + errorBody);
                    } catch (IOException e) {
                        Log.e(TAG, "解析错误体失败", e);
                    }
                }

                // 🌟 使用 AlertDialog 显示结果 🌟
                showResultDialog(context, displayTitle, displayContent);

                // ⚠️ 注意：这里不再调用 dismiss()。将在 showResultDialog 的 PositiveButton 中调用。
            }

            @Override
            public void onFailure(@NonNull Call<DeepSeekResponse> call, @NonNull Throwable t) {
                Context context = getContext();
                if (context == null) return;

                String errorMessage = "网络连接错误或 DeepSeek 服务不可达。";
                Log.e(TAG, errorMessage, t);

                // 使用 AlertDialog 显示连接错误
                showResultDialog(context, "❌ 连接错误", errorMessage);
            }
        });
    }
    private void showResultDialog(Context context, String title, String message) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                // 设置一个确认/返回按钮
                .setPositiveButton("确认并返回", (dialog, which) -> {
                    dialog.dismiss(); // 关闭结果对话框
                    dismiss();      // 🌟 关闭底部的 TaskSelectionBottomSheet 🌟
                })
                .setCancelable(false) // 防止点击外部关闭
                .show();
    }
}