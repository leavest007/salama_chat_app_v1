package com.leavest.sahava.salamav1.utils;


import android.content.Context;
import android.net.Uri;

import com.leavest.sahava.salamav1.models.User;
import com.leavest.sahava.salamav1.services.FirebaseChatService;
import com.leavest.sahava.salamav1.services.MyFirebaseMessagingService;

import io.realm.Realm;
import io.realm.RealmQuery;

public class Helper {
    public static final String BROADCAST_MY_USERS = "";
    public static final String REF_USER = "";
    public static final String BROADCAST_MY_CONTACTS = "";
    public static final String UPLOAD_AND_SEND = "";
    public static final String BROADCAST_LOGOUT = "";
    public static final String REF_CHAT = "";
    public static final String REF_GROUP = "";
    public static final String GROUP_NOTIFIED = "";
    public static final String GROUP_PREFIX = "";
    public static final String CURRENT_CHAT_ID = "";
    public static final String BROADCAST_GROUP = "";
    public static final String BROADCAST_USER = "";

    public Helper(FirebaseChatService myFirebaseMessagingService) {
    }

    public Helper(MyFirebaseMessagingService myFirebaseMessagingService) {
    }

    public static Realm getRealmInstance() {
    }

    public static String getChatChild(String myId, String id) {
    }

    public static void deleteMessageFromRealm(Realm rChatDb, String id) {
    }

    public static RealmQuery getChat(Realm rChatDb, String myId, String userOrGroupId) {
    }

    public static String getDateTime(long millis) {
    }

    public static boolean contactMatches(String id, String phoneNumber) {
    }

    public static void loadUrl(Context context, String url) {
    }

    public static String getMimeType(Context context, Uri uri) {
    }

    public User getLoggedInUser() {
    }

    public Object getSharedPreferenceHelper() {
    }

    public boolean isLoggedIn() {
    }

    public boolean isUserMute(String senderId) {
    }
}
