package com.example.asus.wetmonitor_20170523.wetMonitor;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.asus.wetmonitor_20170523.R;
import com.example.asus.wetmonitor_20170523.dbcontrol.BleDevice;
import com.example.asus.wetmonitor_20170523.dbcontrol.DBcontract;
import com.example.asus.wetmonitor_20170523.dbcontrol.PushDevice;
import com.example.asus.wetmonitor_20170523.dbcontrol.TorkenDBhelper;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
public class MainActivity extends ListActivity implements BeaconConsumer {

    public static final String TAG = "WetTAG";
    public static final int delayTime = 1000 * 20;
    public static int sendCount = 0;
    private BeaconManager mBeaconManager;

    static boolean mScanning = false;
    private com.example.asus.wetmonitor_20170523.wetMonitor.DeviceListAdapter mDevicesListAdapter = null;
    static com.example.asus.wetmonitor_20170523.wetMonitor.RegistListAdapter mRegistListAdapter = null;
    public Context mContext;
    private Boolean isFirstClear = false;
    private Boolean isScanEnd = true;
    private Handler clearHandler;
    private Handler sendHandler;
    private ArrayList<String> outputArr, tempArr;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    com.example.asus.wetmonitor_20170523.wetMonitor.Sendtask sendtask = null;
    static String mac = null;
    static Timer timer;
    boolean isRegistAdd = false;
    public ProgressDialog progress_dialog;
    SharedPreferences preferences;
    String username = "";
    String password = "";
    String token = "";
    static boolean isLogin = false;
    boolean isExit = false;
    boolean hasTask = false;
    Timer tExit = new Timer();
    Thread sec5_thread;
    boolean sec5_while = true;
    ProgressDialog dialog;
    //static ArrayList<String> registList;
    static ArrayList<HashMap<String, String>> enableList;
    static SharedPreferences settings;
    static final String data="DATA";
    static final String token1="TOKEN";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        SysApplication.getInstance().addActivity(this);
        mContext = MainActivity.this;
        mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE); //
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        //
        outputArr = new ArrayList<String>();
        settings=getSharedPreferences(data,0);
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        } /*else {
            if (!mBluetoothAdapter.isEnabled())
                mBluetoothAdapter.enable();
        }*/
        mac = getMacAddress(mContext);
        timer = new Timer(true);

        mBeaconManager = BeaconManager.getInstanceForApplication(this);
        clearHandler = new Handler();

        // new Thread(sendRunnable);
        mBeaconManager.getBeaconParsers()
                .add(new BeaconParser().setBeaconLayout("m:2-3=5980,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));

        mBeaconManager.bind(this);
        //timer.schedule(new Sendtask(mac, 0), 0,1000);


        preferences = getSharedPreferences("UserAccount", 0);
        username = preferences.getString("account", "");
        password = preferences.getString("password", "");
        token = preferences.getString("token", "");
        if (!username.equals("")) {
            //timer.schedule(new Sendtask(username, 0), 0);
            getActionBar().setTitle(username);
            isLogin = true;
            if (!haveInternet()) {
                Message msg = new Message();
                msg.what = 3;
                mHandler.sendMessage(msg);
            }
          /*  dialog = new ProgressDialog(MainActivity.this);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("取得資料中...");
            dialog.setIndeterminate(false);
            dialog.setCancelable(false);
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
                    }
                }
            }).start();*/

        } else if (username.equals("")) {
            getActionBar().setTitle("WetMonitor");
            isLogin = false;
        }

        getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView parent, View view, int position, long id) {
                Log.e("CallLogActivity", view.toString() + "position=" + position);
                if (isLogin) {
                    //  String enable = mRegistListAdapter.enable_item.get(position).get("enable");
                    final PushDevice pushDevice = StartActivity.scanDeviceList.get(position);
                    // CharSequence number = ((TextView) view).get`Text();
                    if (!pushDevice.isEnable && !pushDevice.isRegist) {
                        setRegistDialog(position).show();
                    } else if (!pushDevice.isEnable && pushDevice.isRegist) {
                        setUnRegistDialog(position).show();
                    } else if (pushDevice.isEnable && pushDevice.isRegist) {
                        setUnRegistDialog(position).show();
                    }
                }
                return true;
            }
        });

        sec5_thread = new Thread() {
            @Override
            public void run() {
                int count = 0;
                super.run();
                while (sec5_while) {
                    try {
                        sleep(1000);
                        count += 1;
                        if (count == 5) {
                            if (haveInternet()) {
                                getdata();
                                timer.schedule(new Sendtask(username, 0), 0);
                                Message msg = new Message();
                                msg.what = 2;
                                mHandler.sendMessage(msg);
                            } else {
                                Message msg = new Message();
                                msg.what = 3;
                                mHandler.sendMessage(msg);
                            }
                            count = 0;
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };



        new Thread(new Runnable() {
            @Override
            public void run() {
                try {Thread.sleep(5000);
                }catch (Exception e){

                }finally{
                    if(settings.getString(token1,"").equals("")){
                        saveData(MyInstanceIDService.refreshedToken);
                        Log.e("saveData",MyInstanceIDService.refreshedToken);
                    }else{
                        // readData();

                        Log.e("readData",settings.getString(token1 ,""));

                    }
                }
            }
        }).start();
    }



    public void saveData(String setToken){
        settings.edit()
                .putString(token1, setToken)
                .commit();


    }


    private boolean haveInternet() {
        boolean result = false;
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connManager.getActiveNetworkInfo();
        if (info == null || !info.isConnected()) {
            result = false;
        } else {
            if (!info.isAvailable()) {
                result = false;
            } else {
                result = true;
            }
        }

        return result;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isLogin) {
            if (!sec5_thread.isAlive()) {
                sec5_thread.start();
            }
        }
        // getEnableData(username,password);
        mDevicesListAdapter = new com.example.asus.wetmonitor_20170523.wetMonitor.DeviceListAdapter(this);
        mRegistListAdapter = new com.example.asus.wetmonitor_20170523.wetMonitor.RegistListAdapter(this);

        if (!isLogin) {
            mScanning = true;
            mBluetoothAdapter.enable();
            setListAdapter(mDevicesListAdapter);

        } else {
            mScanning = false;
            mBluetoothAdapter.disable();
            setListAdapter(mRegistListAdapter);
            timer.schedule(new Sendtask(username, 0), 0);
            getdata();
        }


        // Automatically start scanning for devices


    }

    ;

    @Override
    protected void onPause() {
        super.onPause();
        // stopService(new Intent(this, com.example.asus.wetmonitor_20170523.wetMonitor.BackGroundService.class));
        mScanning = false;
        // mBluetoothAdapter.disable();
        // mDevicesListAdapter.clearList();
        mBeaconManager.unbind(this);

        //android.os.Process.killProcess(android.os.Process.myPid());
        //mDevicesListAdapter.clearList();
        //     finish();
        // SysApplication.getInstance().exit();
    }

    ;

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // mBeaconManager.unbind(this);
    }

    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            isExit = false;
            hasTask = true;
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!isExit) {
                isExit = true;
                Toast.makeText(this, R.string.double_back_button_to_exist, Toast.LENGTH_SHORT).show();
                if (!hasTask) {
                    tExit.schedule(task, 2000);
                }
            } else {
                //   mBluetoothAdapter.disable();
                SysApplication.getInstance().exit();
            }
        }
        return false;
    }

    private void getdata() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                TorkenDBhelper mdbhelper = new TorkenDBhelper(MainActivity.this);
                SQLiteDatabase db;

                db = mdbhelper.getReadableDatabase();
                Cursor c = db.rawQuery("SELECT * FROM " + DBcontract.DBcol.TABLE_NAME, null);

                c.moveToFirst();
                String itemrId = c.getString(
                        c.getColumnIndexOrThrow(DBcontract.DBcol.COLUMN_Rrgister_ID)
                );

                Log.e("sql:", itemrId);

                token = itemrId;
                db.close();
                String postParameter = "token=" + token;
                Log.e("string=", postParameter);
                URL url = null;

                try {
                    url = new URL("http://120.105.161.201/jpushex/getdata.php?" + postParameter);
                    HttpURLConnection urlConnection = (HttpURLConnection) url
                            .openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setRequestProperty("Accept",
                            "application/json");
                    urlConnection.setRequestProperty("Content-Type",
                            "application/x-www-form-urlencoded");// setting your headers its a json in my case set your appropriate header

                    urlConnection.setDoOutput(true);
                    urlConnection.connect();// setting your connection

                    StringBuffer buffer = new StringBuffer();
                    InputStream is = urlConnection.getInputStream();
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(is));
                    String line = null;
                    while ((line = br.readLine()) != null)
                        buffer.append(line);
                    // reading your response

                    is.close();
                    urlConnection.disconnect();// close your connection

                    String res = buffer.toString();
                    Log.e("res", res);
                    JSONObject jObject = new JSONObject(res);
                    //     JSONObject data = jObject.getJSONObject("data"); // get data object
                    String ret = jObject.getString("RetCode"); // get the name from data.

                    Log.e("ret", ret);
                    Log.e("res", res);

                    if (ret.equals("0")) {

                        JSONArray jsonArr = jObject.getJSONArray("data");
                        for (int i = 0; i < jsonArr.length(); i++) {  // **line 2**
                            JSONObject childJSONObject = jsonArr.getJSONObject(i);
                            String mac = childJSONObject.getString("device");
                            String data = childJSONObject.getString("dataFormated");
                            String rssi = childJSONObject.getString("rssi");
                            int rssivalue = Integer.parseInt(rssi,16);
                            String state = childJSONObject.getString("state");
                            Log.e("mac", mac);
                            Log.e("data", data);
                            Log.e("value", value(data) + "");
                            Log.e("rssi", rssivalue+"");
                            Log.e("state", state);
                            HashMap<String, String> tag_map = new HashMap<String, String>();
                            check(mac, value(data) + "");
                            boolean isExist = check(mac, value(data) + "");
                            if (!isExist) {
                                tag_map.put("tag", mac);
                                tag_map.put("value", value(data) + "");//////////////////
                                tag_map.put("enable", "true");
                                mRegistListAdapter.enable_item.add(tag_map);
                                Message msg = new Message();
                                msg.what = 2;
                                mHandler.sendMessage(msg);
                            }
                            Message msg = new Message();
                            msg.what = 2;
                            mHandler.sendMessage(msg);
                        }
                    } else if (ret.equals("1")) {
                        //token不正確
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        };


        thread.start();

    }

    int value(String data) {
        String value = data.substring(26, 28);
        int valueint = Integer.parseInt(value, 16);
        return valueint;
    }




    boolean check(String tag, String value) {
        for (int i = 0; i < MainActivity.mRegistListAdapter.enable_item.size(); i++) {
            if (MainActivity.mRegistListAdapter.enable_item.get(i).get("tag").equals(tag)) {
                MainActivity.mRegistListAdapter.enable_item.get(i).put("value", value);
                return true;
            }
        }
        return false;
    }

    ;

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        super.onListItemClick(l, v, position, id);
        if (mScanning) {
            setListItemNameDialog(position).show();
        }
    }

    private AlertDialog setListItemNameDialog(int position) {
        final View dialogView = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_setlistitemname, null);
        final BleDevice tmpitem = mDevicesListAdapter.getItemAddress(position);
        AlertDialog.Builder tmpDialog = new AlertDialog.Builder(mContext);
        tmpDialog.setTitle(R.string.dialog_title_setListItemName);
        tmpDialog.setView(dialogView);
        tmpDialog.setPositiveButton(R.string.confirm_button, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                EditText editText = (EditText) dialogView.findViewById(R.id.edittext);
                String tmpString = editText.getText().toString();
                if (tmpString != null) {
                    if (!tmpString.trim().equals("")) {
                        if (tmpitem.getmName().contains("Wet")) {
                            tmpitem.setmName(tmpString + "'s Wet");
                            mDevicesListAdapter.setItemName(tmpitem);
                        } else if (tmpitem.getmName().contains("WT")) {
                            tmpitem.setmName(tmpString + "'s WT");
                            mDevicesListAdapter.setItemName(tmpitem);
                        } else if (tmpitem.getmName().contains("Thermo") || tmpitem.getmName().contains("Temp")) {
                            tmpitem.setmName(tmpString + "'s Temp");
                            mDevicesListAdapter.setItemName(tmpitem);
                        }
                    } else {
                        if (tmpitem.getmName().contains("Wet")) {
                            tmpitem.setmName("WetBeacon");
                            mDevicesListAdapter.setItemName(tmpitem);
                        }else if (tmpitem.getmName().contains("WT")) {
                            tmpitem.setmName("SP.WT");
                            mDevicesListAdapter.setItemName(tmpitem);
                        } else if (tmpitem.getmName().contains("Thermo") || tmpitem.getmName().contains("Temp")) {
                            tmpitem.setmName("ThermoSensor");
                            mDevicesListAdapter.setItemName(tmpitem);
                        }
                        Toast.makeText(mContext, R.string.dialog_Message_NoEnter, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (tmpitem.getmName().contains("Wet")) {
                        tmpitem.setmName("WetBeacon");
                        mDevicesListAdapter.setItemName(tmpitem);
                    } else if (tmpitem.getmName().contains("Thermo") || tmpitem.getmName().contains("Temp")) {
                        tmpitem.setmName("ThermoSensor");
                        mDevicesListAdapter.setItemName(tmpitem);
                    }

                    Toast.makeText(mContext, R.string.dialog_Message_NoEnter, Toast.LENGTH_SHORT).show();
                }
            }
        });
        return tmpDialog.create();
    }

    private AlertDialog setRegistDialog(final int position) {
        // final BleDevice tmpitem = mDevicesListAdapter.getItemAddress(position);
        final PushDevice pushDevice = StartActivity.scanDeviceList.get(position);
        final View dialogView = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_setregist, null);
        AlertDialog.Builder tmpDialog = new AlertDialog.Builder(mContext);
        tmpDialog.setTitle(R.string.tag_regist_ask_title);
        tmpDialog.setMessage(R.string.tag_regist_ask_content);
        tmpDialog.setView(dialogView);
        tmpDialog.setPositiveButton(R.string.register_button, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                if (haveInternet()) {
                    Log.e("註冊的TAGGGGGGGG", pushDevice.getDeviceMac());
                    match(pushDevice.getDeviceMac(), "true");
                    mRegistListAdapter.notifyDataSetChanged();
                } else {
                    Message msg = new Message();
                    msg.what = 3;
                    mHandler.sendMessage(msg);
                }

            }
        });

        tmpDialog.setNegativeButton(R.string.cancel_button, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

            }
        });
        return tmpDialog.create();
    }

    private AlertDialog setUnRegistDialog(final int position) {
        // final BleDevice tmpitem = mDevicesListAdapter.getItemAddress(position);
        final PushDevice pushDevice = StartActivity.scanDeviceList.get(position);
        final View dialogView = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_setregist, null);
        AlertDialog.Builder tmpDialog = new AlertDialog.Builder(mContext);
        tmpDialog.setTitle(R.string.chang_tag_status_title);

        tmpDialog.setView(dialogView);
        tmpDialog.setPositiveButton(R.string.unregister_button, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                if (haveInternet()) {
                    Log.e("註冊的TAGGGGGGGG", pushDevice.getDeviceMac());
                    removematch(pushDevice.getDeviceMac());
                    mRegistListAdapter.notifyDataSetChanged();
                } else {
                    Message msg = new Message();
                    msg.what = 3;
                    mHandler.sendMessage(msg);
                }

            }
        });
        tmpDialog.setNegativeButton(R.string.cancel_button, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
            }
        });
        if (!pushDevice.isEnable) {
            tmpDialog.setMessage(R.string.chang_tag_status_content_tag_disable);
            tmpDialog.setNeutralButton("Enable", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    if (haveInternet()) {
                        Log.e("註冊的TAGGGGGGGG", pushDevice.getDeviceMac());
                        match(pushDevice.getDeviceMac(), "true");
                        mRegistListAdapter.notifyDataSetChanged();
                    } else {
                        Message msg = new Message();
                        msg.what = 3;
                        mHandler.sendMessage(msg);
                    }

                }
            });
        } else if (pushDevice.isEnable) {
            tmpDialog.setMessage(R.string.chang_tag_status_content_tag_enable);
            tmpDialog.setNeutralButton("Disable", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    if (haveInternet()) {

                        Log.e("註冊的TAGGGGGGGG", pushDevice.getDeviceMac());
                        match(pushDevice.getDeviceMac(), "false");
                        mRegistListAdapter.notifyDataSetChanged();
                    } else {
                        Message msg = new Message();
                        msg.what = 3;
                        mHandler.sendMessage(msg);
                    }

                }
            });
        }
        return tmpDialog.create();
    }


    private AlertDialog logoutDialog() {
        final View dialogView = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_setregist, null);
        // final BleDevice tmpitem =
        // mDevicesListAdapter.getItemAddress(position);
        AlertDialog.Builder tmpDialog = new AlertDialog.Builder(mContext);
        tmpDialog.setTitle(R.string.logout_ask_title);
        tmpDialog.setMessage(R.string.logout_ask_content);
        tmpDialog.setView(dialogView);
        tmpDialog.setPositiveButton(R.string.confirm_button, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                if (haveInternet()) {
                    progress_dialog = new ProgressDialog(MainActivity.this);
                    progress_dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progress_dialog.setMessage(getResources().getText(R.string.logout_process).toString());
                    progress_dialog.setIndeterminate(false);
                    progress_dialog.setCancelable(false);
                    progress_dialog.show();
                    logout(username);

                } else {
                    Message msg = new Message();
                    msg.what = 3;
                    mHandler.sendMessage(msg);
                }


            }
        });
        return tmpDialog.create();
    }

    private AlertDialog loginDialog() {
        final View dialogView = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_setregist, null);
        // final BleDevice tmpitem =
        // mDevicesListAdapter.getItemAddress(position);
        AlertDialog.Builder tmpDialog = new AlertDialog.Builder(mContext);
        tmpDialog.setTitle(R.string.login_ask_title);
        tmpDialog.setMessage(R.string.login_ask_content);
        tmpDialog.setView(dialogView);
        tmpDialog.setPositiveButton(R.string.confirm_button, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
              /*  progress_dialog = new ProgressDialog(MainActivity.this);
                progress_dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress_dialog.setMessage("登出中...");
                progress_dialog.setIndeterminate(false);
                progress_dialog.setCancelable(false);
                progress_dialog.show();*/
                // logout(username);
              /*  preferences.edit()
                        .putString("account", null)
                        .putString("password", null)
                        .putString("rid", null)
                        .putString("token", null).commit();*/
                if (haveInternet()) {
                    startActivity(new Intent(MainActivity.this, Login_dialog.class));
                    finish();
                } else {
                    Message msg = new Message();
                    msg.what = 3;
                    mHandler.sendMessage(msg);
                }
            }
        });
        return tmpDialog.create();
    }

    @Override
    public void onBeaconServiceConnect() {
        final Region region = new Region("WetBeaons", null, null, null);
        final Region region2 = new Region("ThermoSensor", null, null, null);
        mBeaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                try {
                    // Log.d(TAG, "didEnterRegion");
                    mBeaconManager.startRangingBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void didExitRegion(Region region) {
                try {
                    // Log.d(TAG, "didExitRegion");
                    mBeaconManager.stopRangingBeaconsInRegion(region);
                    logToClear();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {

            }
        });

	/*	mBeaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
			public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
				if (beacons.size() > 0) {

					for (Beacon beacon : beacons) {
						logToDisplay(beacon.getBluetoothName(), beacon.getBluetoothAddress(),
								Integer.toString(beacon.getManufacturer(), 16), beacon.getId1().toHexString(),
								beacon.getId1().toByteArrayOfSpecifiedEndianness(true), beacon.getDistance(),
								beacon.getRssi(), beacon.getId2().toHexString(), beacon.getId3().toHexString());

					}
					Log.e("BECAON_SCAN","BECAON.SIZE > 0");
					// logToSort();
				}
			}
		});*/

        mBeaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region regin) {
                if (beacons.size() > 0) {

                    for (Beacon beacon : beacons) {
                        logToDisplay(beacon.getBluetoothName(), beacon.getBluetoothAddress(),
                                Integer.toString(beacon.getManufacturer(), 16), beacon.getId1().toHexString(),
                                beacon.getId1().toByteArrayOfSpecifiedEndianness(true), beacon.getDistance(),
                                beacon.getRssi(), beacon.getId2().toHexString(), beacon.getId3().toHexString());

                    }
                    Log.e("BECAON_SCAN", "BECAON.SIZE > 0");
                    // logToSort();
                }
            }
        });

        try {
            mBeaconManager.startMonitoringBeaconsInRegion(region);
            mBeaconManager.startMonitoringBeaconsInRegion(region2);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    Runnable clearRunnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            logToClear();
            isScanEnd = true;
            clearHandler.postDelayed(this, delayTime);
        }
    };
    Runnable sendRunnable = new Runnable() {
        @Override
        public void run() {
            for (; ; ) {
                // TODO Auto-generated method stub
                String Address = mDevicesListAdapter.getDevice(sendCount);
                String ID1_byte = Integer.toString(mDevicesListAdapter.getWetness(sendCount));
                /*
                 * SendThread st = new SendThread(Address, ID1_byte, time());
				 * st.start();
				 */
                sendCount++;
            }
        }
    };

    private void logToDisplay(final String name, final String Address, final String Manufacturer, final String ID1,
                              final byte[] ID1_byte, final double Distance, final int RSSI, final String ID2, final String ID3) {
        runOnUiThread(new Runnable() {
            public void run() {

                // if(name.equals("WetBeacon"))
                {
                    /*
                     * outputArr.add(new String(Address + "&" + ID1_byte + "&" +
					 * time())); if (outputArr.size()==5){ tempArr = new
					 * ArrayList<String>(); tempArr = outputArr;
					 * outputArr.clear(); }
					 */
                    /*
                     * SendThread st = new SendThread(Address, ID1_byte,
					 * time()); st.start();
					 */
                    boolean isExist = false;
                    PushDevice pushDevice = new PushDevice(Address);
                    mDevicesListAdapter.addDevice(name, Address, RSSI, ID1_byte, ID1);
                    mDevicesListAdapter.notifyDataSetChanged();
                    for (int i = 0; i < StartActivity.scanDeviceList.size(); i++) {
                        if (StartActivity.scanDeviceList.get(i).getDeviceMac().equals(Address)) {
                            isExist = true;
                            break;
                        } else
                            isExist = false;
                    }
                    if (!isExist) {
                        StartActivity.scanDeviceList.add(pushDevice);
                    }
                    Log.e("NAME", name);
                    Log.e("ADDRESS", Address);
                    Log.e("RSSI", RSSI + "");
                    Log.e("ID1", ID1);
                    Log.e("BECAON_LOGTODISPLAY", "LOGGGGGGG");
                    /* 只知道第16byte是電量 第21~24byte是第一溫度 第26~29byte是第二溫度 */
                }

            }
        });
    }

    private void logToClear() {
        runOnUiThread(new Runnable() {
            public void run() {
                mDevicesListAdapter.clearList();
                mDevicesListAdapter.notifyDataSetChanged();
            }
        });
    }

    @SuppressLint("SimpleDateFormat")
    private String time() {
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = sDateFormat.format(new java.util.Date());
        return date;
    }

    private void logToSort() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                mDevicesListAdapter.sortDevice();
                mDevicesListAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.scanning, menu);
        menu.findItem(R.id.start_push).setTitle(R.string.push_on);
        menu.findItem(R.id.stop_push).setTitle(R.string.push_off);
        menu.findItem(R.id.exit).setTitle(R.string.exit_app);
        if (!isLogin) {
            // menu.findItem(R.id.scanning_start).setVisible(false);
            menu.findItem(R.id.stop_push).setVisible(false);
            // menu.findItem(R.id.scanning_stop).setVisible(true);
            menu.findItem(R.id.start_push).setVisible(true);//進入推波
            menu.findItem(R.id.scanning_indicator).setActionView(R.layout.progress_indicator);
        } else {
            menu.findItem(R.id.stop_push).setVisible(true);//退出推波,登出
            menu.findItem(R.id.start_push).setVisible(false);
            menu.findItem(R.id.scanning_indicator).setActionView(null);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.stop_push:
                // mDevicesListAdapter.clearList();
                // mDevicesListAdapter.notifyDataSetChanged();
                //mScanning = true;
                //  mBluetoothAdapter.enable();
                // mDevicesListAdapter = new DeviceListAdapter(this);
                logoutDialog().show();
                //setListAdapter(mDevicesListAdapter);

                // addScanningTimeout();
                // mBleWrapper.startScanning();
                break;
            case R.id.start_push:
                //mScanning = true;
                loginDialog().show();
                break;
            case R.id.exit:
                //mBluetoothAdapter.disable();
                //  stopService(new Intent(this, com.example.asus.wetmonitor_20170523.wetMonitor.BackGroundService.class));
                //mDevicesListAdapter.clearList();
                //	android.os.Process.killProcess(android.os.Process.myPid());
                SysApplication.getInstance().exit();
                break;
          /*  case R.id.regist_tag:
                setListAdapter(mRegistListAdapter);
                mScanning = false;
                mBluetoothAdapter.disable();
                //   stopService(new Intent(this, com.example.asus.wetmonitor_20170523.wetMonitor.BackGroundService.class));
                break;
            case R.id.search_tag:
                setListAdapter(mDevicesListAdapter);
                mScanning = true;
                mBluetoothAdapter.enable();
                //   stopService(new Intent(this, com.example.asus.wetmonitor_20170523.wetMonitor.BackGroundService.class));
                break;*/
        }

        invalidateOptionsMenu();
        return true;
    }


 /*   public Boolean isConnected() {
        ConnectivityManager cManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            // do something
            // ��W��
            return true;
        } else {
            // do something
            // ����W��
            return false;
        }
    }*/

    public String getMacAddress(Context context) {
        WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        Log.e("wifiInf", wifiInf.getMacAddress());
        return wifiInf.getMacAddress();
    }

    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    //處理少量資訊或UI
                    //textView.setText(msg.obj.toString());
                    Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    //        Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
                    break;
                case 2:
                    mRegistListAdapter.notifyDataSetChanged();
                    break;
                case 3:
                    Toast.makeText(MainActivity.this, R.string.check_internet, Toast.LENGTH_LONG).show();
                    break;

            }
        }
    };

    private void logout(final String username) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();

                URL url = null;

                try {
                    TorkenDBhelper mdbhelper = new TorkenDBhelper(MainActivity.this);
                    SQLiteDatabase db;

                    db = mdbhelper.getReadableDatabase();
                    Cursor c = db.rawQuery("SELECT * FROM " + DBcontract.DBcol.TABLE_NAME, null);

                    c.moveToFirst();
                    String itemrId = c.getString(
                            c.getColumnIndexOrThrow(DBcontract.DBcol.COLUMN_Rrgister_ID)
                    );

                    Log.e("sql:", itemrId);

                    token = itemrId;
                    Log.e("LOGOUT_token", token);
                    db.close();
                    String postParameter = "token=" + token;
                    Log.e("string=", postParameter);

                    url = new URL("http://120.105.161.201/jpushex/logout.php?" + postParameter);
                    HttpURLConnection urlConnection = (HttpURLConnection) url
                            .openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setRequestProperty("Accept",
                            "application/json");
                    urlConnection.setRequestProperty("Content-Type",
                            "application/x-www-form-urlencoded");// setting your headers its a json in my case set your appropriate header

                    urlConnection.setDoOutput(true);
                    urlConnection.connect();// setting your connection

                    StringBuffer buffer = new StringBuffer();
                    InputStream is = urlConnection.getInputStream();
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(is));
                    String line = null;
                    while ((line = br.readLine()) != null)
                        buffer.append(line);
                    // reading your response

                    is.close();
                    urlConnection.disconnect();// close your connection
                    String res = buffer.toString();
                    Log.e("res", res);
                    JSONObject jObject = new JSONObject(res);
                    //     JSONObject data = jObject.getJSONObject("data"); // get data object
                    String ret = jObject.getString("RetCode"); // get the name from data.
                    String RetMsg = jObject.getString("RetMsg");

                    Log.e("ret", ret);
                    Log.e("RetMsg", RetMsg);


                    if (ret.equals("0")) {
                        Log.e("LOGOUT_RET", "0");
                        preferences.edit()
                                .putString("account", null)
                                .putString("password", null)
                                .putString("rid", null)
                                .putString("token", null).commit();
                        Message msg = new Message();
                        msg.what = 1;
                        progress_dialog.dismiss();
                        msg.obj = getResources().getText(R.string.logout_success);
                        mHandler.sendMessage(msg);

                        sec5_while = false;
                        sec5_thread.interrupt();
                        sec5_thread = null;

                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    } else if (ret.equals("1")) {
                        Log.e("LOGOUT_RET", "1");
                        Message msg = new Message();
                        msg.what = 1;
                        progress_dialog.dismiss();
                        msg.obj = getResources().getText(R.string.logout_fail);
                        mHandler.sendMessage(msg);
                    } else if (ret.equals("2")) {
                        Log.e("LOGOUT_RET", "2");
                        Message msg = new Message();
                        msg.what = 1;
                        progress_dialog.dismiss();
                        msg.obj = getResources().getText(R.string.logout_fail);
                        mHandler.sendMessage(msg);
                    }


                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        };
        thread.start();
    }

    private void match(final String mac, final String ableBoolean) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                String postParameter = "token=" + token + "&device=" + mac + "&enable=" + ableBoolean;
                Log.e("string=", postParameter);
                URL url = null;

                try {
                    url = new URL("http://120.105.161.201/jpushex/device_register.php?" + postParameter);
                    HttpURLConnection urlConnection = (HttpURLConnection) url
                            .openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setRequestProperty("Accept",
                            "application/json");
                    urlConnection.setRequestProperty("Content-Type",
                            "application/x-www-form-urlencoded");// setting your headers its a json in my case set your appropriate header

                    urlConnection.setDoOutput(true);
                    urlConnection.connect();// setting your connection

                    Log.e("response:", urlConnection.getResponseCode() + "");
                    StringBuffer buffer = new StringBuffer();
                    InputStream is = urlConnection.getInputStream();

                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(is));
                    String line = null;
                    while ((line = br.readLine()) != null)
                        buffer.append(line);
                    // reading your response

                    is.close();
                    urlConnection.disconnect();// close your connection
                    String res = buffer.toString();
                    Log.e("res", res);
                    JSONObject jObject = new JSONObject(res);
                    String ret = jObject.getString("RetCode"); // get the name from data.
                    Log.e("RET", ret);
                    String RetMsg = jObject.getString("RetMsg");
                    //          String address = buffer.toString().substring(2);

                    if (ret.equals("0")) {
                        //配對成功
                        timer.schedule(new Sendtask(username, 0), 0);
                        for (int i = 0; i < mRegistListAdapter.enable_item.size(); i++) {
                            if (mRegistListAdapter.enable_item.get(i).get("tag").equals(mac)) {
                                mRegistListAdapter.enable_item.remove(i);
                                //  mRegistListAdapter.notifyDataSetChanged();
                            }
                        }
                        Message msg = new Message();
                        msg.what = 1;
                        if (ableBoolean.equals("true")) {
                            msg.obj = getResources().getText(R.string.enable_success);
                        } else if (ableBoolean.equals("false")) {
                            msg.obj = getResources().getText(R.string.disable_success);
                        }
                        getdata();
                        mHandler.sendMessage(msg);
                        Message msg2 = new Message();
                        msg2.what = 2;
                        mHandler.sendMessage(msg2);

                        Log.e("RetMsg", RetMsg);
                    } else if (ret.equals("1")) {
                        //token不正確
                        Message msg = new Message();
                        msg.what = 1;
                        msg.obj = getResources().getText(R.string.token_not_correct);
                        mHandler.sendMessage(msg);
                        Log.e("RetMsg", RetMsg);
                    } else if (ret.equals("2")) {
                        //tag已和別的手機配對
                        Message msg = new Message();
                        msg.what = 1;
                        msg.obj = getResources().getText(R.string.tag_not_exist_in_database);
                        mHandler.sendMessage(msg);
                        Log.e("RetMsg", RetMsg);
                    } else if (ret.equals("3")) {
                        //tag已和別的手機配對
                        Message msg = new Message();
                        msg.what = 1;
                        msg.obj = "tag已和其他手機配對";
                        mHandler.sendMessage(msg);
                        Log.e("RetMsg", RetMsg);
                    }


                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        };


        thread.start();

    }

    private void removematch(final String mac) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                String postParameter = "token=" + token + "&device=" + mac;
                Log.e("string=", postParameter);
                URL url = null;

                try {
                    url = new URL("http://120.105.161.201/jpushex/device_remove.php?" + postParameter);
                    HttpURLConnection urlConnection = (HttpURLConnection) url
                            .openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setRequestProperty("Accept",
                            "application/json");
                    urlConnection.setRequestProperty("Content-Type",
                            "application/x-www-form-urlencoded");// setting your headers its a json in my case set your appropriate header

                    urlConnection.setDoOutput(true);
                    urlConnection.connect();// setting your connection


                    Log.e("response:", urlConnection.getResponseCode() + "");
                    StringBuffer buffer = new StringBuffer();
                    InputStream is = urlConnection.getInputStream();

                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(is));
                    String line = null;
                    while ((line = br.readLine()) != null)
                        buffer.append(line);
                    // reading your response

                    is.close();
                    urlConnection.disconnect();// close your connection
                    String res = buffer.toString();
                    Log.e("res", res);
                    JSONObject jObject = new JSONObject(res);
                    String ret = jObject.getString("RetCode"); // get the name from data.
                    String RetMsg = jObject.getString("RetMsg");
                    //          String address = buffer.toString().substring(2);

                    if (ret.equals("0")) {
                        //配對成功
                        Message msg = new Message();
                        msg.what = 1;
                        msg.obj = getResources().getText(R.string.unregist_success);
                        mHandler.sendMessage(msg);
                        for (int i = 0; i < mRegistListAdapter.enable_item.size(); i++) {
                            if (mRegistListAdapter.enable_item.get(i).get("tag").equals(mac)) {
                                mRegistListAdapter.enable_item.remove(i);
                                //  mRegistListAdapter.notifyDataSetChanged();
                            }
                        }

                        timer.schedule(new Sendtask(username, 0), 0);
                        getdata();
                        Message msg2 = new Message();
                        msg2.what = 2;
                        mHandler.sendMessage(msg2);
                        Log.e("RetMsg", RetMsg);
                    } else if (ret.equals("1")) {
                        //token不正確
                        Message msg = new Message();
                        msg.what = 1;
                        msg.obj = getResources().getText(R.string.token_not_correct);
                        mHandler.sendMessage(msg);
                        Log.e("RetMsg", RetMsg);
                    } else if (ret.equals("2")) {
                        //tag已和別的手機配對
                        Message msg = new Message();
                        msg.what = 1;
                        msg.obj = getResources().getText(R.string.tag_not_exist_in_database);
                        mHandler.sendMessage(msg);
                        Log.e("RetMsg", RetMsg);
                    } else if (ret.equals("3")) {
                        //tag已和別的手機配對
                        Message msg = new Message();
                        msg.what = 1;
                        msg.obj = "原本就沒配對";
                        mHandler.sendMessage(msg);
                        Log.e("RetMsg", RetMsg);
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        };


        thread.start();

    }


 /*   private void getEnableData(final String username, final String password) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                Log.e("GET_ENABLE","GET");
                Log.e("GET_ENABLE_USER",username);
                Log.e("GET_ENABLE_PASS",password);
                Log.e("GET_ENABLE_RID",StartActivity.RID);
                String postParameter = "username=" + username + "&password=" + password + "&registrationid=" + StartActivity.RID +"&language=zh";
                Log.e("string=", postParameter);
                URL url = null;

                try {
                    url = new URL("http://120.105.161.201/jpushex/loginre.php?" + postParameter);
                    HttpURLConnection urlConnection = (HttpURLConnection) url
                            .openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setRequestProperty("Accept",
                            "application/json");
                    urlConnection.setRequestProperty("Content-Type",
                            "application/x-www-form-urlencoded");// setting your headers its a json in my case set your appropriate header

                    urlConnection.setDoOutput(true);
                    urlConnection.connect();// setting your connection

                    StringBuffer buffer = new StringBuffer();
                    InputStream is = urlConnection.getInputStream();
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(is));
                    String line = null;
                    while ((line = br.readLine()) != null)
                        buffer.append(line);
                    // reading your response

                    is.close();
                    urlConnection.disconnect();// close your connection

                    String res = buffer.toString();
                    Log.e("res",res);
                    JSONObject jObject  = new JSONObject(res);
                    //     JSONObject data = jObject.getJSONObject("data"); // get data object
                    String ret = jObject.getString("RetCode"); // get the name from data.

                    Log.e("GET_ENABLE_ret",ret);
                    Log.e("GET_ENABLE_res",res);


                    if (ret.equals("0")) {
                        Log.e("GET_ENABLE_RETURN","0");
                        String token = jObject.getString("Token");
                        JSONArray jsonArr = jObject.getJSONArray("Devices");
                       // MainActivity.mRegistListAdapter.regist_item = new ArrayList<>();
                        for (int i = 0; i < jsonArr.length(); i++) {  // **line 2**
                            //HashMap<String, String> login_map = new HashMap<>();
                            JSONObject childJSONObject = jsonArr.getJSONObject(i);
                            String mac = childJSONObject.getString("mac_addr");
                            String enable = childJSONObject.getString("Enable");
                            //  login_map.put("mac",mac);
                            // MainActivity.mRegistListAdapter.regist_item.add(mac);
                            //login_map.put("enable",enable);
                            //  MainActivity.enableList.add(login_map);
                            Log.e("enable",enable);
                            Log.e("mac",mac);
                        }
                        Message msg = new Message();
                        msg.what = 3;
                        msg.obj = token;
                        mHandler.sendMessage(msg);


                    } else if (ret.equals("1")) {
                        Log.e("GET_ENABLE_RETURN","1");
                        String RetMsg = jObject.getString("RetMsg");
                        Message msg = new Message();
                        msg.what = 1;
                        msg.obj = "用戶不存在";
                        mHandler.sendMessage(msg);
                    } else if (ret.equals("2")) {
                        Log.e("GET_ENABLE_RETURN","2");
                        String RetMsg = jObject.getString("RetMsg");
                        Message msg = new Message();
                        msg.what = 1;
                        msg.obj = "密碼不匹配";
                        mHandler.sendMessage(msg);
                    } else if (ret.equals("3")) {
                        Log.e("GET_ENABLE_RETURN","3");
                        String RetMsg = jObject.getString("RetMsg");
                        Message msg = new Message();
                        msg.what = 1;
                        msg.obj = "已經登入?";
                        mHandler.sendMessage(msg);
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        };
        thread.start();
    }*/

}
