package com.example.tfg.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.tfg.Activity.LoginActivity;
import com.example.tfg.R;
import com.example.tfg.databinding.FragmentProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        mAuth = FirebaseAuth.getInstance();

        displayUserInfo();
        setupLogoutButton();

        return binding.getRoot();
    }

    private void displayUserInfo() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Cargar imagen de perfil con Glide
            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .circleCrop()
                        .placeholder(R.drawable.ic_profile_placeholder) // imagen por defecto
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
