package com.example.tfg.ViewModel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tfg.Domain.BannerModel;
import com.example.tfg.Domain.CategoryModel;
import com.example.tfg.Domain.ItemsModel;
import com.example.tfg.Respository.MainRespository;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class MainViewModel extends ViewModel {
    private final MainRespository respository = new MainRespository();

    public LiveData<ArrayList<CategoryModel>> loadCategory() {
        return respository.loadCategory();
    }

    public LiveData<ArrayList<BannerModel>> loadBanner() {
        return respository.loadBanner();
    }

    public LiveData<ArrayList<ItemsModel>> loadPopular() {
        return respository.loadPopular();
    }

    public LiveData<ArrayList<ItemsModel>> loadItemsByUser(String userId) {
        MutableLiveData<ArrayList<ItemsModel>> userItemsLiveData = new MutableLiveData<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Items")
                .whereEqualTo("ownerId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<ItemsModel> userItems = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        ItemsModel item = doc.toObject(ItemsModel.class);
                        item.setId(doc.getId()); // 🔥 ESTA LÍNEA ES CRUCIAL
                        userItems.add(item);
                    }
                    userItemsLiveData.setValue(userItems);
                });

        return userItemsLiveData;
    }


    public LiveData<ArrayList<ItemsModel>> loadItemsExcludingUser(String userId) {
        MutableLiveData<ArrayList<ItemsModel>> otherItemsLiveData = new MutableLiveData<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<ItemsModel> otherItems = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        ItemsModel item = doc.toObject(ItemsModel.class);
                        if (item.getOwnerId() != null && !item.getOwnerId().equals(userId)) {
                            otherItems.add(item);
                        }
                    }
                    otherItemsLiveData.setValue(otherItems);
                });

        return otherItemsLiveData;
    }
    public LiveData<ArrayList<ItemsModel>> loadItemsByCategoryExcludingUser(String category, String currentUserId) {
        MutableLiveData<ArrayList<ItemsModel>> itemsData = new MutableLiveData<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Items")
                .whereArrayContains("categorias", category)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<ItemsModel> itemList = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        ItemsModel item = doc.toObject(ItemsModel.class);
                        if (item.getOwnerId() != null && !item.getOwnerId().equals(currentUserId)) {
                            item.setId(doc.getId());
                            itemList.add(item);
                        }
                    }
                    itemsData.setValue(itemList);
                })
                .addOnFailureListener(e -> itemsData.setValue(null));

        return itemsData;
    }


}