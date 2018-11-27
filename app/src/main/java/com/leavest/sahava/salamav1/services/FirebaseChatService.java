package com.leavest.sahava.salamav1.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.leavest.sahava.salamav1.Manifest;
import com.leavest.sahava.salamav1.R;
import com.leavest.sahava.salamav1.activities.ChatActivity;
import com.leavest.sahava.salamav1.models.Attachment;
import com.leavest.sahava.salamav1.models.AttachmentTypes;
import com.leavest.sahava.salamav1.models.Chat;
import com.leavest.sahava.salamav1.models.Contact;
import com.leavest.sahava.salamav1.models.Group;
import com.leavest.sahava.salamav1.models.Message;
import com.leavest.sahava.salamav1.models.MyString;
import com.leavest.sahava.salamav1.models.User;
import com.leavest.sahava.salamav1.utils.FirebaseUploader;
import com.leavest.sahava.salamav1.utils.Helper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;

public class FirebaseChatService extends Service {

    private static final String CHANNEL_ID_MAIN = "my_channel_01";
    private static final String CHANNEL_ID_GROUP = "my_channel_02";
    private static final String CHANNEL_ID_USER = "my_channel_03";

    private DatabaseReference usersRef, chatRef, groupsRef;
    private Helper helper;
    private String myId;
    private Realm rChatDb;
    private HashMap<String, User> userHashMap = new HashMap<>();
    private HashMap<String, Group> groupHashMap = new HashMap<>();
    private User userMe;
    private ArrayList<Contact> myContacts;
    private ArrayList<User> myUsers;
    private Group group;

