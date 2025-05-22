package com.example.tfg.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;

import com.example.tfg.Adapter.CategoryAdapter;
import com.example.tfg.Adapter.PopularAdapter;
import com.example.tfg.Adapter.SliderAdapter;
import com.example.tfg.Domain.BannerModel;
import com.example.tfg.Fragments.ChatListFragment;
import com.example.tfg.Fragments.HomeFragment;
import com.example.tfg.Fragments.ProfileFragment;
import com.example.tfg.R;
import com.example.tfg.ViewModel.MainViewModel;
import com.example.tfg.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();


        bottomNavigation();
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void bottomNavigation() {
        binding.bottomNavigation.setItemSelected(R.id.home, true);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment()).commit();

        binding.bottomNavigation.setOnItemSelectedListener(itemId -> {
            Fragment fragment = null;

            if (itemId == R.id.home) {
                fragment = new HomeFragment();
            } else if (itemId == R.id.profile) {
                fragment = new ProfileFragment();
            } else if (itemId == R.id.chat) {
                fragment = new ChatListFragment();
            }

            if (fragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

}