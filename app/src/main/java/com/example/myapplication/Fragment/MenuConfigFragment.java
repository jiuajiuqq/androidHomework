package com.example.myapplication.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import android.content.res.ColorStateList; // 导入 ColorStateList
import androidx.core.content.ContextCompat; // 导入 ContextCompat

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Adapter.CanteenAdapter;
import com.example.myapplication.Adapter.DishAdapter;
import com.example.myapplication.Adapter.WindowAdapter;
import com.example.myapplication.DataBase.CanteenDao;
import com.example.myapplication.DataBase.WindowDao;
import com.example.myapplication.DataBase.DishDao;
import com.example.myapplication.DataBase.CarteenDatabase; // 假设食堂/窗口DAO在此
import com.example.myapplication.DataBase.WindowDatabase;
import com.example.myapplication.DataBase.DishDatabase; // 假设菜品DAO在此
import com.example.myapplication.Entity.Canteen;
import com.example.myapplication.Entity.Dish;
import com.example.myapplication.Entity.Windows;
import com.example.myapplication.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.myapplication.Dialog.WindowCrudDialogFragment;
import com.example.myapplication.Dialog.DishCrudDialogFragment;

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
        dishDao = DishDatabase.getDatabase(getContext()).dishDao();

        initListeners();
        switchMode(DisplayMode.CANTEEN); // 默认加载食堂列表

        return view;
    }

    private void initListeners() {
        btnCanteen.setOnClickListener(v -> switchMode(DisplayMode.CANTEEN));
        btnWindow.setOnClickListener(v -> switchMode(DisplayMode.WINDOW));
        btnDish.setOnClickListener(v -> switchMode(DisplayMode.DISH));

        fabAdd.setOnClickListener(v -> handleAddItem());
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 每次文本变化时，只在当前模式为 CANTEEN 时执行搜索
                if (currentMode == DisplayMode.CANTEEN) {
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
            } else {
                // 可以在非食堂模式下隐藏搜索框，或更改提示
                etSearch.setHint("请切换到食堂模式搜索");
                // etSearch.setVisibility(View.GONE); // 如果只想在食堂模式下显示
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
            case WINDOW:
                // 1. 获取数据 (假设 WindowDao.getAllWindows() 存在)
                List<Windows> windows = windowDao.getAllWindows();

                // 2. 设置 WindowAdapter，使用 Fragment 自身作为监听器
                recyclerView.setAdapter(new WindowAdapter(windows, this));
                Toast.makeText(getContext(), "加载窗口列表...", Toast.LENGTH_SHORT).show();
                break;
            case DISH:
                // 1. 获取数据 (假设 DishDao.getAllDishes() 存在)
                List<Dish> dishes = dishDao.getAllDishes();

                // 2. 设置 DishAdapter，使用 Fragment 自身作为监听器
                recyclerView.setAdapter(new DishAdapter(dishes, this));
                Toast.makeText(getContext(), "加载菜品列表...", Toast.LENGTH_SHORT).show();
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