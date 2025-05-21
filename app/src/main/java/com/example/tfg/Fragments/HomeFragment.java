package com.example.tfg.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;

import com.bumptech.glide.Glide;
import com.example.tfg.Adapter.CategoryAdapter;
import com.example.tfg.Adapter.PopularAdapter;
import com.example.tfg.Adapter.SliderAdapter;
import com.example.tfg.Domain.BannerModel;
import com.example.tfg.R;
import com.example.tfg.ViewModel.MainViewModel;
import com.example.tfg.databinding.FragmentHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private FirebaseAuth mAuth;
    private MainViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        mAuth = FirebaseAuth.getInstance();
        displayUsername();
        initCategory();
        initSlider();
        initPopular();

        return binding.getRoot();
    }
    private void displayUsername() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
                binding.textView4.setText(currentUser.getDisplayName());
            } else if (currentUser.getEmail() != null) {
                String username = currentUser.getEmail().split("@")[0];
                binding.textView4.setText(username);
            } else {
                binding.textView4.setText(getString(R.string.username));
            }
            if (currentUser.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(currentUser.getPhotoUrl())
                        .circleCrop()  // para que sea circular (opcional)
                        .placeholder(R.drawable.ic_profile_placeholder) // imagen por defecto mientras carga
                        .into(binding.profileImageView);
            } else {
                // Imagen por defecto si no tiene foto
                binding.profileImageView.setImageResource(R.drawable.ic_profile_placeholder);
            }
        } else {
            binding.textView4.setText(getString(R.string.username));
            binding.profileImageView.setImageResource(R.drawable.ic_profile_placeholder);
        }
    }

    private void initCategory() {
        binding.progressBarCategory.setVisibility(View.VISIBLE);
        viewModel.loadCategory().observe(getViewLifecycleOwner(), categoryModels -> {
            if (categoryModels != null && !categoryModels.isEmpty()) {
                binding.categoryView.setLayoutManager(new LinearLayoutManager(
                        getContext(), LinearLayoutManager.HORIZONTAL, false));

                CategoryAdapter.OnCategoryClickListener listener = categoryName -> {
                    filterItemsByCategory(categoryName); // Aquí filtras
                };

                binding.categoryView.setAdapter(new CategoryAdapter(categoryModels, listener));
                binding.categoryView.setNestedScrollingEnabled(true);
            }
            binding.progressBarCategory.setVisibility(View.GONE);
        });
    }

    private void filterItemsByCategory(String categoryName) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        binding.progressBarPopular.setVisibility(View.VISIBLE);

        viewModel.loadItemsByCategoryExcludingUser(categoryName, currentUser.getUid())
                .observe(getViewLifecycleOwner(), itemsModels -> {
                    if (itemsModels != null) {
                        binding.popularView.setLayoutManager(new LinearLayoutManager(
                                getContext(), LinearLayoutManager.HORIZONTAL, false));
                        binding.popularView.setAdapter(new PopularAdapter(itemsModels));
                        binding.popularView.setNestedScrollingEnabled(true);
                    }
                    binding.progressBarPopular.setVisibility(View.GONE);
                });
    }



    private void initSlider() {
        binding.progressBarSlider.setVisibility(View.VISIBLE);
        viewModel.loadBanner().observe(getViewLifecycleOwner(), bannerModels -> {
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

        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer(new MarginPageTransformer(40));

        binding.viewPagerSlider.setPageTransformer(transformer);
    }

    private void initPopular() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        binding.progressBarPopular.setVisibility(View.VISIBLE);

        viewModel.loadItemsExcludingUser(currentUser.getUid())
                .observe(getViewLifecycleOwner(), itemsModels -> {
                    if (itemsModels != null && !itemsModels.isEmpty()) {
                        binding.popularView.setLayoutManager(new LinearLayoutManager(
                                getContext(), LinearLayoutManager.HORIZONTAL, false));
                        binding.popularView.setAdapter(new PopularAdapter(itemsModels));
                        binding.popularView.setNestedScrollingEnabled(true);
                    }
                    binding.progressBarPopular.setVisibility(View.GONE);
                });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
