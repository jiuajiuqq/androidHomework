package com.example.myapplication.Fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.myapplication.DataBase.CanteenDao;
import com.example.myapplication.DataBase.CarteenDatabase;
import com.example.myapplication.Entity.Canteen;
import com.example.myapplication.R;
import com.example.myapplication.Fragment.MenuConfigFragment; // 引入 Fragment 便于回调

// 注意：你需要先创建对应的 dialog_canteen_crud.xml 布局文件

public class CanteenCrudDialogFragment extends DialogFragment {

    public static final String TAG = "CanteenCrudDialog";
    private static final String ARG_CANTEEN = "arg_canteen";

    private Canteen currentCanteen;
    private CanteenDao canteenDao;

    private EditText etName, etLocation, etHours, etStatus;
    private Button btnSave, btnDelete;

    // 接口回调，用于通知 MenuConfigFragment 刷新列表
    public interface CanteenCrudListener {
        void onOperationComplete();
    }

    // 创建实例的方法
    public static CanteenCrudDialogFragment newInstance(Canteen canteen) {
        CanteenCrudDialogFragment fragment = new CanteenCrudDialogFragment();
        Bundle args = new Bundle();
        if (canteen != null) {
            args.putSerializable(ARG_CANTEEN, canteen); // Canteen 实体需要实现 Serializable
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化 DAO
        canteenDao = CarteenDatabase.getDatabase(getContext()).canteenDao();

        if (getArguments() != null) {
            currentCanteen = (Canteen) getArguments().getSerializable(ARG_CANTEEN);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_canteen_crud, null);
        builder.setView(view);

        // 绑定视图
        etName = view.findViewById(R.id.et_canteen_name);
        etLocation = view.findViewById(R.id.et_canteen_location);
        etHours = view.findViewById(R.id.et_canteen_hours);
        etStatus = view.findViewById(R.id.et_canteen_status);
        btnSave = view.findViewById(R.id.btn_save_canteen);
        btnDelete = view.findViewById(R.id.btn_delete_canteen);

        // 如果是编辑模式，填充数据
        if (currentCanteen != null) {
            builder.setTitle("编辑食堂信息");
            etName.setText(currentCanteen.name);
            etLocation.setText(currentCanteen.location);
            etHours.setText(currentCanteen.openTime);
            etStatus.setText(currentCanteen.status);
            btnDelete.setVisibility(View.VISIBLE); // 编辑模式下显示删除按钮
        } else {
            builder.setTitle("新增食堂");
            btnDelete.setVisibility(View.GONE); // 新增模式下隐藏删除按钮
        }

        // 设置监听器
        btnSave.setOnClickListener(v -> saveCanteen());
        btnDelete.setOnClickListener(v -> deleteCanteen());

        return builder.create();
    }

    private void saveCanteen() {
        // 1. 获取和校验输入 (这部分应在 new Thread() 外部执行，以避免 UI 线程等待)
        String name = etName.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String hours = etHours.getText().toString().trim();
        String status = etStatus.getText().toString().trim();

        // 基础校验
        if (name.isEmpty() || location.isEmpty()) {
            showToast("食堂名称和位置不能为空！");
            return;
        }

        // 2. 数据库操作（后台线程）
        new Thread(() -> {
            try {
                if (currentCanteen == null) {
                    // 【新增 (Create) 逻辑】
                    Canteen newCanteen = new Canteen(name, location, hours, status);
                    canteenDao.insert(newCanteen);
                    // 打印日志以确认插入成功
                    Log.d("CanteenCRUD", "新增食堂成功，总数: " + canteenDao.getAllCanteens().size());
                } else {
                    // 【修改 (Update) 逻辑】
                    currentCanteen.name = name;
                    currentCanteen.location = location;
                    currentCanteen.openTime = hours; // 注意：您的 Canteen 实体字段可能是 openTime
                    currentCanteen.status = status;
                    canteenDao.update(currentCanteen);
                    Log.d("CanteenCRUD", "修改食堂成功, ID: " + currentCanteen.canteenId);
                }

                // 3. 所有数据库操作完成后，切换回主线程处理 UI 和回调
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        if (isAdded()) {
                            showToast("操作成功！");
                            notifyListener();
                            dismiss();
                        }
                    });
                } else {
                    Log.e("CanteenCRUD", "Fragment已退出，无法进行UI操作。");
                }
            } catch (Exception e) {
                // 【捕获并处理异常】
                Log.e("CanteenCRUD", "数据库操作失败: " + e.getMessage());
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getActivity(), "数据库操作失败！", Toast.LENGTH_LONG).show();
                        dismiss();
                    });
                }
            }
        }).start();
    }

    private void deleteCanteen() {
        if (currentCanteen != null) {
            // 使用新线程执行数据库操作
            new Thread(() -> {
                try {
                    // 【删除 (Delete)】：在后台线程安全执行
                    canteenDao.delete(currentCanteen);

                    // 数据库操作完成后，切换回主线程处理 UI 和回调
                    if (getActivity() != null && isAdded()) {
                        getActivity().runOnUiThread(() -> {
                            if (isAdded()) {
                                showToast("删除食堂成功！");
                                // 通知 MenuConfigFragment 刷新列表
                                notifyListener();
                                dismiss();
                            }
                        });
                    }

                } catch (Exception e) {
                    // 捕获并处理异常
                    Log.e("CanteenCRUD", "删除食堂失败: " + e.getMessage());
                    e.printStackTrace();

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getActivity(), "删除失败，请重试！", Toast.LENGTH_LONG).show();
                            dismiss();
                        });
                    }
                }
            }).start();
        }
    }

    private void notifyListener() {
        if (getTargetFragment() instanceof CanteenCrudListener) {
            ((CanteenCrudListener) getTargetFragment()).onOperationComplete();
        }
    }

    private void showToast(String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() ->
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show()
            );
        }
    }
}