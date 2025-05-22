package com.example.tfg.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.tfg.Domain.Message;
import com.example.tfg.Respository.MessageRepository;

import java.util.List;

public class ChatViewModel extends ViewModel {
    private final MessageRepository messageRepository = new MessageRepository();

    public LiveData<List<Message>> getMessages(String chatId) {
        return messageRepository.getMessages(chatId);
    }

    public void sendMessage(String chatId, Message message) {
        messageRepository.sendMessage(chatId, message);
    }
}
