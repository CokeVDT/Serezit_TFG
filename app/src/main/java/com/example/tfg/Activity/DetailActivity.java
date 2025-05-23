package com.example.tfg.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.tfg.Domain.ItemsModel;
import com.example.tfg.Fragments.ChatFragment;
import com.example.tfg.R;
import com.example.tfg.databinding.ActivityDetailBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetailActivity extends AppCompatActivity {

    private ActivityDetailBinding binding;
    private ItemsModel item;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        item = (ItemsModel) getIntent().getSerializableExtra("object");

        if (item == null) {
            finish();
            return;
        }
        binding.backBtn.setOnClickListener(v -> finish());
        showItemDetails();
        configurarBotonAccion();
    }

    private void showItemDetails() {
        binding.titleTxt.setText(item.getTitle());
        binding.descriptionTxt.setText(item.getDescription());
        Glide.with(this)
                .load(item.getPicUrl())
                .into(binding.pic);

        // Mostrar categorías
        if (item.getCategorias() != null && !item.getCategorias().isEmpty()) {
            String categoriasTexto = String.join(", ", item.getCategorias());
            binding.categoriesTxt.setText(categoriasTexto);
        } else {
            binding.categoriesTxt.setText("Sin categorías");
        }

        if (item.getOwnerId() != null) {
            loadUsername(item.getOwnerId());
        } else {
            binding.ownerTxt.setText("Vendido por: Desconocido");
        }

        binding.addToCartBtn.setText("Enviar mensaje");
    }


    private void loadUsername(String ownerId) {
        db.collection("Users").document(ownerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("name"); // Ajusta según tu campo
                        if (username != null && !username.isEmpty()) {
                            binding.ownerTxt.setText("Vendido por: " + username);
                        } else {
                            binding.ownerTxt.setText("Vendido por: Usuario desconocido");
                        }
                    } else {
                        binding.ownerTxt.setText("Vendido por: Usuario no encontrado");
                    }
                })
                .addOnFailureListener(e -> binding.ownerTxt.setText("Vendido por: Error al cargar usuario"));
    }

    private void configurarBotonAccion() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = currentUser != null ? currentUser.getUid() : null;

        if (item.getOwnerId() != null && item.getOwnerId().equals(currentUserId)) {
            // Producto del usuario actual → botón rojo eliminar
            binding.addToCartBtn.setText("Eliminar producto");
            binding.addToCartBtn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.red));
            binding.addToCartBtn.setOnClickListener(v -> eliminarProducto());
        } else {
            // Producto de otro usuario → enviar mensaje
            binding.addToCartBtn.setText("Enviar mensaje");
            binding.addToCartBtn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.orange));
            binding.addToCartBtn.setOnClickListener(v -> {
                if (item.getOwnerId() != null) {
                    openChatWithOwner(item.getOwnerId());
                } else {
                    Toast.makeText(this, "ID del propietario no disponible", Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    private void eliminarProducto() {
        if (item.getId() == null || item.getId().isEmpty()) {
            Toast.makeText(this, "ID del producto no disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Items").document(item.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show());
    }
    private void openChatWithOwner(String ownerUid) {
        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Buscar si chat ya existe
        db.collection("chats")
                .whereArrayContains("participants", currentUserUid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot) {
                        List<String> participants = (List<String>) doc.get("participants");
                        if (participants.contains(ownerUid)) {
                            // Chat encontrado, abrir chat
                            openChatActivity(doc.getId(), ownerUid);
                            return;
                        }
                    }
                    // Si no se encontró, crear chat nuevo
                    createChat(ownerUid);
                });
    }

    private void createChat(String ownerUid) {
        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> chat = new HashMap<>();
        chat.put("participants", Arrays.asList(currentUserUid, ownerUid));
        chat.put("lastMessage", "");
        chat.put("lastTimestamp", FieldValue.serverTimestamp());

        db.collection("chats").add(chat).addOnSuccessListener(documentReference -> {
            openChatActivity(documentReference.getId(), ownerUid);
        });
    }

    private void openChatActivity(String chatId, String ownerUid) {
        Intent intent = new Intent(this, ChatContainerActivity.class);
        intent.putExtra("chatId", chatId);
        intent.putExtra("ownerUid", ownerUid);
        startActivity(intent);
    }

}
