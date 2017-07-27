package com.example.asus.wetmonitor_20170523.wetMonitor;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class FakeService extends Service{

	

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		startForeground(6, new Notification.Builder(this).build());
        startService(new Intent(this, BackGroundService.class));
        Log.e("FakeService","Fake_run");
		return super.onStartCommand(intent, flags, startId);
		
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		stopForeground(true);
		super.onDestroy();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
