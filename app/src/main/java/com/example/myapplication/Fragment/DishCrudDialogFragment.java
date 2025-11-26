package com.example.myapplication.Dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.myapplication.DataBase.WindowDao;
import com.example.myapplication.DataBase.WindowDatabase;
import com.example.myapplication.DataBase.DishDao;
import com.example.myapplication.DataBase.DishDatabase;
import com.example.myapplication.Entity.Dish;
import com.example.myapplication.Entity.Windows;
import com.example.myapplication.R;

import java.util.List;
import java.util.stream.Collectors;

public class DishCrudDialogFragment extends DialogFragment {

    public static final String TAG = "DishCrudDialog";
    private static final String ARG_DISH = "arg_dish";

    private Dish currentDish;
    private DishDao dishDao;
    private WindowDao windowDao;

    private Spinner spWindow;
    private EditText etName, etPrice, etDescription, etCategory;
    private CheckBox cbAvailable;
    private Button btnSave, btnDelete;

    // 用于 Spinner 的窗口数据
    private List<Windows> allWindows;

    // 接口回调
    public interface DishCrudListener {
        void onOperationComplete();
    }

    public static DishCrudDialogFragment newInstance(Dish dish) {
        DishCrudDialogFragment fragment = new DishCrudDialogFragment();
        Bundle args = new Bundle();
        if (dish != null) {
            args.putSerializable(ARG_DISH, dish); // Dish 实体需要实现 Serializable
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dishDao = DishDatabase.getDatabase(getContext()).dishDao();
        windowDao = WindowDatabase.getDatabase(getContext()).windowDao();

        if (getArguments() != null) {
            currentDish = (Dish) getArguments().getSerializable(ARG_DISH);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_dish_crud, null);
        builder.setView(view);

        // 绑定视图
        spWindow = view.findViewById(R.id.sp_dish_window);
        etName = view.findViewById(R.id.et_dish_name);
        etPrice = view.findViewById(R.id.et_dish_price);
        etDescription = view.findViewById(R.id.et_dish_description);
        etCategory = view.findViewById(R.id.et_dish_category);
        cbAvailable = view.findViewById(R.id.cb_dish_available);
        btnSave = view.findViewById(R.id.btn_save_dish);
        btnDelete = view.findViewById(R.id.btn_delete_dish);

        // 异步加载窗口列表
        loadWindowsAndSetupUI();

        // 设置监听器
        btnSave.setOnClickListener(v -> saveDish());
        btnDelete.setOnClickListener(v -> deleteDish());

        if (currentDish != null) {
            builder.setTitle("编辑菜品信息");
            etName.setText(currentDish.name);
            etPrice.setText(String.valueOf(currentDish.price)); // 注意：价格需要转换
            etDescription.setText(currentDish.description);
            etCategory.setText(currentDish.category);
            cbAvailable.setChecked(currentDish.isAvailable); // 注意：状态字段
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            builder.setTitle("新增菜品");
            cbAvailable.setChecked(true); // 默认上架
            btnDelete.setVisibility(View.GONE);
        }

        return builder.create();
    }

    private void loadWindowsAndSetupUI() {
        new Thread(() -> {
            try {
                // 在后台线程获取所有窗口
                allWindows = windowDao.getAllWindows();

                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        if (isAdded()) {
                            setupWindowSpinner();
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "加载窗口列表失败", e);
                showToast("加载窗口列表失败！");
            }
        }).start();
    }

    private void setupWindowSpinner() {
        if (allWindows == null || allWindows.isEmpty()) {
            showToast("数据库中没有窗口，请先添加窗口！");
            return;
        }

        // 提取窗口名称列表
        List<String> windowNames = allWindows.stream()
                .map(w -> w.name + " (ID:" + w.windowId + ")") // 方便区分同名窗口
                .collect(Collectors.toList());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                windowNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spWindow.setAdapter(adapter);

        // 如果是编辑模式，设置当前窗口的选择项
        if (currentDish != null) {
            int selectedIndex = -1;
            for (int i = 0; i < allWindows.size(); i++) {
                if (allWindows.get(i).windowId == currentDish.windowId) {
                    selectedIndex = i;
                    break;
                }
            }
            if (selectedIndex != -1) {
                spWindow.setSelection(selectedIndex);
            }
        }
    }

    private void saveDish() {
        int selectedPosition = spWindow.getSelectedItemPosition();
        if (selectedPosition == Spinner.INVALID_POSITION || allWindows == null || allWindows.isEmpty()) {
            showToast("请选择一个所属窗口！");
            return;
        }

        // 获取外键 ID 和输入数据
        final int windowId = allWindows.get(selectedPosition).windowId;
        final String name = etName.getText().toString().trim();
        final String priceStr = etPrice.getText().toString().trim();
        final String description = etDescription.getText().toString().trim();
        final String category = etCategory.getText().toString().trim();
        final boolean isAvailable = cbAvailable.isChecked();

        if (name.isEmpty() || priceStr.isEmpty()) {
            showToast("菜品名称和价格不能为空！");
            return;
        }

        final double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            showToast("价格格式不正确！");
            return;
        }


        new Thread(() -> {
            try {
                if (currentDish == null) {
                    // 【新增 (Create) 逻辑】
                    // 假设 Dish 构造函数：Dish(windowId, name, price, description, category, imageUrl, isAvailable)
                    // 暂无 imageUrl 字段，传空字符串
                    Dish newDish = new Dish(windowId, name, price, description, category, "", isAvailable, 10);
                    dishDao.insert(newDish);
                } else {
                    // 【修改 (Update) 逻辑】
                    currentDish.windowId = windowId;
                    currentDish.name = name;
                    currentDish.price = price;
                    currentDish.description = description;
                    currentDish.category = category;
                    currentDish.isAvailable = isAvailable;
                    // currentDish.imageUrl 保持不变
                    dishDao.update(currentDish);
                }

                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        if (isAdded()) {
                            showToast("菜品操作成功！");
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
                        Toast.makeText(getActivity(), "菜品操作失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        dismiss();
                    });
                }
            }
        }).start();
    }

    private void deleteDish() {
        if (currentDish != null) {
            new Thread(() -> {
                try {
                    // 【删除 (Delete)】
                    dishDao.delete(currentDish);

                    if (getActivity() != null && isAdded()) {
                        getActivity().runOnUiThread(() -> {
                            if (isAdded()) {
                                showToast("删除菜品成功！");
                                notifyListener();
                                dismiss();
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "删除菜品失败", e);
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
        if (getTargetFragment() instanceof DishCrudListener) {
            ((DishCrudListener) getTargetFragment()).onOperationComplete();
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