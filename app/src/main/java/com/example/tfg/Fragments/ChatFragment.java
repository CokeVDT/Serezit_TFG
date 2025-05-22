package com.example.tfg.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tfg.R;

public class ChatFragment extends Fragment {

    private static final String ARG_CHAT_ID = "chatId";
    private static final String ARG_OTHER_USER_ID = "otherUserId";

    private String chatId;
    private String otherUserId;

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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            chatId = getArguments().getString(ARG_CHAT_ID);
            otherUserId = getArguments().getString(ARG_OTHER_USER_ID);
        }
        // Aquí inflarías el layout del chat, y cargarías mensajes, etc.
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }
}

