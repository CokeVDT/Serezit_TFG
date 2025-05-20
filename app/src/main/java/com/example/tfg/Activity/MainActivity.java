package com.example.tfg.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;

import com.example.tfg.Adapter.CategoryAdapter;
import com.example.tfg.Adapter.PopularAdapter;
import com.example.tfg.Adapter.SliderAdapter;
import com.example.tfg.Domain.BannerModel;
import com.example.tfg.R;
import com.example.tfg.ViewModel.MainViewModel;
import com.example.tfg.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        viewModel = new MainViewModel();

        displayUsername();

        initCategory();
        initSlider();
        initPopular();
        bottomNavigation();
    }

    private void displayUsername() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Primero intenta con displayName
            if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
                binding.textView4.setText(currentUser.getDisplayName());
                Log.d("USERNAME", "Nombre mostrado: " + currentUser.getDisplayName());
            }
            // Si no hay displayName, usa el email (sin el @)
            else if (currentUser.getEmail() != null) {
                String username = currentUser.getEmail().split("@")[0];
                binding.textView4.setText(username);
                Log.d("USERNAME", "Email usado como nombre: " + username);
            }
            // Como último recurso, usa el string de recursos
            else {
                binding.textView4.setText(getString(R.string.username));
                Log.w("USERNAME", "Usando nombre por defecto");
            }
        } else {
            // Esto no debería ocurrir porque MainActivity debe ser protegida
            Log.e("USERNAME", "Usuario nulo en MainActivity");
            finish(); // Cierra la actividad si no hay usuario
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Verificar si el usuario está logueado
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            displayUsername();
        }
    }
    private void bottomNavigation() {
        binding.bottomNavigation.setItemSelected(R.id.home, true);
        binding.bottomNavigation.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                // Manejar la selección de items del menú
            }
        });

    }

    private void initPopular() {
        binding.progressBarPopular.setVisibility(View.VISIBLE);
        viewModel.loadPopular().observe(this, itemsModels -> {
            if (itemsModels != null && !itemsModels.isEmpty()) {
                binding.popularView.setLayoutManager(new LinearLayoutManager(
                        MainActivity.this, LinearLayoutManager.HORIZONTAL, false));
                binding.popularView.setAdapter(new PopularAdapter(itemsModels));
                binding.popularView.setNestedScrollingEnabled(true);
            }
            binding.progressBarPopular.setVisibility(View.GONE);
        });
    }


    private void initSlider() {
        binding.progressBarSlider.setVisibility(View.VISIBLE);
        viewModel.loadBanner().observe(this, bannerModels -> {
            if (bannerModels != null && !bannerModels.isEmpty()) {
                banners(bannerModels);
            }
            binding.progressBarSlider.setVisibility(View.GONE);
        });
    }


    private void banners(ArrayList<BannerModel> bannerModels) {
        binding.viewPagerSlider.setAdapter(new SliderAdapter(bannerModels, binding.viewPagerSlider));
        binding.viewPagerSlider.setClipToPadding(false);
        binding.viewPagerSlider.setClipChildren(false);
        binding.viewPagerSlider.setOffscreenPageLimit(3);
        binding.viewPagerSlider.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));

        binding.viewPagerSlider.setPageTransformer(compositePageTransformer);
    }

    private void initCategory() {
        binding.progressBarCategory.setVisibility(View.VISIBLE);
        viewModel.loadCategory().observe(this, categoryModels -> {
            if (categoryModels != null) {
                binding.categoryView.setLayoutManager(new LinearLayoutManager(
                        MainActivity.this, LinearLayoutManager.HORIZONTAL, false));
                binding.categoryView.setAdapter(new CategoryAdapter(categoryModels));
                binding.categoryView.setNestedScrollingEnabled(true);
            }
            binding.progressBarCategory.setVisibility(View.GONE);
        });
    }


}