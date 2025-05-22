package com.example.tfg.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.tfg.Domain.Chat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tfg.Adapter.ChatAdapter;

import com.example.tfg.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ChatListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private List<Chat> chatList = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        recyclerView = findViewById(R.id.recyclerViewChatList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        adapter = new ChatAdapter(chatList, chat -> {
            Intent intent = new Intent(ChatListActivity.this, ChatActivity.class);
            intent.putExtra("chatId", chat.getId());
            intent.putExtra("ownerUid", chat.getOtherUserId(currentUserUid)); // método para obtener otro usuario
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        loadChats();
    }

    private void loadChats() {
        db.collection("chats")
                .whereArrayContains("participants", currentUserUid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    chatList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Chat chat = doc.toObject(Chat.class);
                        chat.setId(doc.getId());
                        chatList.add(chat);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar los chats", Toast.LENGTH_SHORT).show();
                    Log.e("ChatListActivity", "Error al obtener chats", e);
                });
    }
}