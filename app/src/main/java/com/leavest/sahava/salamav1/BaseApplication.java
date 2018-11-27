package com.leavest.sahava.salamav1;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.leavest.sahava.salamav1.receivers.ConnectivityReceiver;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.google.GoogleEmojiProvider;

public class BaseApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ConnectivityReceiver.init(this);
        EmojiManager.install(new GoogleEmojiProvider());
    }
}
