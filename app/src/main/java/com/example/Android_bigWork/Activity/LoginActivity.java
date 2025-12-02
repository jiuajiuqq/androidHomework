package com.example.Android_bigWork.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.Android_bigWork.Database.PersonDao;
import com.example.Android_bigWork.Database.PersonDatabase;
import com.example.Android_bigWork.Entity.Person;
import com.example.Android_bigWork.R;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private Button btnRegister;
    private Button btnLogin;

    // 角色选择器
    private RadioGroup rgRoleSelector;
    private RadioButton rbStudent;
    private RadioButton rbAdmin;

    private PersonDao personDao;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();

        // 1. 初始化数据库 DAO
        // ⚠️ 注意：如果 PersonDatabase 未使用 .allowMainThreadQueries()，
        // 则 DAO 的方法调用必须在后台线程中执行。
        personDao = PersonDatabase.getDatabase(this).getPersonDao();

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

        // 初始化新的视图组件
        rgRoleSelector = findViewById(R.id.rgRoleSelector);
        rbStudent = findViewById(R.id.rbStudent);
        rbAdmin = findViewById(R.id.rbAdmin);
    }

    /**
     * 处理用户注册逻辑 (数据库操作在后台线程执行)
     */
    private void handleRegister() {
        final String username = etUsername.getText().toString().trim();
        final String password = etPassword.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "用户名和密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🌟 修复：使用新线程执行注册数据库操作 🌟
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 1. 检查用户名是否已存在 (后台线程 I/O)
                Person existingPerson = personDao.getUserByUsername(username);

                // 2. 切回主线程处理 UI 结果
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (existingPerson != null) {
                            Toast.makeText(LoginActivity.this, "注册失败：该用户名已被占用", Toast.LENGTH_SHORT).show();
                        } else {
                            // 3. 在后台线程插入新用户
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Person newPerson = new Person(
                                            username,
                                            password,
                                            Person.ROLE_STUDENT,
                                            System.currentTimeMillis(),
                                            110,
                                            Person.GENDER_MALE,
                                            123
                                    );
                                    try {
                                        personDao.insert(newPerson);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(LoginActivity.this, "注册成功！请登录。", Toast.LENGTH_LONG).show();
                                                Log.d("UserAction", "User registered: " + username);
                                            }
                                        });
                                    } catch (Exception e) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(LoginActivity.this, "注册失败：数据库错误", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        Log.e("UserAction", "Database insert error", e);
                                    }
                                }
                            }).start();
                        }
                    }
                });
            }
        }).start();
    }

    /**
     * 处理用户登录逻辑 (数据库操作在后台线程执行)
     */
    private void handleLogin() {
        final String username = etUsername.getText().toString().trim();
        final String password = etPassword.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
            return;
        }

        final String selectedRole;
        if (rbStudent.isChecked()) {
            selectedRole = Person.ROLE_STUDENT;
        } else if (rbAdmin.isChecked()) {
            selectedRole = Person.ROLE_ADMIN;
        } else {
            Toast.makeText(this, "请选择登录身份", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🌟 修复：使用新线程执行登录数据库查询 🌟
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 1. 在后台线程根据用户名查找用户
                final Person person = personDao.getUserByUsername(username);

                // 2. 切回主线程处理 UI 逻辑和跳转
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (person == null) {
                            Toast.makeText(LoginActivity.this, "登录失败：用户名不存在", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // 验证密码
                        if (!password.equals(person.password)) {
                            Toast.makeText(LoginActivity.this, "登录失败：密码错误", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // 验证用户选择的角色是否与数据库中存储的角色匹配
                        if (!person.role.equals(selectedRole)) {
                            Toast.makeText(LoginActivity.this, "登录失败：您的身份与选择的身份不匹配！", Toast.LENGTH_LONG).show();
                            return;
                        }

                        // 3. 登录成功，跳转到对应的 Activity
                        Toast.makeText(LoginActivity.this, "登录成功！欢迎 " + person.username, Toast.LENGTH_LONG).show();
                        Log.d("UserAction", "User logged in as " + selectedRole + ": " + username);

                        Intent intent;
                        if (selectedRole.equals(Person.ROLE_ADMIN)) {
                            // 管理员跳转到管理端主页
                            intent = new Intent(LoginActivity.this, AdminMainActivity.class);
                        } else {
                            // 普通用户跳转到用户端主页
                            intent = new Intent(LoginActivity.this, MainActivity.class);
                        }

                        // 🌟 关键修复：传输完整的 Person 对象，满足 MainActivity 的需求 🌟
                        intent.putExtra("user", person);

                        // 原始代码中的这两行不再是关键，但如果 AdminMainActivity 等需要，可以保留
                        intent.putExtra("USER_ID", person.UID);
                        intent.putExtra("USER_ROLE", person.role);

                        startActivity(intent);
                        finish();
                    }
                });
            }
        }).start();
    }
}