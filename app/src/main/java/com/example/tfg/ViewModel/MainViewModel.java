package com.example.tfg.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.tfg.Domain.BannerModel;
import com.example.tfg.Domain.CategoryModel;
import com.example.tfg.Domain.ItemsModel;
import com.example.tfg.Respository.MainRespository;

import java.util.ArrayList;

public class MainViewModel extends ViewModel {
    private final MainRespository respository= new MainRespository();

    public LiveData<ArrayList<CategoryModel>> loadCategory(){
         return respository.loadCategory();
    }

    public LiveData<ArrayList<BannerModel>> loadBanner(){
        return respository.loadBanner();
    }

    public LiveData<ArrayList<ItemsModel>> loadPopular(){
        return respository.loadPopular();
    }
}
