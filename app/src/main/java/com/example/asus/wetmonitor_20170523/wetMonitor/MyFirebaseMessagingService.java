package com.example.asus.wetmonitor_20170523.wetMonitor;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static com.example.asus.wetmonitor_20170523.wetMonitor.MainActivity.TAG;

/**
 * Created by poco on 2017/7/25.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.e(TAG,"From:"+remoteMessage.getFrom());
        Log.e(TAG,"Title:"+remoteMessage.getNotification().getTitle());
        Log.e(TAG,"From:"+remoteMessage.getNotification().getBody());
    }
}
