package com.leavest.sahava.salamav1.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.leavest.sahava.salamav1.services.FirebaseChatService;

public class ChatActivity extends AppCompatActivity {

    public static Intent newIntent(FirebaseChatService chatService, FirebaseChatService firebaseChatService, Object p1) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
    }
}
