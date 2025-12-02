package com.example.Android_bigWork.Fragments;


import static com.example.Android_bigWork.Utils.RelativePopupWindow.makeDropDownMeasureSpec;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.widget.PopupWindowCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Android_bigWork.Activity.MainActivity;
import com.example.Android_bigWork.Adapters.CouponAdapter;
import com.example.Android_bigWork.Adapters.FoodCategoryAdapter;
import com.example.Android_bigWork.Adapters.FoodStickyAdapter;
import com.example.Android_bigWork.Adapters.ImageAdapter;
import com.example.Android_bigWork.Adapters.ShoppingCarAdapter;
import com.example.Android_bigWork.Database.CouponDao;
import com.example.Android_bigWork.Database.CouponDatabase;
import com.example.Android_bigWork.Database.DishDao;
import com.example.Android_bigWork.Database.DishDatabase;
import com.example.Android_bigWork.Database.FavoriteDao;
import com.example.Android_bigWork.Database.PersonDao;
import com.example.Android_bigWork.Database.PersonDatabase;
import com.example.Android_bigWork.Database.UserDishDao;
import com.example.Android_bigWork.Database.UserDishDatabase;
import com.example.Android_bigWork.Entity.Coupon;
import com.example.Android_bigWork.Entity.Dish;
import com.example.Android_bigWork.Entity.Person;
import com.example.Android_bigWork.Entity.PopularDish;
import com.example.Android_bigWork.Entity.UserDish;
import com.example.Android_bigWork.R;
import com.example.Android_bigWork.Utils.BaseDialog;
import com.example.Android_bigWork.Utils.PayPasswordDialog;
import com.example.Android_bigWork.Utils.RelativePopupWindow;
import com.example.Android_bigWork.Utils.StringUtil;
import com.example.Android_bigWork.ViewModels.OrderViewModel;
import com.hjq.xtoast.XToast;
import com.hjq.xtoast.draggable.SpringDraggable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import android.text.Editable; // 导入 Editable
import android.text.TextWatcher; // 导入 TextWatcher
import android.widget.EditText; // 导入 EditText
import java.util.Locale; // 导入 Locale (用于toLowerCase)

public class DishMenuFragment extends Fragment {

    private final String TAG = "my";

    // 布局控件
    private StickyListHeadersListView stickyListView;
    private ListView listView;
    // 【新增】
    private UserDishDao userDishDao;
    // 【新增】
    private UserDishDatabase userDishDatabase; // 假设 UserDishDao 属于 UserDishDatabase
    LinearLayout shoppingCar;
    Button payment;
    private String userName;
    //private Banner banner;
    private EditText searchEditText; // 【新增】搜索框成员变量

    // 界面数据(列表)
    private ArrayList<Dish> dishList;
    private ArrayList<FoodCategoryAdapter.CategoryItem> categoryItems;
    private ArrayList<UserDish> userDishList;
    double total;
    private OrderViewModel orderViewModel;
    private Coupon selectedCoupon;

    //数据库
    private DishDatabase dishDatabase;
    private DishDao dishDao;
    private PersonDatabase personDatabase;
    private PersonDao personDao;

