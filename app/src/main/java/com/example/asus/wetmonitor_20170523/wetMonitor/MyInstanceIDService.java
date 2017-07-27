package com.example.asus.wetmonitor_20170523.wetMonitor;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import static com.example.asus.wetmonitor_20170523.wetMonitor.MainActivity.TAG;

/**
 * Created by poco on 2017/7/25.
 */

public class MyInstanceIDService extends FirebaseInstanceIdService {
    static String refreshedToken;

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        refreshedToken= FirebaseInstanceId.getInstance().getToken();
        Log.e(TAG,"Refreshed Token:"+refreshedToken);
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String refreshedToken) {
    }
}
