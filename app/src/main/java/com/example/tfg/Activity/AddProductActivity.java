package com.example.tfg.Activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.UploadCallback;
import com.example.tfg.Domain.ItemsModel;
import com.example.tfg.databinding.ActivityAddProductBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddProductActivity extends AppCompatActivity {

    private ActivityAddProductBinding binding;
    private static final int IMAGE_PICK_REQUEST = 1001;
    private Uri selectedImageUri;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        setupCloudinary();

        binding.imageSelectBtn.setOnClickListener(v -> openImagePicker());

        binding.publishBtn.setOnClickListener(v -> {
            if (selectedImageUri == null) {
                Toast.makeText(this, "Selecciona una imagen", Toast.LENGTH_SHORT).show();
                return;
            }
            subirImagenYCrearProducto(selectedImageUri);
        });
    }

    private void setupCloudinary() {
        // Solo inicializar MediaManager una vez
        try {
            MediaManager.get(); // Si ya está inicializado, no hace nada
        } catch (IllegalStateException e) {
            // Si no está inicializado, lo inicializamos aquí
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "dlnrrq7er");
            config.put("api_key", "974664524211252");
            config.put("api_secret", "_Q02w6QN73E7SaS5KC71WrHD8YU");
            MediaManager.init(this, config);
        }
    }


    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, IMAGE_PICK_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICK_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            binding.imagePreview.setImageURI(selectedImageUri);
        }
    }

    private void subirImagenYCrearProducto(Uri uri) {
        binding.publishBtn.setEnabled(false);
        binding.publishBtn.setText("Publicando...");

        MediaManager.get().upload(uri)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {}

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = resultData.get("secure_url").toString();
                        crearProducto(imageUrl);
                    }

                    @Override
                    public void onError(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                        Toast.makeText(AddProductActivity.this, "Error al subir imagen: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                        binding.publishBtn.setEnabled(true);
                        binding.publishBtn.setText("Publicar");
                    }

                    @Override
                    public void onReschedule(String requestId, com.cloudinary.android.callback.ErrorInfo error) {}
                })
                .dispatch();
    }

    private void crearProducto(String imageUrl) {
        String title = binding.titleInput.getText().toString().trim();
        String desc = binding.descriptionInput.getText().toString().trim();
        String priceStr = binding.priceInput.getText().toString().trim();

        if (title.isEmpty() || desc.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            binding.publishBtn.setEnabled(true);
            binding.publishBtn.setText("Publicar");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Precio inválido", Toast.LENGTH_SHORT).show();
            binding.publishBtn.setEnabled(true);
            binding.publishBtn.setText("Publicar");
            return;
        }

        String ownerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        ItemsModel item = new ItemsModel();
        item.setTitle(title);
        item.setDescription(desc);
        item.setPrice(price);
        item.setPicUrl(imageUrl);
        item.setOwnerId(ownerId);


        db.collection("Items")
                .add(item)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Producto publicado", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al publicar", Toast.LENGTH_SHORT).show();
                    binding.publishBtn.setEnabled(true);
                    binding.publishBtn.setText("Publicar");
                });
    }
}