    private CouponDatabase couponDatabase;
    private CouponDao couponDao;
    private Person user;//MainActivity中的用户信息
    private Button btnShowFavorites;
    private FavoriteDao favoriteDao; // 【新增】
    private boolean isShowingFavorites = false; // 【新增】: 记录当前是否在显示收藏夹
    private static final int CID_RECOMMEND = -1; // 【新增】推荐分类ID
    private static final int CID_POPULAR = -2;   // 【新增】热度排行分类ID
    public static DishMenuFragment newInstance() {
        return new DishMenuFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        //初始化数据库
        dishDatabase = DishDatabase.getDatabase(context);
        dishDao = dishDatabase.getDishDao();
        personDatabase = PersonDatabase.getDatabase(context);
        personDao = personDatabase.getPersonDao();
        couponDatabase = CouponDatabase.getDatabase(context);
        couponDao = couponDatabase.getCouponDao();
        favoriteDao = DishDatabase.getDatabase(context).getFavoriteDao(); // 【新增】
        // 【新增】: 初始化 UserDishDao
        //userDishDatabase = UserDishDatabase.getDatabase(context); // 假设你的数据库类名为 UserDishDatabase
        //userDishDao = userDishDatabase.userDishDao();
        userDishDao = dishDatabase.userDishDao();
        //获取MainActivity的Bundle数据
        Intent intent = ((Activity) context).getIntent();
        Bundle bundle = intent.getExtras();
        user = (Person) bundle.getSerializable("user");

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        initDishList();
        initCategoryItems();
        userDishList = new ArrayList<>();
        total = 0;
        selectedCoupon = null;
        return inflater.inflate(R.layout.fragment_dish_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // init ViewModel
        orderViewModel = new ViewModelProvider(requireActivity()).get(OrderViewModel.class);
        orderViewModel.getUserDishesForUser(user.username).observe(requireActivity(), new Observer<List<UserDish>>() {
            @Override
            public void onChanged(List<UserDish> userDishes) {
                Log.d(TAG, "userDishesObserver: data changed");
            }
        });

        // bind Views
        bindViews(view);
        // 【新增代码】: 搜索框文本变化监听器
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 将查询字符串转换为小写
                final String query = s.toString().toLowerCase(Locale.getDefault());

                // 1. 如果搜索框为空，恢复显示全部菜品
                if (query.isEmpty()) {
                    // 重新使用原始的 dishList 初始化 Adapter
                    FoodStickyAdapter originalAdapter = new FoodStickyAdapter(getContext(), DishMenuFragment.this, dishList, userDishList, user.username);
                    stickyListView.setAdapter(originalAdapter);

                    // 显示左侧分类栏
                    listView.setVisibility(View.VISIBLE);
                    // 重新设置分类栏 Adapter (为了同步)
                    FoodCategoryAdapter foodCategoryAdapter = new FoodCategoryAdapter(getContext(), categoryItems);
                    listView.setAdapter(foodCategoryAdapter);

                    // 【注意】: 需要重新设置分类栏的点击和滑动监听器（因为 Adapter 变了）
                    // 原始的 Adapter 监听器在 onViewCreated 后面会重新设置，这里保持简单，在下面步骤 4 中进行更正。
                } else {
                    // 2. 执行搜索过滤
                    ArrayList<Dish> filteredList = new ArrayList<>();
                    // 遍历原始菜品列表 (dishList)
                    for (Dish dish : dishList) {
                        // 核心：使用 dish.getName() 进行模糊匹配
                        if (dish.getName().toLowerCase(Locale.getDefault()).contains(query)) {
                            filteredList.add(dish);
                        }
                    }

                    // 3. 使用筛选后的列表创建新的 Adapter 并设置
                    FoodStickyAdapter filteredAdapter = new FoodStickyAdapter(getContext(), DishMenuFragment.this, filteredList, userDishList, user.username);
                    stickyListView.setAdapter(filteredAdapter);

                    // 4. 隐藏左侧分类栏
                    listView.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 菜品栏初始化
        final FoodStickyAdapter foodStickyAdapter = new FoodStickyAdapter(getContext(), this, dishList, userDishList, user.username);
        stickyListView.setAdapter(foodStickyAdapter);
        // 分类栏初始化
        final FoodCategoryAdapter foodCategoryAdapter = new FoodCategoryAdapter(getContext(), categoryItems);
        listView.setAdapter(foodCategoryAdapter);

// 菜品栏滑动监听
        stickyListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // 【新增修正代码】: 检查列表是否为空
                if (totalItemCount == 0 || stickyListView.getAdapter() == null || stickyListView.getAdapter().getCount() == 0) {
                    return; // 列表为空或 Adapter 未设置，直接返回，避免崩溃
                }
                // 提醒左栏变化
                int firstVisibleCID = ((Dish) stickyListView.getAdapter().getItem(firstVisibleItem)).getCID();
                foodCategoryAdapter.updateCategorySelectionByCID(firstVisibleCID);

            }
        });

// 类别栏按钮点击监听
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 获得点击类别的CID
                int selectedCID = ((FoodCategoryAdapter.CategoryItem) foodCategoryAdapter.getItem(position)).getCID();
                // 根据CID，获取右侧菜单中该类别的第一个菜品的位置
                int selectedPosition = foodStickyAdapter.getPositionByCID(selectedCID);
                // 根据位置，进行跳转
                stickyListView.setSelection(selectedPosition);
                Log.d(TAG, "onItemClick: click and set selection");
            }
        });

