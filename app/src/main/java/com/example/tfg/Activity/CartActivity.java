package com.example.tfg.Activity;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tfg.Adapter.CartAdapter;
import com.example.tfg.Domain.ItemsModel;
import com.example.tfg.Helper.ChangeNumberItemsListener;
import com.example.tfg.Helper.ManagmentCart;
import com.example.tfg.R;
import com.example.tfg.databinding.ActivityCartBinding;

public class CartActivity extends AppCompatActivity {
    private ActivityCartBinding binding;
    private double tax;
    private ManagmentCart managmentCart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding=ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        managmentCart=new ManagmentCart(this);

        calculatorCart();
        setVariable();
        initCartList();
    }

    private void initCartList() {
        if(managmentCart.getListCart().isEmpty()){
            binding.emptyTxt.setVisibility(View.VISIBLE);
            binding.scrollView3.setVisibility(View.GONE);
        } else {
            binding.emptyTxt.setVisibility(View.GONE);
            binding.scrollView3.setVisibility(View.VISIBLE);
        }

        binding.cartView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.cartView.setAdapter(new CartAdapter(managmentCart.getListCart(), this, () -> {
            calculatorCart(); // Recalcular cuando cambien los items
            initCartList();   // Actualizar la lista por si se quedó vacía
        }));
    }

    private void setVariable() {
        binding.backBtn.setOnClickListener(v -> finish());
    }

    private void calculatorCart() {
        double percentTax = 0.02;
        double delivery = 10;

        if (managmentCart.getListCart().isEmpty()) {
            // Si el carrito está vacío, mostrar todo en 0
            binding.totalFeeTxt.setText("$0");
            binding.taxTxt.setText("$0");
            binding.deliveryTxt.setText("$0");
            binding.totalTxt.setText("$0");
        } else {
            // Calcular valores normales si hay items en el carrito
            tax = Math.round((managmentCart.getTotalFee() * percentTax * 100.0)) / 100.0;
            double total = Math.round((managmentCart.getTotalFee() + tax + delivery) * 100.0) / 100.0;
            double itemTotal = Math.round((managmentCart.getTotalFee() * 100.0)) / 100.0;

            binding.totalFeeTxt.setText("$" + itemTotal);
            binding.taxTxt.setText("$" + tax);
            binding.deliveryTxt.setText("$" + delivery);
            binding.totalTxt.setText("$" + total);
        }
    }

}