    public FirebaseChatService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("Salama", "onCreate");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_MAIN, "Salama chat service", NotificationManager.IMPORTANCE_LOW);
            channel.setSound(null, null);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
        }
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_MAIN)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Salama Chat running")
                .setSound(null)
                .build();
        startForeground(1, notification);
        LocalBroadcastManager.getInstance(this).registerReceiver(uploadAndSendReceiver, new IntentFilter(Helper.UPLOAD_AND_SEND));
        LocalBroadcastManager.getInstance(this).registerReceiver(logoutReceiver, new IntentFilter(Helper.BROADCAST_LOGOUT));
    }

    private BroadcastReceiver logoutReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stopForeground(true);
            stopSelf();
        }
    };

    private BroadcastReceiver uploadAndSendReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null & intent.getAction().equals(Helper.UPLOAD_AND_SEND)) {
                Attachment attachment = intent.getParcelableExtra("attachment");
                int type = intent.getIntExtra("attachment_type", -1);
                String attachmentFilePath = intent.getStringExtra("attachment_file_path");
                String attachmentChatChild = intent.getStringExtra("attachment_chat_child");
                String attachmentRecipientId = intent.getStringExtra("attachment_recipient_id");
                uploadAndSend(new File(attachmentFilePath), attachment, type, attachmentChatChild, attachmentRecipientId);


            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("Salama", "onStartCommand");
        if (!User.validate(userMe)) {
            initVars();
            if (User.validate(userMe)) {
            myId = userMe.getId();
            rChatDb = Helper.getRealmInstance();
            registerUserUpdates();
            registerGroupUpdates(true, group.getId());
            if (ContextCompat.checkSelfPermission(this, Manifest.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                if (!FetchMyUserService.STARTED) {
                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (firebaseUser != null) {
                        firebaseUser.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                            @Override
                            public void onComplete(@NonNull Task<GetTokenResult> task) {
                                if (task.isSuccessful()) {
                                    String idToken = task.getResult().getToken();
                                    FetchMyUserService.startMyUsersService(FirebaseChatService.this, userMe.getId(), idToken);
                                    }
                                }
                            });
                     }
                    }
                }
            } else { stopForeground(true);
            stopSelf();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void initVars() {
        Log.e("Salama", "initVars");
        helper = new Helper(this);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        usersRef = firebaseDatabase.getReference(Helper.REF_USER);
        chatRef = firebaseDatabase.getReference(Helper.REF_CHAT);
        groupsRef = firebaseDatabase.getReference(Helper.REF_GROUP);
        Realm.init(this);

        userMe = helper.getLoggedInUser();
    }

    private void restartService() {
    Log.e("Salama", "Restart");
    if (new Helper(this).isLoggedIn()) {
        String action;
        Intent intent = new Intent(this, FirebaseChatService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 99, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 500, pendingIntent);

    }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("Salama", "onDestroy");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(uploadAndSendReceiver);
        restartService();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        restartService();
        super.onTaskRemoved(rootIntent);
        Log.e("Salama", "onTaskRemoved");
    }

    private void uploadAndSend(final File fileToUpload, final Attachment attachment, final int attachmentType, final String chatChild, final String recipientId) {
        if (!fileToUpload.exists())
            return;
        final String fileName = Uri.fromFile(fileToUpload).getLastPathSegment();
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child(getString(R.string.app_name)).child(AttachmentTypes.getTypeName(attachmentType)).child(fileName);
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // If file is already uploaded
                Attachment attachment1 = attachment;
                if (attachment1 == null) attachment1 = new Attachment();
                attachment1.setName(fileName);
                attachment1.setUrl(uri.toString());
                attachment1.setBytesCount(fileToUpload.length());
                sendMessage(null, attachmentType, attachment1, chatChild, recipientId);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Elase upload and then send message
                FirebaseUploader firebaseUploader = new FirebaseUploader(new FirebaseUploader.UploadListener() {
                    @Override
                    public void onUploadFail(String message) {
                        Log.e("DatabaseException", message);
                    }

                    @Override
                    public void onUploadSuccess(String downloadUrl) {
                        Attachment attachment1 = attachment;
                        if (attachment1 == null) attachment1 = new Attachment();
                        attachment1.setName(fileToUpload.getName());
                        attachment1.setUrl(downloadUrl);
                        attachment1.setBytesCount(fileToUpload.length());
                        sendMessage(null, attachmentType, attachment, chatChild, recipientId);
                    }

                    @Override
                    public void onUploadProgress(int progress) {

                    }

                    @Override
                    public void onUploadCancelled() {

                    }
                }, storageReference);
                firebaseUploader.uploadOthers(getApplicationContext(), fileToUpload);
            }
        });
    }

    private void sendMessage(String messageBody, @AttachmentTypes.AttachmentType int attachmentType, Attachment attachment, String chatChild, String userOrGroupId) {
        //Create message object
        Message message = new Message();
        message.setAttachmentType(attachmentType);
        if (attachmentType != AttachmentTypes.NONE_TEXT)
            message.setAttachment(attachment);
        message.setBody(messageBody);
        message.setDate(System.currentTimeMillis());
        message.setSenderId(userMe.getId());
        message.setSenderName(userMe.getName());
        message.setSent(true);
        message.setDelivered(false);
        message.setRecipientId(userOrGroupId);
        message.setId(chatRef.child(chatChild).push().getKey());

        //Add messages in chat child
        chatRef.child(chatChild).child(message.getId()).setValue(message);
    }

    private void registerGroupUpdates(boolean b, String id) {
        groupsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                try {
                    Group group = dataSnapshot.getValue(Group.class);
                    if (Group.validate(group) && group.getUserIds().contains(new MyString(myId))) {
                        if (!groupHashMap.containsKey(group.getId())) {
                            groupHashMap.put(group.getId(), group);
                            broadcastGroup("added", group);
                            checkAndNotify(group);
                            registerGroupUpdates(true, group.getId());
                        }
                    }
                } catch (Exception ex) {
                    Log.e("GROUP", "invalid group");
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                try {
                    Group group = dataSnapshot.getValue(Group.class);
                    if (Group.validate(group)) {
                        if (group.getUserIds().contains(new MyString(myId))) {
                            broadcastGroup("changed", group);
                            updateGroupInDB(group);
                        } else if (groupHashMap.containsKey(group.getId())) {
                            registerChatUpdates(false, group.getId());
                            groupHashMap.remove(group.getId());
                            broadcastGroup("changed", group);
                            updateGroupInDB(group);
                        }
                    }
                }catch (Exception ex) {
                    Log.e("GROUP", "invalid group");
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkAndNotify(Group group) {
        Chat thisGroupChat  = rChatDb.where(Chat.class)
                .equalTo("myId", myId)
                .equalTo("groupId", group.getId()).findFirst();
        if (thisGroupChat == null) {
            if (!group.getUserIds().get(0).equals(new MyString(myId)) && !helper.getSharedPreferenceHelper().getBooleanPreference(Helper.GROUP_NOTIFIED, false)) {
                notifyNewGroup(group);
                helper.getSharedPreferenceHelper().setBooleanPreference(Helper.GROUP_NOTIFIED, true);
            }
            rChatDb.beginTransaction();
            thisGroupChat = rChatDb.createObject(Chat.class);
            thisGroupChat.setGroup(rChatDb.copyToRealm(groupHashMap.get(group.getId())));
            thisGroupChat.setGroupId(group.getId());
            thisGroupChat.setMessages(new RealmList<Message>());
            thisGroupChat.setMyId(myId);
            thisGroupChat.setRead(false);
            long millis = System.currentTimeMillis();
            thisGroupChat.setLastMessage("Created on " + Helper.getDateTime(millis));
            thisGroupChat.setTimeUpdated(millis);
            rChatDb.commitTransaction();
        }
    }



    private void notifyNewGroup(Group group) {

        Intent chatActivity = ChatActivity.newIntent(this, null, group);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(chatActivity);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(99, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notificationBuilder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_GROUP, "Salama new group notification", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
            notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID_GROUP);
        } else {
            notificationBuilder = new NotificationCompat.Builder(this);
        }

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notificationBuilder.setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("Group: " + group.getName())
                .setContentText("You have been added to new group called" + group.getName())
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        int msgId = Integer.parseInt(group.getId().substring(group.getId().length() - 4, group.getId().length() - 1));
        notificationManager.notify(msgId, notificationBuilder.build());
    }

    private void registerUserUpdates() {
        usersRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                try {
                    User user = dataSnapshot.getValue(User.class);
                    if (User.validate(user)) {
                        if (!userHashMap.containsKey(user.getId())) {
                            userHashMap.put(user.getId(), user);
                            broadcastUser("added", user);
                            registerChatUpdates(true, user.getId());
                        }
                    }
                } catch (Exception ex) {
                    Log.e("USER", "invalid user");
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                try {
                    User user = dataSnapshot.getValue(User.class);
                    if (User.validate(user)) {
                        broadcastUser("changed", user);
                        updateUserInDB(user);
                    }
                } catch (Exception ex) {
                    Log.e("USER", "invalid user");
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    private void registerChatUpdates(boolean register, String id) {
        if (!TextUtils.isEmpty(myId) && !TextUtils.isEmpty(id)) {
            DatabaseReference idChatRef = chatRef.child(id.startsWith(Helper.GROUP_PREFIX) ? id : Helper.getChatChild(myId, id));
            if (register) {
                idChatRef.addChildEventListener(chatUpdateListener);
            } else {
                idChatRef.removeEventListener(chatUpdateListener);
            }
        }
    }

    private ChildEventListener chatUpdateListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Message message = dataSnapshot.getValue(Message.class);

            if (message.isDelivered() || (message.getRecipientId().startsWith(Helper.GROUP_PREFIX) && !groupHashMap.containsKey(message.getRecipientId())))
                return;

            Message result = rChatDb.where(Message.class).equalTo("id", message.getId()).findFirst();
            if (result == null && !TextUtils.isEmpty(myId) && helper.isLoggedIn()) {
                saveMessage(message);
                if (!message.getRecipientId().startsWith(Helper.GROUP_PREFIX) && !message.getSenderId().equals(myId) && !message.isDelivered())
                    chatRef.child(dataSnapshot.getRef().getParent().getKey()).child("delivered").setValue(true);
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Message message = dataSnapshot.getValue(Message.class);
            Message result = rChatDb.where(Message.class).equalTo("id", message.getId()).findFirst();
            if (result != null) {
                rChatDb.beginTransaction();
                result.setDelivered(message.isDelivered());
                rChatDb.commitTransaction();
            }
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            Message message = dataSnapshot.getValue(Message.class);
            Helper.deleteMessageFromRealm(rChatDb, message.getId());

            String userOrGroupId = myId.equals(message.getSenderId()) ? message.getRecipientId() : message.getSenderId();
            Chat chat = (Chat) Helper.getChat(rChatDb, myId, userOrGroupId).findFirst();
            if (chat != null) {
                rChatDb.beginTransaction();
                RealmList<Message> realmList = chat.getMessages();
                if (realmList.size() == 0)
                    RealmObject.deleteFromRealm(chat);
                else {
                    chat.setLastMessage(realmList.get(realmList.size() - 1).getBody());
                    chat.setTimeUpdated(realmList.get(realmList.size() - 1).getDate());
                }
                rChatDb.commitTransaction();
            }
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    private void saveMessage(Message message) {
        if (message.getAttachment() != null && !TextUtils.isEmpty(message.getAttachment().getUrl()) && !TextUtils.isEmpty(message.getAttachment().getName())) {
            String idToCompare = "loading" + message.getAttachment().getBytesCount() + message.getAttachment().getName();
            Helper.deleteMessageFromRealm(rChatDb, idToCompare);
        }

        String userOrGroupId = message.getRecipientId().startsWith(Helper.GROUP_PREFIX) ? message.getRecipientId() : myId.equals(message.getSenderId()) ? message.getRecipientId() : message.getSenderId();
        Chat chat = (Chat) Helper.getChat(rChatDb, myId, userOrGroupId).findFirst();
        rChatDb.beginTransaction();
        if (chat == null) {
            chat = rChatDb.createObject(Chat.class);
            if (userOrGroupId.startsWith(Helper.GROUP_PREFIX)) {
                chat.setGroup(rChatDb.copyToRealm(groupHashMap.get(userOrGroupId)));
                chat.setGroupId(userOrGroupId);
                chat.setUser(null);
                chat.setUserId(null);
            } else {
                chat.setUser(rChatDb.copyToRealm(userHashMap.get(userOrGroupId)));
                chat.setUserId(userOrGroupId);
                chat.setGroup(null);
                chat.setGroupId(null);
            }
            chat.setMessages(new RealmList<Message>());
            chat.setLastMessage(message.getBody());
            chat.setMyId(myId);
            chat.setTimeUpdated(message.getDate());
        }

        if (!message.getSenderId().equals(myId))
            chat.setRead(false);
        chat.setTimeUpdated(message.getDate());
        chat.getMessages().add(message);
        chat.setLastMessage(message.getBody());
        rChatDb.commitTransaction();

        if (!message.isDelivered() && !message.getSenderId().equals(myId) && !helper.isUserMute(message.getSenderId()) && (Helper.CURRENT_CHAT_ID == null || !Helper.CURRENT_CHAT_ID.equals(userOrGroupId))) {
            Intent chatActivity = null;
            if (userOrGroupId.startsWith(Helper.GROUP_PREFIX))
                chatActivity = ChatActivity.newIntent(this, null, chat.getGroup());
            else
                chatActivity = ChatActivity.newIntent(this, null, chat.getUser());
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addNextIntentWithParentStack(chatActivity);
            PendingIntent pendingIntent = stackBuilder.getPendingIntent(99, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder notificationBuilder = null;
            String channelId = userOrGroupId.startsWith(Helper.GROUP_PREFIX) ? CHANNEL_ID_GROUP : CHANNEL_ID_USER;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId, "Salama new message notification", NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
                notificationBuilder = new NotificationCompat.Builder(this, channelId);
            } else {
                notificationBuilder = new NotificationCompat.Builder(this);
            }

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            notificationBuilder.setSmallIcon(R.drawable.notification_icon)
                    .setContentTitle(userOrGroupId.startsWith(Helper.GROUP_PREFIX) ? chat.getGroup().getName() : (CharSequence) chat.getUser().getName())
                    .setContentText(message.getBody())
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);
            int msgId = 0;

            try {
                msgId = Integer.parseInt(message.getSenderId());
            } catch (NumberFormatException ex ) {
                msgId = Integer.parseInt(message.getSenderId().substring(message.getSenderId().length() / 2));
            }
            notificationManager.notify(msgId, notificationBuilder.build());
        }


    }

    private void updateUserInDB(User value) {
        //update in database
        if (!TextUtils.isEmpty(myId)) {
            Chat chat = rChatDb.where(Chat.class).equalTo("myId", myId).equalTo("userId", value.getId()).findFirst();
            if (chat != null) {
                rChatDb.beginTransaction();
                chat.setGroup(rChatDb.copyToRealm(group));
                rChatDb.commitTransaction();
            }
        }
    }

    private void updateGroupInDB(Group group) {
        if (!TextUtils.isEmpty(myId)) {
            Chat chat = rChatDb.where(Chat.class).equalTo("myId", myId).equalTo("groupId", group.getId()).findFirst();
            if (chat != null) {
                rChatDb.beginTransaction();
                chat.setGroup(rChatDb.copyToRealm(group));
                rChatDb.commitTransaction();
            }
        }
    }

    private void broadcastGroup(String what, Group value) {
        Intent intent = new Intent(Helper.BROADCAST_GROUP);
        intent.putExtra("data", value);
        intent.putExtra("what", what);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.sendBroadcast(intent);
    }

    private void broadcastUser(String what, User value) {
        Intent intent = new Intent(Helper.BROADCAST_USER);
        intent.putExtra("data", value);
        intent.putExtra("what", what);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.sendBroadcast(intent);
    }


}