// 支付按钮点击事件
        payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 点击后生成确认对话框
                AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                builder.setTitle(getRString(R.string.confirm_to_pay));
                builder.setMessage(getRString(R.string.confirm_message));
                // 点击取消
                builder.setNegativeButton(getRString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "dialogNo: payment cancel");
                    }
                });
                // 点击确认
                builder.setPositiveButton(getRString(R.string.confirm), (dialogInterface, i) -> {
                    //获取当前购物车中的价格
                    double price = 0;
                    for (UserDish userDish : userDishList) {
                        price += userDish.getPrice();
                    }
                    if (price == 0) {
                        Toast.makeText(getContext(), "买点,多少买点", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //确认订单则弹出支付窗口
                    new PayPasswordDialog.Builder(requireActivity())
                            .setTitle(R.string.pay_title)
                            .setSubTitle(R.string.pay_sub_title)
                            .setMoney(StringUtil.getSSMoney(total, 72))// 设置订单金额
                            .setAutoDismiss(true)//支付满6位自动关闭
                            .setListener(new PayPasswordDialog.OnListener() {
                                @Override
                                public void onCompleted(BaseDialog dialog, String payPassword) {
                                    if (Integer.parseInt(payPassword) == user.payPassword) {
//                                        Toast.makeText(requireActivity(), getRString(R.string.pay_success), Toast.LENGTH_SHORT).show();
                                        //new XToast
                                        //获取MainActivity对象
                                        MainActivity mainActivity = (MainActivity) getActivity();
                                        //输出
                                        Log.d(TAG, "onCompleted: " + mainActivity);
                                        new XToast<>(requireActivity())
                                                .setContentView(R.layout.window_hint)
                                                .setDuration(1000)
                                                .setImageDrawable(android.R.id.icon, R.drawable.icon_success)
                                                .setText(R.string.pay_success)
                                                //设置动画效果
                                                .setAnimStyle(R.style.IOSAnimStyle)
                                                // 设置外层是否能被触摸
                                                .setOutsideTouchable(false)
                                                // 设置窗口背景阴影强度
                                                .setBackgroundDimAmount(0.5f)
                                                .show();
                                        // 为 userDishList 中所有菜品添加时间戳（订单生成时间），并插入数据库
                                        long currentTime = System.currentTimeMillis();
                                        for (UserDish ud : userDishList) {
                                            ud.setCreatedTime(currentTime);
                                            Log.d(TAG, "after payment: " + ud.display());
                                            orderViewModel.insert(ud);
                                        }
                                        // 支付后清空购物车
                                        clearShoppingCar();
                                        // 消耗优惠券
                                        if (selectedCoupon != null) {
                                            couponDao.deleteCoupon(selectedCoupon.CID);
                                            selectedCoupon = null;
                                        }
                                    } else {
//                                        Toast.makeText(requireActivity(), getRString(R.string.pay_fail), Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, "onPay: " + payPassword + " " + personDao.queryPayPassword(user.username));
                                        new XToast<>(requireActivity())
                                                .setContentView(R.layout.window_hint)
                                                .setDuration(1000)
                                                .setImageDrawable(android.R.id.icon, R.drawable.icon_error)
                                                .setText(R.string.pay_fail)
                                                //设置动画效果
                                                .setAnimStyle(R.style.IOSAnimStyle)
                                                // 设置外层是否能被触摸
                                                .setOutsideTouchable(false)
                                                // 设置窗口背景阴影强度
                                                .setBackgroundDimAmount(0.5f)
                                                .show();
                                    }
                                }

                                @Override
                                public void onCancel(BaseDialog dialog) {
                                    new XToast<>(requireActivity())
                                            .setContentView(R.layout.window_hint)
                                            .setDuration(1000)
                                            .setImageDrawable(android.R.id.icon, R.drawable.icon_warning)
                                            .setText(R.string.pay_cancel)
                                            //设置动画效果
                                            .setAnimStyle(R.style.IOSAnimStyle)
                                            // 设置外层是否能被触摸
                                            .setOutsideTouchable(false)
                                            // 设置窗口背景阴影强度
                                            .setBackgroundDimAmount(0.5f)
                                            .show();
                                }
                            })
                            .show();
                });
                builder.create().show();
            }

        });

        // 初始化购物车已购金额
        setShoppingCarAccount(0);

        // 设置购物车栏点击事件
        shoppingCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showShoppingCar();
            }
        });


