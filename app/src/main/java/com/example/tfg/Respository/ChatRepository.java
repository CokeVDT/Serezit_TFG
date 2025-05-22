package com.example.tfg.Respository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tfg.Domain.Chat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ChatRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    public LiveData<List<Chat>> getUserChats() {
        MutableLiveData<List<Chat>> chatsLiveData = new MutableLiveData<>();

        db.collection("chats")
                .whereArrayContains("participants", currentUserId)
                .orderBy("lastTimestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;

                    List<Chat> chats = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Chat chat = doc.toObject(Chat.class);
                        if (chat != null) {
                            chat.setId(doc.getId());
                            chats.add(chat);
                        }
                    }
                    chatsLiveData.setValue(chats);
                });

        return chatsLiveData;
    }

    public void createOrGetChat(String userId, ChatCallback callback) {
        List<String> participantsList = Arrays.asList(currentUserId, userId);

        db.collection("chats")
                .whereEqualTo("participants", participantsList)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        callback.onChatReady(queryDocumentSnapshots.getDocuments().get(0).getId());
                    } else {
                        Chat newChat = new Chat();
                        newChat.setParticipants(participantsList);
                        newChat.setLastMessage("");
                        newChat.setLastTimestamp(new Date());

                        db.collection("chats")
                                .add(newChat)
                                .addOnSuccessListener(docRef -> callback.onChatReady(docRef.getId()));
                    }
                });
    }

    public interface ChatCallback {
        void onChatReady(String chatId);
    }
}
