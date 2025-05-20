package com.example.tfg.Activity;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.tfg.Domain.ItemsModel;
import com.example.tfg.databinding.ActivityDetailBinding;
import com.google.firebase.firestore.FirebaseFirestore;

public class DetailActivity extends AppCompatActivity {
private ActivityDetailBinding binding;
private ItemsModel object;
private int numberOrder = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getBundles();

    }





    private void getBundles() {
        object = (ItemsModel) getIntent().getSerializableExtra("object");

        if (object == null) return;

        // Cargar la imagen en el ImageView
        Glide.with(this)
                .load(object.getPicUrl())
                .into(binding.pic);

        binding.titleTxt.setText(object.getTitle());
        binding.descriptionTxt.setText(object.getDescription());

        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(object.getOwnerId())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String username = snapshot.getString("name");
                        Log.d("DetailActivity", "Username obtenido: " + username);
                        if (username != null) {
                            binding.ownerTxt.setText("Vendido por: " + username);
                        } else {
                            binding.ownerTxt.setText("Vendido por: Nombre no disponible");
                            Log.w("DetailActivity", "El campo username es null en el documento");
                        }
                    } else {
                        binding.ownerTxt.setText("Vendido por: Usuario no encontrado");
                        Log.w("DetailActivity", "El documento del usuario no existe");
                    }
                })
                .addOnFailureListener(e -> {
                    binding.ownerTxt.setText("Error al cargar propietario");
                    Log.e("DetailActivity", "Error al obtener usuario", e);
                });


        binding.backBtn.setOnClickListener(v -> finish());
    }

}