package com.example.Android_bigWork.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.content.res.ColorStateList; // 导入 ColorStateList
import androidx.core.content.ContextCompat; // 导入 ContextCompat

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Android_bigWork.Adapters.CanteenAdapter;
import com.example.Android_bigWork.Adapters.DishAdapter;
import com.example.Android_bigWork.Adapters.WindowAdapter;
import com.example.Android_bigWork.Database.CanteenDao;
import com.example.Android_bigWork.Database.WindowDao;
import com.example.Android_bigWork.Database.DishDao;
import com.example.Android_bigWork.Database.CarteenDatabase; // 假设食堂/窗口DAO在此
import com.example.Android_bigWork.Database.WindowDatabase;
import com.example.Android_bigWork.Database.DishDatabase; // 假设菜品DAO在此
import com.example.Android_bigWork.Entity.Canteen;
import com.example.Android_bigWork.Entity.Dish;
import com.example.Android_bigWork.Entity.Windows;
import com.example.Android_bigWork.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.Android_bigWork.Dialog.WindowCrudDialogFragment;
import com.example.Android_bigWork.Dialog.DishCrudDialogFragment;

import android.text.Editable; // 导入
import android.text.TextWatcher; // 导入
import android.widget.EditText; // 导入
import java.util.List;

public class MenuConfigFragment extends Fragment implements CanteenAdapter.OnCanteenClickListener, WindowAdapter.OnWindowClickListener,
        DishAdapter.OnDishClickListener ,
        CanteenCrudDialogFragment.CanteenCrudListener,
        WindowCrudDialogFragment.WindowCrudListener,
        DishCrudDialogFragment.DishCrudListener{
    @Override
    public void onOperationComplete() {
        // 确保在 Fragment 附加到 Activity时执行
        if (isAdded()) {
            updateList(); // <--- 关键调用
        }
    }

    @Override
    public void onDishClick(Dish dish) {
        // 当点击菜品列表项时，调用 showCrudDialog 进入编辑/详情模式
        showCrudDialog("菜品", dish);
    }

    @Override
    public void onCanteenClick(Canteen canteen) {
        // 当点击列表项时，调用 showCrudDialog 进入编辑/详情模式
        showCrudDialog("食堂", canteen);
    }

    @Override
    public void onWindowClick(Windows window) {
        showCrudDialog("窗口", window);
    }

    // UI 元素
    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    // 修正：将 Button 改为 MaterialButton
    private com.google.android.material.button.MaterialButton btnCanteen, btnWindow, btnDish;

    // 数据库 DAO
    private CanteenDao canteenDao;
    private WindowDao windowDao;
    private DishDao dishDao;

    private EditText etSearch; // 【新增】搜索输入框

    private enum DisplayMode { CANTEEN, WINDOW, DISH }
    private DisplayMode currentMode = DisplayMode.CANTEEN;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 假设布局文件名为 fragment_menu_config
        View view = inflater.inflate(R.layout.fragment_menu_config, container, false);

        recyclerView = view.findViewById(R.id.rv_menu_list);
        fabAdd = view.findViewById(R.id.fab_add_item);
        btnCanteen = view.findViewById(R.id.btn_canteen_mode);
        btnWindow = view.findViewById(R.id.btn_window_mode);
        btnDish = view.findViewById(R.id.btn_dish_mode);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        etSearch = view.findViewById(R.id.et_search_query);

        // 实例化 DAO
        canteenDao = CarteenDatabase.getDatabase(getContext()).canteenDao();
        windowDao = WindowDatabase.getDatabase(getContext()).windowDao();
        dishDao = DishDatabase.getDatabase(getContext()).getDishDao();

        initListeners();
        switchMode(DisplayMode.CANTEEN); // 默认加载食堂列表

        return view;
    }

    private void initListeners() {
        btnCanteen.setOnClickListener(v -> switchMode(DisplayMode.CANTEEN));
        btnWindow.setOnClickListener(v -> switchMode(DisplayMode.WINDOW));
        btnDish.setOnClickListener(v -> switchMode(DisplayMode.DISH));

        fabAdd.setOnClickListener(v -> handleAddItem());
        // 【修改】搜索框文本监听器：现在包括 DISH 模式
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 每次文本变化时，在所有模式下都执行搜索/刷新
                if (currentMode == DisplayMode.CANTEEN || currentMode == DisplayMode.WINDOW || currentMode == DisplayMode.DISH) {
                    updateList();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void switchMode(DisplayMode mode) {
        currentMode = mode;

        // 【新增或修改】：在切换模式时，更新按钮样式
        updateButtonStyles();
        // 切换模式时，清空搜索框
        if (etSearch != null) {
            etSearch.setText("");
            // 提示用户当前搜索目标
            if (mode == DisplayMode.CANTEEN) {
                etSearch.setHint("搜索食堂名称或位置...");
                etSearch.setVisibility(View.VISIBLE);
            } else if (mode == DisplayMode.WINDOW) { // 【新增窗口搜索提示】
                etSearch.setHint("搜索窗口名称或描述...");
                etSearch.setVisibility(View.VISIBLE);
            } else if (mode == DisplayMode.DISH) { // 【新增菜品搜索提示】
                etSearch.setHint("搜索菜品名称、描述或类别...");
                etSearch.setVisibility(View.VISIBLE);
            }
        }
        // 刷新列表
        updateList();
    }

    private void handleAddItem() {
        String itemType = "";
        switch (currentMode) {
            case CANTEEN:
                CanteenCrudDialogFragment dialog = CanteenCrudDialogFragment.newInstance(null);
                dialog.setTargetFragment(this, 0); // 设置回调目标
                dialog.show(getParentFragmentManager(), CanteenCrudDialogFragment.TAG);
                return;
            case WINDOW:
                WindowCrudDialogFragment wDialog = WindowCrudDialogFragment.newInstance(null);
                wDialog.setTargetFragment(this, 0);
                wDialog.show(getParentFragmentManager(), WindowCrudDialogFragment.TAG);
                return;
            case DISH:
                DishCrudDialogFragment dDialog = DishCrudDialogFragment.newInstance(null);
                dDialog.setTargetFragment(this, 0);
                dDialog.show(getParentFragmentManager(), DishCrudDialogFragment.TAG);
                return;
        }

        // 调用通用的 CRUD Dialog 方法，itemData = null 表示新增操作
        showCrudDialog(itemType, null);
    }

    /**
     * 根据当前模式加载数据和适配器
     */
    private void updateList() {
        final String currentQuery = etSearch.getText().toString().trim();

        switch (currentMode) {
            case CANTEEN:
                // 在后台线程执行查询操作，避免 ANR
                new Thread(() -> {
                    List<Canteen> canteens;

                    if (currentQuery.isEmpty()) {
                        // 搜索关键词为空，加载全部食堂
                        canteens = canteenDao.getAllCanteens();
                        Log.d("MenuConfig", "加载全部食堂，数量: " + canteens.size());
                    } else {
                        // 搜索关键词不为空，执行模糊查询
                        // Room SQL 需要手动添加 %
                        String searchQuery = "%" + currentQuery + "%";
                        canteens = canteenDao.searchCanteens(searchQuery);
                        Log.d("MenuConfig", "搜索 [" + currentQuery + "]，结果数量: " + canteens.size());
                    }

                    // 切换回主线程更新 UI
                    if (getActivity() != null) {
                        List<Canteen> finalCanteens = canteens; // 变量必须是 final 或 effectively final
                        getActivity().runOnUiThread(() -> {
                            // 检查 Fragment 状态
                            if (isAdded()) {
                                recyclerView.setAdapter(new CanteenAdapter(finalCanteens, this));
                            }
                        });
                    }
                }).start();
                break;
            case WINDOW: // 【新增 WINDOW 查询逻辑】
                new Thread(() -> {
                    List<Windows> windows;

                    if (currentQuery.isEmpty()) {
                        // 搜索关键词为空，加载全部窗口
                        windows = windowDao.getAllWindows();
                        Log.d("MenuConfig", "加载全部窗口，数量: " + windows.size());
                    } else {
                        // 搜索关键词不为空，执行模糊查询
                        String searchQuery = "%" + currentQuery + "%";
                        windows = windowDao.searchWindows(searchQuery);
                        Log.d("MenuConfig", "窗口搜索 [" + currentQuery + "]，结果数量: " + windows.size());
                    }

                    // 切换回主线程更新 UI
                    if (getActivity() != null) {
                        List<Windows> finalWindows = windows;
                        getActivity().runOnUiThread(() -> {
                            if (isAdded()) {
                                recyclerView.setAdapter(new WindowAdapter(finalWindows, this));
                            }
                        });
                    }
                }).start();
                break;
            case DISH: // 【新增 DISH 查询逻辑】
                new Thread(() -> {
                    List<Dish> dishes;

                    if (currentQuery.isEmpty()) {
                        // 搜索关键词为空，加载全部菜品
                        dishes = dishDao.getAllDishes();
                        Log.d("MenuConfig", "加载全部菜品，数量: " + dishes.size());
                    } else {
                        // 搜索关键词不为空，执行模糊查询
                        String searchQuery = "%" + currentQuery + "%";
                        dishes = dishDao.searchDishes(searchQuery);
                        Log.d("MenuConfig", "菜品搜索 [" + currentQuery + "]，结果数量: " + dishes.size());
                    }

                    // 切换回主线程更新 UI
                    if (getActivity() != null) {
                        List<Dish> finalDishes = dishes;
                        getActivity().runOnUiThread(() -> {
                            if (isAdded()) {
                                recyclerView.setAdapter(new DishAdapter(finalDishes, this));
                            }
                        });
                    }
                }).start();
                break;
        }
    }

    /**
     * 处理新增和修改 Dialog 逻辑
     */
    private void showCrudDialog(String itemType, Object itemData) {
        // TODO: 在这里实现具体的增删改查 Dialog 逻辑。
        //       你需要根据 itemType 和 itemData 是否为空，判断是执行新增还是编辑/删除操作。

        if (itemType.equals("食堂") && itemData instanceof Canteen) {
            // 【编辑食堂】
            Canteen canteen = (Canteen) itemData;
            CanteenCrudDialogFragment dialog = CanteenCrudDialogFragment.newInstance(canteen);
            dialog.setTargetFragment(this, 0); // 设置回调目标
            dialog.show(getParentFragmentManager(), CanteenCrudDialogFragment.TAG);
        } else if (itemType.equals("窗口") && itemData instanceof Windows) { // 【新增窗口编辑逻辑】
            // 编辑窗口
            Windows window = (Windows) itemData;
            WindowCrudDialogFragment dialog = WindowCrudDialogFragment.newInstance(window);
            dialog.setTargetFragment(this, 0);
            dialog.show(getParentFragmentManager(), WindowCrudDialogFragment.TAG);
        } else if (itemType.equals("菜品") && itemData instanceof Dish) { // 【新增菜品编辑逻辑】
            // 编辑菜品
            Dish dish = (Dish) itemData;
            DishCrudDialogFragment dialog = DishCrudDialogFragment.newInstance(dish);
            dialog.setTargetFragment(this, 0);
            dialog.show(getParentFragmentManager(), DishCrudDialogFragment.TAG);
        }
        // 【重要】数据库操作成功后，记得调用 updateList() 刷新界面。
    }
    private void updateButtonStyles() {
        if (getContext() == null) return;

        // 获取颜色值 (使用 ContextCompat 确保兼容性)
        int selectedColor = ContextCompat.getColor(getContext(), R.color.red_700);
        int unselectedColor = ContextCompat.getColor(getContext(), R.color.gray_300);

        // 创建 ColorStateList 用于设置背景Tint
        ColorStateList selectedCsl = ColorStateList.valueOf(selectedColor);
        ColorStateList unselectedCsl = ColorStateList.valueOf(unselectedColor);

        // 重点：如果 setBackgroundTintList 无效，尝试重新设置背景为 null
        // 然后再设置 Tint。

        // 食堂按钮
        if (currentMode == DisplayMode.CANTEEN) {
            btnCanteen.setBackgroundTintList(selectedCsl);
        } else {
            btnCanteen.setBackgroundTintList(unselectedCsl);
        }

        // 窗口按钮
        if (currentMode == DisplayMode.WINDOW) {
            btnWindow.setBackgroundTintList(selectedCsl);
        } else {
            btnWindow.setBackgroundTintList(unselectedCsl);
        }

        // 菜品按钮
        if (currentMode == DisplayMode.DISH) {
            btnDish.setBackgroundTintList(selectedCsl);
        } else {
            btnDish.setBackgroundTintList(unselectedCsl);
        }
    }
}