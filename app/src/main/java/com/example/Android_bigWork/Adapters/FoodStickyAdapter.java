package com.example.Android_bigWork.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.example.Android_bigWork.Entity.Dish;
import com.example.Android_bigWork.Entity.UserDish;
import com.example.Android_bigWork.Fragments.DishMenuFragment;
import com.example.Android_bigWork.R;
import com.example.Android_bigWork.Utils.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

import android.app.Activity; // 导入 Activity
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity; // 导入 AppCompatActivity
import com.example.Android_bigWork.Fragments.DishDetailFragment; // 导入详情 Fragment
import com.example.Android_bigWork.Entity.Dish; // 导入 Dish

/**
 * @Type FoodStickyAdapter
 * @Desc 用于菜品显示的适配器
 * @author Bubu
 * @date 2022/10/29 11:55
 * @version
 */
public class FoodStickyAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    private static final String TAG = "my";
    private Context context;
    private LayoutInflater inflater;
    private ArrayList<Dish> dishList;
    private Resources resources;
    private ArrayList<UserDish> userDishList;
    private String userName;
    private DishMenuFragment dishMenuFragment;

    static class ViewHolder {
        TextView name;
        TextView price;
        TextView count;
        ImageButton add;
        ImageButton sub;
        ImageView img;
        CardView dishCardView;

        public ViewHolder(View view) {
            this.name = view.findViewById(R.id.dish_name);
            this.price = view.findViewById(R.id.dish_price);
            this.add = view.findViewById(R.id.dish_add);
            this.sub = view.findViewById(R.id.dish_sub);
            this.img = view.findViewById(R.id.dish_img);
            this.dishCardView = view.findViewById(R.id.dish_cardView);
            this.count = view.findViewById(R.id.dish_count);
        }
    }

    static class HeaderViewHolder {
        TextView category;

        public HeaderViewHolder(View view) {
            this.category = view.findViewById(R.id.dish_category);
        }
    }

    public FoodStickyAdapter(Context context, DishMenuFragment dishMenuFragment, ArrayList<Dish> dishList, ArrayList<UserDish> userDishList, String userName) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.dishList = dishList;
        this.resources = context.getResources();
        this.userDishList = userDishList;
        this.userName = userName;
        this.dishMenuFragment = dishMenuFragment;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder headerViewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.category_header, null);
            headerViewHolder = new HeaderViewHolder(convertView);
            convertView.setTag(headerViewHolder);
        } else {
            headerViewHolder = (HeaderViewHolder) convertView.getTag();
        }
        headerViewHolder.category.setText(StringUtil.replaceToBlank(dishList.get(position).getCategory()));
        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        return this.dishList.get(position).getCID();
    }

    @Override
    public int getCount() {
        return dishList.size();
    }

    @Override
    public Object getItem(int position) {
        return dishList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return dishList.get(position).getGID();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_dish, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        // 在视图上设置文本、图片
        Dish dish = dishList.get(position);
        holder.name.setText(dish.getName());
//        holder.price.setText(String.valueOf(dish.getPrice()));
        holder.price.setText(StringUtil.getSSMoney(dish.getPrice(), 54));
        holder.count.setText(String.valueOf(dish.getCount()));
        holder.img.setImageResource(resources.getIdentifier("dish_" + dish.getGID(), "drawable", "com.example.Android_bigWork"));
        // 加号点击事件
        holder.add.setOnClickListener(v -> {
            showDishDetail(dish);
        });
        // 减号点击事件
        holder.sub.setOnClickListener(v -> {
            if (dish.getCount() == 1) {
                // 数据层-1
                dish.setCount(dish.getCount() - 1);
                // 视图层-1
//                TextView count_sub = contentView.findViewById(R.id.dish_count);
                holder.count.setText(String.valueOf(dish.getCount()));
                // 从购物车中移除
                removeSingleDishFromShoppingCar(dish);
                // 通知视图改变
                notifyDataSetChanged();
            }
            if (dish.getCount() > 1) {
                dishMenuFragment.showShoppingCar();
            }

        });
        // 菜品卡片点击事件
        holder.dishCardView.setOnClickListener(v -> {
            showDishDetail(dish);
        });

        return convertView;
    }

    /**
     * 【修改后的新逻辑】: 启动 DishDetailFragment 来显示详情/点餐/评论
     *
     * @param dish
     * @return void (不再返回 PopupWindow)
     */
    private void showDishDetail(Dish dish) { // 【注意】: 返回类型从 PopupWindow 变更为 void
        // Context 转换成 Activity 以获取 FragmentManager
        if (context instanceof AppCompatActivity) {
            // 1. 创建新的 DialogFragment 实例
            DishDetailFragment detailFragment = DishDetailFragment.newInstance(dish);

            // 2. 设置为底部弹出样式（可选，但通常 DialogFragment 更灵活）
            // detailFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme);

            // 3. 显示底部弹出对话框
            detailFragment.show(
                    ((AppCompatActivity) context).getSupportFragmentManager(),
                    "DishDetailDialog"
            );
        } else {
            Toast.makeText(context, "无法打开详情页", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeSingleDishFromShoppingCar(Dish dish) {
        for(UserDish ud:userDishList){
            if(ud.getGID()==dish.getGID()){
                userDishList.remove(ud);
                break;
            }
        }
        dishMenuFragment.updateShoppingCarAccount();
    }

    public int getPositionByCID(int CID) {
        for (int i = 0; i < dishList.size(); i++) {
            Dish dish = dishList.get(i);
            if (CID == dish.getCID()) {
                return i;
            }
        }
        return 0;
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
                userName);
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
        dishMenuFragment.setUserDishList(userDishList);
        Log.d(TAG, "addDishToShoppingCar: userDishList length=" + userDishList.size());
    }

    int transformDishGID(Dish dish, int spicy, int sweet) {
        return dish.getGID() * 100 + spicy * 10 + sweet;
    }

    public Resources getResources() {
        return resources;
    }

    private String getRString(int id) {
        return getResources().getString(id);
    }
}
