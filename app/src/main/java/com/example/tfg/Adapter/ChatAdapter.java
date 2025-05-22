package com.example.tfg.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tfg.Domain.Chat;
import com.example.tfg.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private final List<Chat> chatList;
    private final OnChatClickListener listener;

    // Interfaz para el callback cuando se pulsa un chat
    public interface OnChatClickListener {
        void onChatClick(Chat chat);
    }

    // Constructor que recibe la lista y el listener
    public ChatAdapter(List<Chat> chatList, OnChatClickListener listener) {
        this.chatList = chatList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chatList.get(position);

        String otherUserIdTemp = null;
        for (String id : chat.getParticipants()) {
            if (!id.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                otherUserIdTemp = id;
                break;
            }
        }
        final String otherUserId = otherUserIdTemp;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (otherUserId != null) {
            db.collection("Users").document(otherUserId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            holder.usernameText.setText("Chat con: " + name);
                        } else {
                            holder.usernameText.setText("Chat con: " + otherUserId);
                        }
                    })
                    .addOnFailureListener(e -> holder.usernameText.setText("Chat con: " + otherUserId));
        } else {
            holder.usernameText.setText("Chat");
        }

        holder.lastMessageText.setText(chat.getLastMessage());

        if (chat.getLastTimestamp() != null) {
            String time = new SimpleDateFormat("HH:mm dd/MM", Locale.getDefault()).format(chat.getLastTimestamp());
            holder.timestampText.setText(time);
        } else {
            holder.timestampText.setText("");
        }

        // Listener para clicks en el item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChatClick(chat);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText, lastMessageText, timestampText;

        ChatViewHolder(View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.chatUserTextView);
            lastMessageText = itemView.findViewById(R.id.lastMessageTextView);
            timestampText = itemView.findViewById(R.id.lastMessageTimeTextView);
        }
    }
}
