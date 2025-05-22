package com.example.tfg.Respository;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.firestore.DocumentSnapshot;

import com.example.tfg.Domain.Message;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MessageRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LiveData<List<Message>> getMessages(String chatId) {
        MutableLiveData<List<Message>> messagesLiveData = new MutableLiveData<>();

        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;

                    List<Message> messages = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                        Message message = doc.toObject(Message.class);
                        if (message != null) messages.add(message);
                    }
                    messagesLiveData.setValue(messages);
                });

        return messagesLiveData;
    }


    public void sendMessage(String chatId, Message message) {
        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(message);

        db.collection("chats")
                .document(chatId)
                .update("lastMessage", message.getText(),
                        "lastTimestamp", message.getTimestamp());
    }
}