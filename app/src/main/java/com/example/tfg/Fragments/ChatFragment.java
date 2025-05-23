package com.example.tfg.Fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

public class ChatFragment extends Fragment {

    private static final String ARG_CHAT_ID = "chatId";
    private static final String ARG_OTHER_USER_ID = "otherUserId";

    private String chatId;
    private String otherUserId;
    private String currentUserUid;

    private RecyclerView recyclerView;
    private EditText editTextMessage;
    private ImageButton buttonSend;


    private TextView usernameTextView;
    private ImageView profileImageView;

    private FirebaseFirestore db;

    private List<Message> messageList = new ArrayList<>();
    private MessageAdapter adapter;

    public static ChatFragment newInstance(String chatId, String otherUserId) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CHAT_ID, chatId);
        args.putString(ARG_OTHER_USER_ID, otherUserId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        // Obtener argumentos
        if (getArguments() != null) {
            chatId = getArguments().getString(ARG_CHAT_ID);
            otherUserId = getArguments().getString(ARG_OTHER_USER_ID);
        }

        currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();

        // Vincular vistas
        recyclerView = view.findViewById(R.id.messagesRecyclerView);
        editTextMessage = view.findViewById(R.id.messageEditText);
        buttonSend = view.findViewById(R.id.sendMessageButton);
        usernameTextView = view.findViewById(R.id.chatHeaderTextView);
        profileImageView = view.findViewById(R.id.profileImageView);

        // Configurar RecyclerView
        adapter = new MessageAdapter(messageList, currentUserUid);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Cargar mensajes y datos
        listenMessages();
        loadOtherUserData();

        // Botón de envío
        buttonSend.setOnClickListener(v -> sendMessage());

        return view;
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
        if (TextUtils.isEmpty(text)) return;

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
                        String profileUrl = documentSnapshot.getString("photoUrl");

                        if (name != null) {
                            usernameTextView.setText(name);
                        }

                        if (profileUrl != null && !profileUrl.isEmpty()) {
                            Glide.with(requireContext())
                                    .load(profileUrl)
                                    .circleCrop() // 👈 Hace la imagen circular
                                    .placeholder(R.drawable.ic_profile_placeholder)
                                    .error(R.drawable.ic_profile_placeholder)
                                    .into(profileImageView);
                        }
                    }
                });
    }
}