// 类别栏按钮点击监听
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 获得点击类别的CID
                int selectedCID = ((FoodCategoryAdapter.CategoryItem) foodCategoryAdapter.getItem(position)).getCID();

                // 1. 处理特殊模式 (推荐/热度)
                if (selectedCID == CID_RECOMMEND || selectedCID == CID_POPULAR) {
                    // 如果 Adapter 已经是特殊 Adapter，则不重复设置（可选优化）
                    // 保持你原有的逻辑：执行数据库查询并显示特殊列表

                    if (selectedCID == CID_RECOMMEND) {
                        // 显示推荐列表 (保持原有逻辑)
                        showSpecialList(dishDao.getRecommendedDishes(), false);
                    } else { // CID_POPULAR
                        new Thread(() -> {
                            // 1. 【核心】: 从 DAO 获取数据 (现在是同步获取 List<PopularDish>)
                            List<PopularDish> popularDishes = userDishDao.getPopularDishes();

                            // 【新增调试代码】: 打印查询结果到 Logcat
                            if (popularDishes == null || popularDishes.isEmpty()) {
                                Log.e(TAG, "热度排行查询结果: 列表为空！");
                                // 订单项的 GID 检查已经不需要了，如果 SQL 语句正确，这里为空说明 dish_table 为空
                            } else {
                                Log.d(TAG, "热度排行查询结果: 列表大小=" + popularDishes.size());
                                for(PopularDish pd : popularDishes) {
                                    Log.d(TAG, "热度排行菜品: GID=" + pd.GID + ", Name=" + pd.name + ", Sales=" + pd.totalSales);
                                }
                            }
                            // 【结束调试代码】

                            // 2. 将 List<PopularDish> 转换为 List<Dish> 供 FoodStickyAdapter 使用
                            ArrayList<Dish> dishesToShow = new ArrayList<>();
                            if (popularDishes != null) {
                                for (PopularDish pd : popularDishes) {
                                    // 实例化 Dish 并赋值
                                    Dish dish = new Dish(pd.GID, pd.name, pd.description, pd.price, pd.category, pd.CID, pd.spicy, pd.sweet,pd.windowId,pd.imageUrl,pd.isAvailable,pd.remainingStock);
                                    // 假设 Dish 中有 setTotalSales 方法，并调用它来存储销量
                                    dish.setTotalSales(pd.totalSales);
                                    dishesToShow.add(dish);
                                }
                            }

                            // 3. 回到主线程更新 UI
                            requireActivity().runOnUiThread(() -> {
                                showSpecialList(dishesToShow, false);
                                Toast.makeText(getContext(), "已显示热度排行列表", Toast.LENGTH_SHORT).show();
                            });
                        }).start();
                    }
                }

                // 2. 处理普通分类点击 (CID > 0)
                else {
                    // 【核心修正】：如果当前 Adapter 不是完整的 FoodStickyAdapter，先恢复它！
                    // 只有当 stickyListView 的 Adapter 不是 foodStickyAdapter 时才需要恢复
                    if (stickyListView.getAdapter() != foodStickyAdapter) {
                        stickyListView.setAdapter(foodStickyAdapter); // 恢复为完整的菜单 Adapter
                        // 恢复左侧分类栏的可见性（如果你在特殊模式下隐藏了）
                        listView.setVisibility(View.VISIBLE);
                    }

                    // 【原有逻辑】：执行跳转
                    int selectedPosition = foodStickyAdapter.getPositionByCID(selectedCID);

                    // 确保跳转位置不越界
                    if (selectedPosition >= 0 && selectedPosition < foodStickyAdapter.getCount()) {
                        stickyListView.setSelection(selectedPosition);
                        Log.d(TAG, "onItemClick: click and set selection");
                    } else {
                        // 找不到分类起始位置，可能分类里没有菜
                        Toast.makeText(getContext(), "该分类暂无菜品", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    /**
     * 显示特殊列表 (推荐/热度排行)
     *
     * @param specialDishList 要显示的 Dish 列表
     * @param hideCategoryList 是否隐藏左侧分类列表
     */
    private void showSpecialList(List<Dish> specialDishList, boolean hideCategoryList) {
        // 1. 创建新的 Adapter (使用特殊列表)
        FoodStickyAdapter specialAdapter = new FoodStickyAdapter(getContext(), this, (ArrayList<Dish>) specialDishList, userDishList, user.username);
        stickyListView.setAdapter(specialAdapter);

        // 2. 隐藏或显示左侧分类栏
        listView.setVisibility(hideCategoryList ? View.GONE : View.VISIBLE);

        // 3. 确保左侧分类栏的高亮状态正确 (可选)
        // foodCategoryAdapter.updateCategorySelectionByCID(CID_RECOMMEND/CID_POPULAR);
    }
    /**
     * 更新购物车已购金额、
     *
     * @return void
     * @Author Bubu
     * @date 2022/10/14 21:03
     * @commit
     */
    public void updateShoppingCarAccount() {
        double total = 0;
        for (UserDish ud : userDishList) {
            total += ud.getPrice();
        }
        this.total = total;
        setShoppingCarAccount(total);
    }

    /**
     * 设置购物车已购金额
     *
     * @param money 设置的金额
     * @return void
     * @Author Bubu
     * @date 2022/10/14 19:55
     * @commit
     */
    public void setShoppingCarAccount(double money) {
        TextView totalAccount = shoppingCar.findViewById(R.id.account_in_car);
        if (selectedCoupon == null || money < 0.01) {
            totalAccount.setText(StringUtil.getSSMoney(money, 72));
        } else {
            totalAccount.setText(StringUtil.getSSMoneyAfterDiscount(money, 72, selectedCoupon));
            switch (selectedCoupon.getType()) {
                case 0:
                    this.total = selectedCoupon.getDiscount() * this.total / 10;
                    break;
                case 1:
                    if (this.total >= selectedCoupon.getCondition()) {
                        this.total -= selectedCoupon.getReduction();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 绑定视图
     *
     * @param view
     * @return void
     * @Author Bubu
     * @date 2022/10/12 20:51
     * @commit none
     */
    private void bindViews(View view) {
        stickyListView = view.findViewById(R.id.showdishes);
        listView = view.findViewById(R.id.category_list);
        payment = view.findViewById(R.id.shopping_commit);
        shoppingCar = view.findViewById(R.id.shopping_car);
        searchEditText = view.findViewById(R.id.edittext_search); // 【修改】绑定新 ID 的搜索框
        btnShowFavorites = view.findViewById(R.id.btn_show_favorites); // 假设你添加了这个按钮

        // 【新增】: 收藏夹按钮点击事件
        btnShowFavorites.setOnClickListener(v -> showFavoriteList());
        redPackInit();
    }
    // 【新增方法】: 显示收藏列表
    private void showFavoriteList() {
        // 切换状态
        isShowingFavorites = !isShowingFavorites;

        if (isShowingFavorites) {
            // ========== 模式：显示收藏夹 ==========
            new Thread(() -> {
                // 1. 获取收藏的菜品列表
                List<Dish> favoriteDishes = favoriteDao.getFavoriteDishes(user.username);

                requireActivity().runOnUiThread(() -> {
                    // 2. 用收藏列表更新 Adapter
                    FoodStickyAdapter adapter = new FoodStickyAdapter(getContext(), this, (ArrayList<Dish>) favoriteDishes, userDishList, user.username);
                    stickyListView.setAdapter(adapter);

                    // 3. 隐藏左侧分类栏
                    listView.setVisibility(View.GONE);

                    Toast.makeText(getContext(), "已显示收藏夹列表", Toast.LENGTH_SHORT).show();
                });
            }).start();

        } else {
            // ========== 模式：退出收藏夹，恢复完整菜单 ==========
            // 1. 恢复 dishList
            initDishList(); // 重新从数据库加载完整菜品列表 (确保 dishList 是完整的)

            // 2. 恢复 Adapter
            FoodStickyAdapter adapter = new FoodStickyAdapter(getContext(), this, dishList, userDishList, user.username);
            stickyListView.setAdapter(adapter);

            // 3. 恢复左侧分类栏
            listView.setVisibility(View.VISIBLE);
            // 重新设置分类栏 Adapter
            FoodCategoryAdapter foodCategoryAdapter = new FoodCategoryAdapter(getContext(), categoryItems);
            listView.setAdapter(foodCategoryAdapter);

            Toast.makeText(getContext(), "已恢复完整菜单", Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * 初始化红包
     *
     * @return void
     * @Author Anduin9527
     * @date 2022/10/29 10:18
     * @commit
     */
    private void redPackInit() {
        //计数器
        final int[] count = {0};
        new XToast<>(requireActivity())
                .setContentView(R.layout.window_redpack)
                .setAnimStyle(R.style.IOSAnimStyle)
                .setImageDrawable(android.R.id.icon, R.drawable.redpack)
                // 设置成可拖拽的
                .setDraggable(new SpringDraggable())
                .setOnClickListener(android.R.id.icon, new XToast.OnClickListener<ImageView>() {
                    @Override
                    public void onClick(final XToast<?> toast, ImageView view) {
                        new XToast<>(requireActivity())
                                .setContentView(R.layout.dialog_red_packet)
                                .setAnimStyle(R.style.IOSAnimStyle)
                                .setOnClickListener(R.id.iv_close, new XToast.OnClickListener<ImageView>() {
                                    @Override
                                    public void onClick(XToast<?> toast, ImageView view) {
                                        toast.cancel();
                                        count[0] -= 1;
                                    }
                                })
                                .setOnClickListener(R.id.iv_open, new XToast.OnClickListener<ImageView>() {
                                    @Override
                                    public void onClick(XToast<?> toast, ImageView view) {
                                        //获取id为R.id.iv_open的ImageView
                                        AnimationDrawable animationDrawable = (AnimationDrawable) view.getBackground();
                                        animationDrawable.start();
                                        //生成优惠券
                                        String couponText = geneCoupon();
                                        toast.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                //new XToast 显示领取成功
                                                new XToast<>(requireActivity())
                                                        .setDuration(2000)
                                                        .setContentView(R.layout.window_hint)
                                                        .setAnimStyle(R.style.IOSAnimStyle)
                                                        .setImageDrawable(android.R.id.icon, R.drawable.yanhua)
                                                        .setText(android.R.id.message, getRString(R.string.successfullyReceived)
                                                                + "\n" +
                                                                couponText
                                                                + " " + getRString(R.string.coupon))
                                                        .show();
                                                toast.cancel();
                                            }
                                        }, 900);
                                        Log.d(TAG, "redPack: " + couponDao.getAllCoupon(user.username));
                                    }
                                })
                                .show();
                        count[0] += 1;
                        if (count[0] == 3) {
                            toast.cancel();
                        }
                    }
                })
                .show();
        //查询用户目前拥有的优惠券
        Log.d(TAG, "redPackInit: " + couponDao.getAllCoupon(user.username));
    }

    /**
     * 随机生成优惠券，并插入数据库
     *
     * @return String
     * @Author Anduin9527
     * @date 2022/10/18 20:47
     * @commit
     */
    private String geneCoupon() {
        //随机生成优惠卷
        //生成优惠券类型0~1
        String couponText = "";
        double condition = 0, reduction = 0, discount = 0;
        int type = (int) (Math.random() * 2);
        boolean isChinese = false;
        String language = Locale.getDefault().getLanguage();
        if (language.equals("CN") || language.equals("zh")) {
            isChinese = true;
        }
        if (type == 1) {
            condition = (int) (Math.random() * 100) + 1;
            reduction = (int) (Math.random() * condition * 0.7) + 1;
            if (isChinese) {
                couponText = "满" + condition + "减" + reduction;
            } else {
                couponText = "Over " + condition + " Minus " + reduction;
            }
        } else {
            discount = (int) (Math.random() * 4) + 2;
            if (isChinese) {
                couponText = discount + "折";
            } else {
                couponText = (10 - discount) * 10 + "% OFF";
            }
        }
        //插入数据库

        couponDao.addCoupon(user.username, type, discount, condition, reduction);

        return couponText;
    }

    /**
     * 获取string中的属性值
     *
     * @param id
     * @return String
     * @Author Anduin9527
     * @date 2022/10/12 8:29
     * @commit
     */
    private String getRString(@StringRes int id) {
        return getResources().getString(id);
    }

    /**
     * 测试用：初始化添加商品列表
     *
     * @return void
     * @description
     * @Author Bubu
     * @date 2022/10/12 17:45
     * @commit
     */
    private void initDishList() {
        Resources r = getResources();
        //连接数据库
        dishList = new ArrayList<>();
        dishDatabase = DishDatabase.getDatabase(getContext());
        DishDao dishDao = dishDatabase.getDishDao();

        //获取数据库中的菜品
        dishList = (ArrayList<Dish>) dishDao.getAllDish();

        // 🔴 关键新增 Log：打印 dish_table 中的菜品总数
        int dishCount = dishDao.getDishCount();
        Log.w(TAG, "🔍 dish_table 菜品总数: " + dishCount);
        // 🔴 关键新增 Log：打印所有菜品列表（包括 GID）
        for (Dish dish : dishList) {
            Log.w(TAG, "🔍 Dish in dish_table: GID=" + dish.getGID() + ", Name=" + dish.getName());
        }

        //输出内容（原有 Log，可以保留）
        for (Dish dish : dishList) {
            Log.d(TAG, "initDishListForTest: " + dish.toString());
        }
    }

    /**
     * 初始化类别列表，从商品列表中提取分类
     *
     * @return void
     * @Author Bubu
     * @date 2022/10/13 0:51
     * @commit
     */
    private void initCategoryItems() {
        categoryItems = null;
        // 1. 【新增】初始化 categoryItems (确保它不是 null)
        if (categoryItems == null) {
            categoryItems = new ArrayList<>();
        } else {
            categoryItems.clear(); // 清空，确保每次只初始化一次
        }

        // 2. 【新增】手动添加新的特殊分类
        // 推荐分类 (CID = -1)
        categoryItems.add(new FoodCategoryAdapter.CategoryItem("餐品推荐", CID_RECOMMEND));
        // 热度排行分类 (CID = -2)
        categoryItems.add(new FoodCategoryAdapter.CategoryItem("热度排行", CID_POPULAR));

        // 遍历菜单列表，如果该菜品所属类别尚未添加到类别列表中，则将此菜品的类别添加。
        dishList.forEach(dish -> {
            // 若类别列表为空，则直接添加
            if (categoryItems == null) {
                categoryItems = new ArrayList<>();
                categoryItems.add(new FoodCategoryAdapter.CategoryItem(dish.getCategory(), dish.getCID()));
            }
            // 若不为空，则遍历类别列表，若无此类，则添加
            else {
                boolean addCategory = true;
                for (int i = 0; i < categoryItems.size(); i++) {
                    if (dish.getCID() == categoryItems.get(i).getCID()) {
                        addCategory = false;
                        break;
                    }
                }
                if (addCategory) {
                    categoryItems.add(new FoodCategoryAdapter.CategoryItem(dish.getCategory(), dish.getCID()));
                }
            }
        });
    }

    /**
     * 显示购物车
     *
     * @return void
     * @Author Bubu
     * @date 2022/10/12 17:45
     * @commit
     */
    public void showShoppingCar() {
        RelativePopupWindow shoppingCar = new RelativePopupWindow(getContext());
        // 绑定视图
        View contentView = shoppingCar.getContentView();
        Button button = contentView.findViewById(R.id.clear_shopping);
        RecyclerView shoppingList = contentView.findViewById(R.id.shopping_list);
        Spinner selectCoupon = contentView.findViewById(R.id.spinner_coupon);
        // 设置 RecyclerView
        shoppingList.setLayoutManager(new LinearLayoutManager(requireActivity()));
        ShoppingCarAdapter shoppingCarAdapter = new ShoppingCarAdapter(getContext(), this, userDishList, dishList);
        shoppingList.setAdapter(shoppingCarAdapter);
        // 设置优惠券下拉框 Spinner
        List<Coupon> coupons = couponDao.getAllCoupon(user.username);
        CouponAdapter couponAdapter = new CouponAdapter(getContext(), coupons);
        selectCoupon.setAdapter(couponAdapter);
        /*初始化用户选择的优惠券*/
        int position = -1;
        for (int i = 0; i < coupons.size(); i++) {
            if (selectedCoupon != null && selectedCoupon.getCID() == coupons.get(i).getCID()) {
                position = i;
            }
        }
        if (position > -1) {
            selectCoupon.setSelection(position);
        }
        // 设置下拉框选项的点击事件
        selectCoupon.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCoupon = coupons.get(position);
                String couponString = selectedCoupon.toString();
                Log.d(TAG, "onCouponItemClick: select " + couponString);
                updateShoppingCarAccount();
        }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //需要先测量PopupWindow的宽高
        contentView.measure(makeDropDownMeasureSpec(shoppingCar.getWidth()),
                makeDropDownMeasureSpec(shoppingCar.getHeight()));
        // 计算偏移量
        int offsetX = -contentView.getMeasuredWidth();
        // int offsetY = (contentView.getMeasuredHeight() + payment.getHeight());
        int offsetY = 0;
        // 设置显隐动画
        shoppingCar.setAnimationStyle(R.style.shoppingCar_anim_style);
        // 显示购物车弹窗
        PopupWindowCompat.showAsDropDown(shoppingCar, payment, offsetX, offsetY, Gravity.END);
        Log.d(TAG, "showShoppingCar: X,Y=" + offsetX + "," + offsetY);
        // 设置"清空"按钮的点击事件
        button.setOnClickListener(v -> {
            Log.d(TAG, "onClick: 清空");
            // 清空购物车
            clearShoppingCar();
            // 更新购物车
            shoppingList.getAdapter().notifyDataSetChanged();
        });
    }


    /**
     * 清空购物车
     *
     * @return void
     * @Author Bubu
     * @date 2022/10/26 13:47
     * @commit
     */
    public void clearShoppingCar() {
        Log.d(TAG, "clear the shopping car!");
        userDishList.clear();
        // 将选择的份数清零
        for (Dish dish : dishList) {
            if (dish.getCount() > 0) {
                dish.setCount(0);
            }
        }
        // 更新菜单列表
        ((FoodStickyAdapter) stickyListView.getAdapter()).notifyDataSetChanged();
        // 更新购物车
        updateShoppingCarAccount();
    }

    public ArrayList<Dish> getDishList() {
        return dishList;
    }

    public void setDishList(ArrayList<Dish> dishList) {
        this.dishList = dishList;
    }

    public ArrayList<UserDish> getUserDishList() {
        return userDishList;
    }

    public void setUserDishList(ArrayList<UserDish> userDishList) {
        this.userDishList = userDishList;
    }

    public StickyListHeadersListView getStickyListView() {
        return stickyListView;
    }

    public void setStickyListView(StickyListHeadersListView stickyListView) {
        this.stickyListView = stickyListView;
    }
    public interface OnDishCountChangeListener {
        void onDishCountChanged(Dish dish);
    }

    // 【新增】: DishDetailFragment 需要调用的回调实现
    public void onDishCountChanged(Dish dish) {
        // 1. 刷新菜单列表 (主列表)
        // 假设 stickyListView.getAdapter() 是 FoodStickyAdapter
        if (stickyListView.getAdapter() != null) {
            ((FoodStickyAdapter) stickyListView.getAdapter()).notifyDataSetChanged();
        }

        // 2. 刷新购物车总价和内容
        // ❗ 注意：你需要在这里重新计算 userDishList 并调用 updateShoppingCarAccount()
        // 由于你原来的 FoodStickyAdapter 中有 addDishToShoppingCar, removeSingleDishFromShoppingCar 等方法，
        // 你需要将这些方法也移动到 DishMenuFragment 中，并在这里调用它们来更新 userDishList。

        // 简单快速处理：强制重新计算总价和刷新 UI
        updateShoppingCarAccount();
    }

    public void removeSingleDishFromShoppingCar(Dish dish) {
        for(UserDish ud:userDishList){
            if(ud.getGID()==dish.getGID()){
                userDishList.remove(ud);
                break;
            }
        }
        //updateShoppingCarAccount();
        updateShoppingCarAccount();
    }

    public void addDishToShoppingCar(Dish dish, int spicy, int sweet,String customText) {
        UserDish userDish = new UserDish(
                dish.getGID(),
                dish.getName(),
                dish.getDescription(),
                dish.getPrice(),
                dish.getCategory(),
                dish.getCID(),
                spicy,
                sweet,
                customText,
                1,
                user.username);
        // 如果购物车没有菜，直接添加
        if (userDishList.size() == 0) {
            userDishList.add(userDish);
        }
        // 如果有菜，判断是否有相同的。有则数量、价格改变；没有则添加新菜
        else {
            boolean existSameUserDish = false;
            for (UserDish ud : userDishList) {
                if (ud.equals(userDish)) {
                    existSameUserDish = true;
                    ud.setCount(ud.getCount() + 1);
                    ud.setPrice(ud.getPrice() + userDish.getPrice());
                    break;
                }
            }
            if (!existSameUserDish) {
                userDishList.add(userDish);
            }
        }
        //setUserDishList(userDishList);
        Log.d(TAG, "addDishToShoppingCar: userDishList length=" + userDishList.size());
        updateShoppingCarAccount();
    }
    public void onDishCountChanged() {
        // 【核心】：刷新菜单列表 (主列表)
        if (stickyListView.getAdapter() != null) {
            ((FoodStickyAdapter) stickyListView.getAdapter()).notifyDataSetChanged();
        }
        // 【核心】：刷新购物车总价 (在 add/remove 方法中已经调用，这里再次调用确保安全)
        updateShoppingCarAccount();
    }
}

