package com.example.tfg.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tfg.Adapter.ChatAdapter;
import com.example.tfg.Domain.Chat;
import com.example.tfg.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ChatListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private List<Chat> chatList = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserUid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_chat_list, container, false);

        recyclerView = root.findViewById(R.id.recyclerViewChatList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();
        currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        adapter = new ChatAdapter(chatList, chat -> {
            // Cuando se pulsa un chat, abrimos el ChatFragment
            ChatFragment chatFragment = ChatFragment.newInstance(chat.getId(), chat.getOtherUserId(currentUserUid));
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, chatFragment)
                    .addToBackStack(null)
                    .commit();
        });

        recyclerView.setAdapter(adapter);

        loadChats();

        return root;
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
                    Toast.makeText(getContext(), "Error al cargar los chats", Toast.LENGTH_SHORT).show();
                    Log.e("ChatListFragment", "Error al obtener chats", e);
                });
    }
}
