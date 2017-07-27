package com.example.asus.wetmonitor_20170523.wetMonitor;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;

import com.example.asus.wetmonitor_20170523.R;
import com.example.asus.wetmonitor_20170523.dbcontrol.PushDevice;

import java.util.ArrayList;
import java.util.Timer;

public class StartActivity extends Activity {

    MessageReceiver mMessageReceiver;
    public static final String MESSAGE_RECEIVED_ACTION = "com.example.user.jpushli.MESSAGE_RECEIVED_ACTION";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_EXTRAS = "extras";
    String phone_mac = "";
    Context mContext;
    Timer timer;
    static ProgressDialog progress_dialog;
    static String RID = "";
  //  ImageView background_img;
   /* private void init() {
        JPushInterface.init(getApplicationContext());
    }*/
    Login_dialog ld = new Login_dialog();
    SharedPreferences sharedPreferences;
    static boolean isAlive = false;
    static ArrayList<PushDevice> scanDeviceList = new ArrayList<>();
    public void registerMessageReceiver() {
        mMessageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        filter.addAction(MESSAGE_RECEIVED_ACTION);
        registerReceiver(mMessageReceiver, filter);
    }
    public class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (MESSAGE_RECEIVED_ACTION.equals(intent.getAction())) {
                String messge = intent.getStringExtra(KEY_MESSAGE);
                String extras = intent.getStringExtra(KEY_EXTRAS);
                StringBuilder showMsg = new StringBuilder();
                showMsg.append(KEY_MESSAGE + " : " + messge + "\n");
                Log.e("1", "");
                if (!ExampleUtil.isEmpty(extras)) {
                    showMsg.append(KEY_EXTRAS + " : " + extras + "\n");
                }
                Log.e("m:", showMsg.toString());
            }
        }
    }

    ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        SysApplication.getInstance().addActivity(this);
      //  JPushInterface.setDebugMode(true);    // 设置开启日志,发布时请关闭日志
       // JPushInterface.init(this);            // 初始化 JPush
       // init();
        registerMessageReceiver();

       // background_img = (ImageView)findViewById(R.id.imageView);


      /*  if (!BackGroundService.service_is_alive) {
            Log.e("Service_alive", "alive_false");
            startService(new Intent(this, FakeService.class));
        } else
            Log.e("Service_alive", "alive_true");*/

        timer = new Timer();
        mContext = StartActivity.this;
        phone_mac = getMacAddress(mContext);
       // timer.schedule(new Sendtask(phone_mac, 0), 0);
        //progress_dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog = new ProgressDialog(StartActivity.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage(getResources().getText(R.string.getting_rid).toString());
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);

        // progress_dialog.show();
        String rid = "";
       // rid = JPushInterface.getRegistrationID(getApplicationContext());
        if(rid.equals("")) {
            dialog.show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(5000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        dialog.dismiss();
                    //    String rid = JPushInterface.getRegistrationID(getApplicationContext());
                        //RID = rid;
                     //   Log.e("RIDDDDDD", rid);
                        startActivity(new Intent(StartActivity.this, MainActivity.class));
                    }
                }
            }).start();
        }else{
            RID = rid;
            Log.e("RIDDDDDD", rid);
            startActivity(new Intent(StartActivity.this, MainActivity.class));
        }
    }

   /* protected void onResume() {
        super.onResume();

        if (!Login_dialog.username.equals("")) {
            startActivity(new Intent(this, MainActivity.class));
           // finish();
        }

    }*/

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        isAlive = true;
        timer.cancel();
    }

    public String getMacAddress(Context context) {
        WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        Log.e("wifiInf", wifiInf.getMacAddress());
        return wifiInf.getMacAddress();
    }


}
