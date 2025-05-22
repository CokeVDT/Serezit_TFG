package com.example.tfg.Activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tfg.Adapter.MessageAdapter;
import com.example.tfg.Domain.Message;
import com.example.tfg.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private String chatId;
    private String currentUserUid;
    private String otherUserId;

    private RecyclerView recyclerView;
    private EditText editTextMessage;
    private Button buttonSend;
    private TextView usernameTextView;
    private ImageView profileImageView;

    private FirebaseFirestore db;

    private List<Message> messageList = new ArrayList<>();
    private MessageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Inicializar Firestore y autenticación
        db = FirebaseFirestore.getInstance();
        currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Obtener extras del intent
        chatId = getIntent().getStringExtra("chatId");
        otherUserId = getIntent().getStringExtra("ownerUid");

        // Asignar vistas
        recyclerView = findViewById(R.id.recyclerViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        usernameTextView = findViewById(R.id.usernameTextView);
        profileImageView = findViewById(R.id.profileImageView);

        // Configurar RecyclerView
        adapter = new MessageAdapter(messageList, currentUserUid);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Escuchar mensajes y cargar datos del otro usuario
        listenMessages();
        loadOtherUserData();

        // Enviar mensajes
        buttonSend.setOnClickListener(v -> sendMessage());
    }

    private void listenMessages() {
        db.collection("chats").document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    messageList.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        Message message = doc.toObject(Message.class);
                        messageList.add(message);
                    }
                    adapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(messageList.size() - 1);
                });
    }

    private void sendMessage() {
        String text = editTextMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        Message message = new Message(currentUserUid, text, new Date());

        db.collection("chats").document(chatId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    editTextMessage.setText("");

                    db.collection("chats").document(chatId).update(
                            "lastMessage", text,
                            "lastTimestamp", FieldValue.serverTimestamp()
                    );
                });
    }

    private void loadOtherUserData() {
        db.collection("Users").document(otherUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String profileUrl = documentSnapshot.getString("profileImage");

                        if (name != null) {
                            usernameTextView.setText(name);
                        }

                        if (profileUrl != null && !profileUrl.isEmpty()) {
                            Glide.with(this).load(profileUrl).into(profileImageView);
                        }
                    }
                });
    }
}
