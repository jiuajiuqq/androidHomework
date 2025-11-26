package com.example.myapplication.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.DataBase.PersonDao; // 引用 PersonDao
import com.example.myapplication.DataBase.PersonDatabase; // UserDatabase 需更新为引用 Person.class
import com.example.myapplication.Entity.Person; // 引用 Person 实体
import com.example.myapplication.R; // 假设您的项目资源路径

// 假设您已经将 UserDatabase 类更新为引用 Person.class 并提供 personDao() 方法

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private Button btnRegister;
    private Button btnLogin;

    private PersonDao personDao; // 更改为 PersonDao

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // 假设布局文件名为 activity_login_register

        initViews();

        // 1. 初始化数据库 DAO (假设 UserDatabase.getDatabase(this).personDao() 已经配置正确)
        personDao = PersonDatabase.getDatabase(this).personDao();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleRegister();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnLogin = findViewById(R.id.btnLogin);
    }

    /**
     * 处理用户注册逻辑 (直接存储明文密码)
     */
    private void handleRegister() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "用户名和密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. 检查用户名是否已存在
        Person existingPerson = personDao.getUserByUsername(username);

        if (existingPerson != null) {
            Toast.makeText(this, "注册失败：该用户名已被占用", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. 创建新用户实体 (直接使用明文密码，默认角色为学生)
        Person newPerson = new Person(
                username,
                password, // 直接存储明文密码
                Person.ROLE_STUDENT,
                System.currentTimeMillis()
        );

        // 3. 插入数据库
        try {
            personDao.insert(newPerson);
            Toast.makeText(this, "注册成功！请登录。", Toast.LENGTH_LONG).show();
            Log.d("UserAction", "User registered: " + username);
        } catch (Exception e) {
            Toast.makeText(this, "注册失败：数据库错误", Toast.LENGTH_SHORT).show();
            Log.e("UserAction", "Database insert error", e);
        }
    }

    /**
     * 处理用户登录逻辑 (直接比对明文密码)
     */
    private void handleLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. 根据用户名查找用户
        Person person = personDao.getUserByUsername(username);

        if (person == null) {
            Toast.makeText(this, "登录失败：用户名不存在", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. 验证密码 (直接比对明文密码)
        if (password.equals(person.password)) {
            Toast.makeText(this, "登录成功！欢迎 " + person.username, Toast.LENGTH_LONG).show();
            Log.d("UserAction", "User logged in: " + username);

            // 3. 登录成功，跳转到主界面
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("USER_ID", person.userId);
            intent.putExtra("USER_ROLE", person.role);
            startActivity(intent);
            finish(); // 销毁登录界面
        } else {
            Toast.makeText(this, "登录失败：密码错误", Toast.LENGTH_SHORT).show();
        }
    }
}