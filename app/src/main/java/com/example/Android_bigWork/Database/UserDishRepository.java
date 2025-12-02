package com.example.Android_bigWork.Database;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.Android_bigWork.Entity.PopularDish;
import com.example.Android_bigWork.Entity.UserDish;

import java.util.List;
import com.example.Android_bigWork.Database.DishDatabase; // 【新增导入】
public class UserDishRepository {

    private UserDishDao userDishDao;
    private LiveData<List<UserDish>> userDishes;

    public UserDishRepository(Application application) {
        //UserDishDatabase db=UserDishDatabase.getDatabase(application);
        DishDatabase db = DishDatabase.getDatabase(application);
        userDishDao = db.userDishDao();
        userDishes= userDishDao.getUserDishesFromAllUsers();
    }

    public LiveData<List<UserDish>> getUserDishes(){
        return userDishes;
    }

    public LiveData<List<UserDish>> getUserDishesForUser(String userName){
        return userDishDao.getUserDishesForUser(userName);
    }

    public LiveData<List<UserDish>> getUserDishesForUserByTime(String userName,long time){
        return userDishDao.getUserDishesForUserByTime(userName,time);
    }

    public void insert(UserDish userDish){
        DishDatabase.DatabaseExecutor.execute(()->{
            userDishDao.insert(userDish);
        });
    }

    public List<PopularDish> getPopularDishes() {
        // 直接从 DAO 获取 LiveData<List<PopularDish>>
        return userDishDao.getPopularDishes();
    }
}
