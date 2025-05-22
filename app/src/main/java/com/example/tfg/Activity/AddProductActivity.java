package com.example.tfg.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.UploadCallback;
import com.example.tfg.Domain.ItemsModel;
import com.example.tfg.databinding.ActivityAddProductBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddProductActivity extends AppCompatActivity {

    private ActivityAddProductBinding binding;
    private static final int IMAGE_PICK_REQUEST = 1001;
    private Uri selectedImageUri;
    private FirebaseFirestore db;

    private final List<String> categoryList = new ArrayList<>();
    private final List<String> selectedCategories = new ArrayList<>();
    private boolean[] checkedItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        setupCloudinary();
        setupCategoryMultiSelect();

        binding.imageSelectBtn.setOnClickListener(v -> openImagePicker());
        binding.publishBtn.setOnClickListener(v -> {
            if (selectedImageUri == null) {
                Toast.makeText(this, "Selecciona una imagen", Toast.LENGTH_SHORT).show();
            } else {
                subirImagenYCrearProducto(selectedImageUri);
            }
        });
    }

    private void setupCloudinary() {
        try {
            MediaManager.get();
        } catch (IllegalStateException e) {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "dlnrrq7er");
            config.put("api_key", "974664524211252");
            config.put("api_secret", "_Q02w6QN73E7SaS5KC71WrHD8YU");
            MediaManager.init(this, config);
        }
    }

    private void setupCategoryMultiSelect() {
        db.collection("Category")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    categoryList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("title");
                        if (name != null) {
                            categoryList.add(name);
                        }
                    }

                    checkedItems = new boolean[categoryList.size()];

                    binding.categoryDropdown.setOnClickListener(v -> showMultiSelectDialog());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar categorías", Toast.LENGTH_SHORT).show();
                });
    }

    private void showMultiSelectDialog() {
        String[] categoriesArray = categoryList.toArray(new String[0]);

        new AlertDialog.Builder(this)
                .setTitle("Selecciona categorías")
                .setMultiChoiceItems(categoriesArray, checkedItems, (dialog, indexSelected, isChecked) -> {
                    String selected = categoryList.get(indexSelected);
                    if (isChecked) {
                        if (!selectedCategories.contains(selected)) {
                            selectedCategories.add(selected);
                        }
                    } else {
                        selectedCategories.remove(selected);
                    }
                })
                .setPositiveButton("OK", (dialog, which) -> {
                    binding.categoryDropdown.setText(String.join(", ", selectedCategories));
                })
                .setNegativeButton("Cancelar", null)
                .show();
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
        setLoading(true);

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
                        setLoading(false);
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

        if (title.isEmpty() || desc.isEmpty() || priceStr.isEmpty() || selectedCategories.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            setLoading(false);
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Precio inválido", Toast.LENGTH_SHORT).show();
            setLoading(false);
            return;
        }

        String ownerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        ItemsModel item = new ItemsModel();
        item.setTitle(title);
        item.setDescription(desc);
        item.setPrice(price);
        item.setPicUrl(imageUrl);
        item.setOwnerId(ownerId);
        item.setCategorias(new ArrayList<>(selectedCategories));

        db.collection("Items")
                .add(item)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Producto publicado", Toast.LENGTH_SHORT).show();
                    cerrarTeclado();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al publicar", Toast.LENGTH_SHORT).show();
                    setLoading(false);
                });
    }

    private void setLoading(boolean isLoading) {
        binding.publishBtn.setEnabled(!isLoading);
        binding.publishBtn.setText(isLoading ? "Publicando..." : "Publicar");
    }

    private void cerrarTeclado() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
