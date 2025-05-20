package com.example.tfg.Respository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tfg.Domain.BannerModel;
import com.example.tfg.Domain.CategoryModel;
import com.example.tfg.Domain.ItemsModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class MainRespository {

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public LiveData<ArrayList<CategoryModel>> loadCategory() {
        MutableLiveData<ArrayList<CategoryModel>> listData = new MutableLiveData<>();
        firestore.collection("Category")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    ArrayList<CategoryModel> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        CategoryModel item = doc.toObject(CategoryModel.class);
                        list.add(item);
                    }
                    listData.setValue(list);
                });
        return listData;
    }

    public LiveData<ArrayList<BannerModel>> loadBanner() {
        MutableLiveData<ArrayList<BannerModel>> listData = new MutableLiveData<>();
        firestore.collection("Banner")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    ArrayList<BannerModel> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        BannerModel item = doc.toObject(BannerModel.class);
                        list.add(item);
                    }
                    listData.setValue(list);
                });
        return listData;
    }

    public LiveData<ArrayList<ItemsModel>> loadPopular() {
        MutableLiveData<ArrayList<ItemsModel>> listData = new MutableLiveData<>();
        firestore.collection("Items")
                .whereEqualTo("type", "popular") // Asegúrate de tener este campo en tus documentos
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    ArrayList<ItemsModel> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        ItemsModel item = doc.toObject(ItemsModel.class);
                        list.add(item);
                    }
                    listData.setValue(list);
                });
        return listData;
    }
}
