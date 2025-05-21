package com.example.tfg.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.tfg.Activity.AddProductActivity;
import com.example.tfg.Activity.LoginActivity;
import com.example.tfg.Adapter.PopularAdapter;
import com.example.tfg.R;
import com.example.tfg.ViewModel.MainViewModel;
import com.example.tfg.databinding.FragmentProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseAuth mAuth;
    private MainViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        mAuth = FirebaseAuth.getInstance();
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        displayUserInfo();
        setupLogoutButton();
        setupAddProductButton();
        initUserItems();

        return binding.getRoot();
    }

    private void initUserItems() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            binding.progressBarUserItems.setVisibility(View.VISIBLE);
            viewModel.loadItemsByUser(user.getUid()).observe(getViewLifecycleOwner(), itemsModels -> {
                if (itemsModels != null && !itemsModels.isEmpty()) {
                    binding.userItemsRecycler.setLayoutManager(new LinearLayoutManager(
                            getContext(), LinearLayoutManager.HORIZONTAL, false));
                    binding.userItemsRecycler.setAdapter(new PopularAdapter(itemsModels));
                    binding.userItemsRecycler.setNestedScrollingEnabled(true);
                }
                binding.progressBarUserItems.setVisibility(View.GONE);
            });
        }
    }

    private void displayUserInfo() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .circleCrop()
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .into(binding.profileImageView);
            } else {
                binding.profileImageView.setImageResource(R.drawable.ic_profile_placeholder);
            }

            String name = user.getDisplayName();
            String email = user.getEmail();

            if (name != null && !name.isEmpty()) {
                binding.usernameText.setText(name);
            } else if (email != null) {
                binding.usernameText.setText(email.split("@")[0]);
            } else {
                binding.usernameText.setText("Usuario");
            }

            binding.emailText.setText(email != null ? email : "No email");
        }
    }

    private void setupLogoutButton() {
        binding.logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
        });
    }

    private void setupAddProductButton() {
        binding.addProductBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddProductActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
