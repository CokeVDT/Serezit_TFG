package com.example.tfg.Activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tfg.Fragments.ChatFragment;
import com.example.tfg.R;

public class ChatContainerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_container);

        String chatId = getIntent().getStringExtra("chatId");
        String otherUserId = getIntent().getStringExtra("ownerUid");

        if (chatId != null && otherUserId != null) {
            ChatFragment chatFragment = ChatFragment.newInstance(chatId, otherUserId);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, chatFragment)
                    .commit();
        } else {
            finish(); // Si no hay datos, cerramos la actividad
        }
    }
}
