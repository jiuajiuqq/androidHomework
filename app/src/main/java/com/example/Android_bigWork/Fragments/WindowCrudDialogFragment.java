package com.example.Android_bigWork.Dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.Android_bigWork.Database.CanteenDao;
import com.example.Android_bigWork.Database.CarteenDatabase;
import com.example.Android_bigWork.Database.WindowDao;
import com.example.Android_bigWork.Database.WindowDatabase;
import com.example.Android_bigWork.Entity.Canteen;
import com.example.Android_bigWork.Entity.Windows;
import com.example.Android_bigWork.R;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors; // 需要 Java 8 或启用 desugaring

public class WindowCrudDialogFragment extends DialogFragment {

    public static final String TAG = "WindowCrudDialog";
    private static final String ARG_WINDOW = "arg_window";

    private Windows currentWindow;
    private WindowDao windowDao;
    private CanteenDao canteenDao; // 用于获取食堂列表，以便选择外键

    private Spinner spCanteen;
    private EditText etName, etDescription, etStatus;
    private Button btnSave, btnDelete;

    // 用于 Spinner 的食堂数据
    private List<Canteen> allCanteens;

    // 接口回调
    public interface WindowCrudListener {
        void onOperationComplete();
    }

    public static WindowCrudDialogFragment newInstance(Windows window) {
        WindowCrudDialogFragment fragment = new WindowCrudDialogFragment();
        Bundle args = new Bundle();
        if (window != null) {
            args.putSerializable(ARG_WINDOW, window); // Windows 实体需要实现 Serializable
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        windowDao = WindowDatabase.getDatabase(getContext()).windowDao();
        canteenDao = CarteenDatabase.getDatabase(getContext()).canteenDao();

        if (getArguments() != null) {
            currentWindow = (Windows) getArguments().getSerializable(ARG_WINDOW);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_window_crud, null);
        builder.setView(view);

        // 绑定视图
        spCanteen = view.findViewById(R.id.sp_window_canteen);
        etName = view.findViewById(R.id.et_window_name);
        etDescription = view.findViewById(R.id.et_window_description);
        etStatus = view.findViewById(R.id.et_window_status);
        btnSave = view.findViewById(R.id.btn_save_window);
        btnDelete = view.findViewById(R.id.btn_delete_window);

        // 异步加载食堂列表
        loadCanteensAndSetupUI();

        // 设置监听器
        btnSave.setOnClickListener(v -> saveWindow());
        btnDelete.setOnClickListener(v -> deleteWindow());

        if (currentWindow != null) {
            builder.setTitle("编辑窗口信息");
            etName.setText(currentWindow.name);
            etDescription.setText(currentWindow.description);
            etStatus.setText(currentWindow.status);
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            builder.setTitle("新增窗口");
            btnDelete.setVisibility(View.GONE);
        }

        return builder.create();
    }

    private void loadCanteensAndSetupUI() {
        new Thread(() -> {
            try {
                // 在后台线程获取所有食堂
                allCanteens = canteenDao.getAllCanteens();

                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        if (isAdded()) {
                            setupCanteenSpinner();
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "加载食堂列表失败", e);
                showToast("加载食堂列表失败！");
            }
        }).start();
    }

    private void setupCanteenSpinner() {
        if (allCanteens == null || allCanteens.isEmpty()) {
            showToast("数据库中没有食堂，请先添加食堂！");
            return;
        }

        // 提取食堂名称列表
        List<String> canteenNames = allCanteens.stream()
                .map(c -> c.name)
                .collect(Collectors.toList());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                canteenNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCanteen.setAdapter(adapter);

        // 如果是编辑模式，设置当前食堂的选择项
        if (currentWindow != null) {
            int selectedIndex = -1;
            for (int i = 0; i < allCanteens.size(); i++) {
                if (allCanteens.get(i).canteenId == currentWindow.canteenId) {
                    selectedIndex = i;
                    break;
                }
            }
            if (selectedIndex != -1) {
                spCanteen.setSelection(selectedIndex);
            }
        }
    }

    private void saveWindow() {
        int selectedPosition = spCanteen.getSelectedItemPosition();
        if (selectedPosition == Spinner.INVALID_POSITION || allCanteens == null || allCanteens.isEmpty()) {
            showToast("请选择一个食堂！");
            return;
        }

        // 获取外键 ID
        final int canteenId = allCanteens.get(selectedPosition).canteenId;
        final String name = etName.getText().toString().trim();
        final String description = etDescription.getText().toString().trim();
        final String status = etStatus.getText().toString().trim();

        if (name.isEmpty()) {
            showToast("窗口名称不能为空！");
            return;
        }

        new Thread(() -> {
            try {
                if (currentWindow == null) {
                    // 【新增 (Create) 逻辑】
                    Windows newWindow = new Windows(canteenId, name, description, status);
                    windowDao.insert(newWindow);
                } else {
                    // 【修改 (Update) 逻辑】
                    currentWindow.canteenId = canteenId; // 允许修改所属食堂
                    currentWindow.name = name;
                    currentWindow.description = description;
                    currentWindow.status = status;
                    windowDao.update(currentWindow);
                }

                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        if (isAdded()) {
                            showToast("窗口操作成功！");
                            notifyListener();
                            dismiss();
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "数据库操作失败", e);
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getActivity(), "窗口操作失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        dismiss();
                    });
                }
            }
        }).start();
    }

    private void deleteWindow() {
        if (currentWindow != null) {
            new Thread(() -> {
                try {
                    // 【删除 (Delete)】
                    windowDao.delete(currentWindow);

                    if (getActivity() != null && isAdded()) {
                        getActivity().runOnUiThread(() -> {
                            if (isAdded()) {
                                showToast("删除窗口成功！");
                                notifyListener();
                                dismiss();
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "删除窗口失败", e);
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
        if (getTargetFragment() instanceof WindowCrudListener) {
            ((WindowCrudListener) getTargetFragment()).onOperationComplete();
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