package com.example.myapplication.Activity;

// ... (保持原有的导入，并添加以下网络相关的导入) ...
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import android.provider.MediaStore;
import android.util.Base64; // 用于 Base64 编码
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.example.myapplication.R;
import com.example.myapplication.model.DeepSeekRequest;
import com.example.myapplication.model.DeepSeekResponse;
import com.example.myapplication.network.DeepSeekService;
import com.example.myapplication.network.NutritionService; // 步骤一创建的接口
import com.example.myapplication.model.NutritionRequest; // 步骤二创建的模型
import com.example.myapplication.model.NutritionResponse; // 步骤二创建的模型

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Part;

public class AINutritionistActivity extends AppCompatActivity {

    private static final String TAG = "AINutritionistActivity";
    private EditText etDishInput;
    private Button btnUploadImage;
    private Button btnAnalyze;
    private TextView tvNutritionResult;

    // 用于存储上传图片的Uri，便于后续处理
    private Uri selectedImageUri = null;

    // Activity Result Launchers
    // 1. 用于处理运行时权限请求
    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    // 2. 用于处理从相册选择图片的结果
    private ActivityResultLauncher<String> selectImageLauncher;
    private static final String BASE_URL = "sk-e5335b8c2b0049709618bb38046642b0"; // ⚠️ 请替换为您的真实API基础地址
    private static final String DEEPSEEK_API_KEY = "sk-dfbb419e72094dd1ae3b7912644b3f3d";
    private DeepSeekService deepSeekService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_nutritionist);

        // 1. 初始化 Retrofit 客户端和 DeepSeekService
        initializeDeepSeekService();

        // 2. 设置 ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("AI 营养师 (DeepSeek)");
        }

        // 3. 视图初始化
        etDishInput = findViewById(R.id.et_dish_input);
        btnUploadImage = findViewById(R.id.btn_upload_image);
        btnAnalyze = findViewById(R.id.btn_analyze);
        tvNutritionResult = findViewById(R.id.tv_nutrition_result);

        // 4. 设置按钮监听器
        // 假设您已移除了图片上传逻辑，只保留文本分析
        btnAnalyze.setOnClickListener(v -> {
            String inputText = etDishInput.getText().toString().trim();

            if (inputText.isEmpty()) {
                Toast.makeText(this, "请输入菜品名称。", Toast.LENGTH_LONG).show();
                return;
            }

            callAiNutritionistApi(inputText, selectedImageUri); // 忽略 selectedImageUri
        });

        // 移除或禁用图片上传按钮的监听器，如果您不需要它
        btnUploadImage.setOnClickListener(v -> Toast.makeText(this, "图片上传功能当前已禁用。", Toast.LENGTH_SHORT).show());
    }

    private void initializeDeepSeekService() {
        if (DEEPSEEK_API_KEY.startsWith("YOUR_")) {
            Log.e(TAG, "API Key 未配置！");
            return;
        }

        // 创建 OkHttpClient 并添加拦截器，将 API Key 放入 Authorization Header
        OkHttpClient client = new OkHttpClient.Builder()
                // 🌟 增加连接超时 🌟
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                // 🌟 增加读取超时 🌟
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .header("Authorization", "Bearer " + DEEPSEEK_API_KEY)
                            .header("Content-Type", "application/json") // DeepSeek 要求 JSON
                            .method(original.method(), original.body())
                            .build();
                    return chain.proceed(request);
                })
                .build();

        // 初始化 Retrofit 客户端
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(DeepSeekService.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        deepSeekService = retrofit.create(DeepSeekService.class);
    }

    /**
     * 初始化 Activity Result API 的 launchers
     */
    private void setupActivityResultLaunchers() {
        // 注册权限请求的回调
        requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), isGranted -> {
                    if (isGranted.getOrDefault(Manifest.permission.READ_EXTERNAL_STORAGE, false)) {
                        // 权限已授予，启动图库
                        launchImageSelector();
                    } else {
                        Toast.makeText(this, "需要存储权限才能选择图片。", Toast.LENGTH_SHORT).show();
                    }
                });

        // 注册图片选择的回调
        selectImageLauncher =
                registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        etDishInput.setText(""); // 如果上传了图片，清空文本输入
                        btnUploadImage.setText("图片已选定");
                        Toast.makeText(this, "图片已成功选定。", Toast.LENGTH_SHORT).show();
                    } else {
                        selectedImageUri = null;
                        btnUploadImage.setText("上传图片");
                        Toast.makeText(this, "未选择图片。", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 检查存储权限并启动图库选择器
     */
    private void checkPermissionAndLaunchGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            // 权限已授予，直接启动图库
            launchImageSelector();
        } else {
            // 请求权限
            requestPermissionLauncher.launch(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE});
        }
    }

    /**
     * 启动系统图库选择器
     */
    private void launchImageSelector() {
        // 使用 "image/*" 表示选择所有图片类型
        selectImageLauncher.launch("image/*");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 🌟 步骤三：AI 营养师 API 调用的占位方法（模拟） 🌟
     * 这是您真正需要实现网络请求和 API 调用的地方。
     */
    private void callAiNutritionistApi(String dishName, Uri imageUri) {
        tvNutritionResult.setText("正在分析中，请稍候...");

        if (deepSeekService == null) {
            tvNutritionResult.setText("错误：DeepSeek 服务初始化失败，请检查 API Key。");
            return;
        }

        // 1. 构建 DeepSeek 请求体
        String prompt = "请作为专业的 AI 营养师，分析菜品 “" + dishName + "”。生成一个详细的营养成分概览（估计热量、蛋白质、脂肪、碳水），并提供针对性的健康饮食建议。格式清晰，使用 Markdown 格式。";

        List<DeepSeekRequest.Message> messages = new ArrayList<>();
        // 增加系统角色设定，提高回答质量
        messages.add(new DeepSeekRequest.Message("system", "You are a professional nutritionist AI. Your response must be in Chinese and use markdown formatting."));
        messages.add(new DeepSeekRequest.Message("user", prompt));

        // 使用 deepseek-coder 模型，或根据您的需求选择 deepseek-chat
        DeepSeekRequest request = new DeepSeekRequest("deepseek-chat", messages);

        // 2. 发起异步网络请求
        deepSeekService.getNutritionAnalysis(request).enqueue(new Callback<DeepSeekResponse>() {
            @Override
            public void onResponse(@NonNull Call<DeepSeekResponse> call, @NonNull Response<DeepSeekResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 请求成功
                    String resultText = response.body().getAnalysisResult();
                    if (resultText != null && !resultText.isEmpty()) {
                        tvNutritionResult.setText(resultText);
                        Toast.makeText(AINutritionistActivity.this, "DeepSeek 分析完成！", Toast.LENGTH_SHORT).show();
                    } else {
                        tvNutritionResult.setText("DeepSeek 返回空结果，可能请求或模型输出异常。");
                    }
                } else {
                    // HTTP 错误 (4xx 或 5xx)
                    String errorMsg = "DeepSeek API 请求失败。HTTP Code: " + response.code();
                    try {
                        errorMsg += "\n错误详情: " + response.errorBody().string();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    tvNutritionResult.setText(errorMsg);
                    Log.e(TAG, errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<DeepSeekResponse> call, @NonNull Throwable t) {
                // 网络连接错误
                tvNutritionResult.setText("网络连接错误或 DeepSeek 服务不可达: " + t.getMessage());
                Log.e(TAG, "网络连接错误: " + t.getMessage());
            }
        });
    }

    // 模拟生成结果的方法
    private String generateMockNutritionResult(String dishName, Uri imageUri) {
        if (!dishName.isEmpty()) {
            String lowerCaseDish = dishName.toLowerCase();
            if (lowerCaseDish.contains("fish")) {
                return "【AI 营养分析 - 鱼类】\n\n" +
                        "营养成分：富含优质蛋白（约20g/100g），Omega-3 脂肪酸（DHA/EPA）。\n" +
                        "饮食建议：每周食用2-3次，有助于心血管健康。若煎炸，请搭配大量蔬菜以平衡脂肪摄入。";
            } else if (lowerCaseDish.contains("chicken")) {
                return "【AI 营养分析 - 鸡肉】\n\n" +
                        "营养成分：高蛋白、低脂肪（若去皮）。每100克鸡胸肉约含30克蛋白质。\n" +
                        "饮食建议：鸡肉是健身理想选择，搭配碳水化合物（如米饭）和蔬菜，可确保能量均衡。";
            } else {
                return String.format("【AI 营养分析 - %s】\n\n营养分析：根据您输入的菜品已完成初步分析。\n饮食建议：请适量食用。", dishName);
            }
        } else if (imageUri != null) {
            return "【AI 营养分析 - 图片识别】\n\n" +
                    "图片已成功接收，AI 正在分析菜品内容和份量。\n" +
                    "分析结果：高蛋白、中等碳水，建议增加膳食纤维。\n" +
                    "图片URI: " + imageUri.getLastPathSegment();
        }
        return "未能识别输入内容。";
    }
    private String uriToBase64(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        if (inputStream == null) {
            throw new IOException("无法打开图片流");
        }

        // 读取输入流到字节数组
        byte[] bytes;
        byte[] buffer = new byte[8192];
        int bytesRead;
        java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
        bytes = output.toByteArray();

        inputStream.close();

        // 将字节数组编码为 Base64 字符串
        // Base64.NO_WRAP 用于移除换行符
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }
}