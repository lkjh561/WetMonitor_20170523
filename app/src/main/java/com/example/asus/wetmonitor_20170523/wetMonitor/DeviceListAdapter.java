package com.example.asus.wetmonitor_20170523.wetMonitor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus.wetmonitor_20170523.R;
import com.example.asus.wetmonitor_20170523.dbcontrol.BleDevice;
import com.example.asus.wetmonitor_20170523.dbcontrol.BleDeviceCourseDAO;

import java.util.ArrayList;
import java.util.Collections;

@SuppressLint("SimpleDateFormat")
public class DeviceListAdapter extends BaseAdapter {
    public static final String TAG = "DeviceListAdapter";
    // private ArrayList<String> mNames;
    // private ArrayList<String> mAddresses;
    // private ArrayList<Integer> mRSSIs;
    // private ArrayList<byte[]> mRecords;

    public ArrayList<BleDevice> mBleDevices;
    private BleDevice tmpDevice;
    private LayoutInflater mInflater;
    private Activity activity;
    BleDeviceCourseDAO courseDAO;
    private String value = "";
    private String send_value = "";
    private String send_things = "";
    private int send_times = 0;
    private ArrayList<String> send_array = new ArrayList<String>();

    public DeviceListAdapter(Activity par) {
        super();
        // mNames = new ArrayList<String>();
        // mAddresses = new ArrayList<String>();
        // mRSSIs = new ArrayList<Integer>();
        // mRecords = new ArrayList<byte[]>();
        mBleDevices = new ArrayList<BleDevice>();
        mInflater = par.getLayoutInflater();
        courseDAO = new BleDeviceCourseDAO(par);
        activity = par;

    }

    public void addDevice(String name, String address, int rssi, byte[] scanRecord, String ID1) {
        tmpDevice = new BleDevice(name, address, null, null, null);
        if (mBleDevices.contains(tmpDevice) == false) {
            tmpDevice = courseDAO.get(tmpDevice);
            // tmpDevice = courseDAO.insert(tmpDevice);
            tmpDevice.setmRSSI(rssi);
            tmpDevice.setmRecord(scanRecord);
            tmpDevice.setmID1(ID1);
            if (!tellName(name)) {
                tmpDevice.setmValue(ID1);
                value = tmpDevice.getmValue();

            } else {
                value = (int) (scanRecord[4] & 0xff) + "";
            }
            mBleDevices.add(tmpDevice);
            // BackGroundService.sendMessage(new Sendtask(address, value,
            // MainActivity.mac, rssi));
            // MainActivity.timer.schedule(new Sendtask(address, value,
            // MainActivity.mac, rssi), 0);
        } else {
            upDateDevice(address, rssi, scanRecord, true, ID1);
        }

        // if (mAddresses.contains(address) == false) {
        // mNames.add(name);
        // mAddresses.add(address);
        // mRSSIs.add(rssi);
        // mRecords.add(scanRecord);
        // } else {
        // upDateDevice(address, rssi, scanRecord);
        // }
    }

    // public void upDateDevice(String address, int rssi, byte[] scanRecord) {
    // int index = mAddresses.indexOf(address);
    // Log.d(TAG, "Index=" + index);
    // mRSSIs.set(index, rssi);
    // mRecords.set(index, scanRecord);
    // }

    public void upDateDevice(String address, int rssi, byte[] scanRecord, boolean isDevice, String ID1) {
        tmpDevice = new BleDevice(null, address, null, null, null);
        int index = mBleDevices.indexOf(tmpDevice);
        tmpDevice.clear();
        Log.d(TAG, "Index2=" + index);
        // mBleDevices.set(index, );
        mBleDevices.get(index).setmRSSI(rssi);
        mBleDevices.get(index).setmRecord(scanRecord);
        mBleDevices.get(index).setmID1(ID1);
        if (!tellName(mBleDevices.get(index).getmName())) {
            mBleDevices.get(index).setmValue(ID1);
            value = mBleDevices.get(index).getmValue(); // �ū�
            Log.e("UPDATE_TEMPRTSTURE",value);
        } else {
            value = (int) (scanRecord[4] & 0xff) + ""; // ���
        }
        // BackGroundService.sendMessage(new Sendtask(address, value,
        // MainActivity.mac, rssi));

        // �N�C����ƦX���@�Ӧr��A�W��PHP�APHP�A�����ΡC
   /*     send_value = address + "," + value + "," + MainActivity.mac + "," + rssi + ";";

        Log.e("Send_values", send_value);

        send_array.add(send_value);

        send_times++;
        Log.e("Send_times", send_times + "");

        if (send_times == getCount() * 5) {
            // �u���̷s���X��
            for (int i = send_array.size() - getCount() * 2; i < send_array.size(); i++) {
                send_things += send_array.get(i);
            }
            MainActivity.timer.schedule(new com.example.asus.wetmonitor_20170523.wetMonitor.Sendtask(send_things), 0);
            Log.e("Send_things", send_things);
            send_things = "";
            send_array = new ArrayList<String>();
            send_times = 0;

        }*/
        // MainActivity.timer.schedule(new Sendtask("78:A5:04:83:A0:A9" , "87"
        // ,"super87", 16.2), 0);
    }

    public String getDevice(int index) {
        // return mAddresses.get(index);
        return mBleDevices.get(index).getmAddress();
    }

    public int getRssi(int index) {
        // return mRSSIs.get(index);
        return mBleDevices.get(index).getmRSSI();
    }

    public void sortDevice() {
        Collections.sort(mBleDevices);
    }

    public void clearList() {
        // mNames.clear();
        // mAddresses.clear();
        // mRSSIs.clear();
        // mRecords.clear();
        mBleDevices.clear();
    }

    @Override
    public int getCount() {
        // return mAddresses.size();
        return mBleDevices.size();
    }

    @Override
    public Object getItem(int position) {
        return getDevice(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setItemName(String address, String name) {
        Log.d(TAG, "address=" + address);
        // mNames.set(position, name);
        int position = mBleDevices.indexOf(address);
        if (position != -1) {
            mBleDevices.get(position).setmName(name);
            if (courseDAO.update(mBleDevices.get(position))
                    && !(mBleDevices.get(position).getmName().equals("ThermoSensor")
                    || mBleDevices.get(position).getmName().equals("WetBeacon"))) {
                Toast.makeText(activity, "Edit Name Success", Toast.LENGTH_SHORT).show();
            } else {
                // Toast.makeText(activity, "error", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(activity, "position = -1 ,error", Toast.LENGTH_SHORT).show();
        }
    }

    public void setItemName(BleDevice setItem) {
        Log.d(TAG, "address=" + setItem.getmAddress());
        // mNames.set(position, name);
        int position = mBleDevices.indexOf(setItem);
        if (position != -1) {
            mBleDevices.get(position).setmName(setItem.getmName());
            if (courseDAO.update(mBleDevices.get(position))
                    && !(setItem.getmName().equals("ThermoSensor") || setItem.getmName().equals("WetBeacon"))) {
                Toast.makeText(activity, "Edit Name Success", Toast.LENGTH_SHORT).show();
            } else {
                // Toast.makeText(activity, "error", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(activity, "position = -1 ,error", Toast.LENGTH_SHORT).show();
        }
    }

    public BleDevice getItemAddress(int position) {
        return mBleDevices.get(position);
    }

    public int getWetness(int index) {
        byte[] ID1_byte = mBleDevices.get(index).getmRecord();
        return (int) (ID1_byte[4] & 0xff);
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // get already available view or create new if necessary
        FieldReferences fields;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.activity_scanning_item, null);
            fields = new FieldReferences();
            fields.waterImage = (ImageView) convertView.findViewById(R.id.waterImage);

            fields.deviceAddress = (TextView) convertView.findViewById(R.id.deviceAddress);
            fields.deviceName = (TextView) convertView.findViewById(R.id.deviceName);
            fields.deviceRssi = (TextView) convertView.findViewById(R.id.deviceRssi);
            fields.batteryLV = (TextView) convertView.findViewById(R.id.batteryLV);
            fields.smValue = (TextView) convertView.findViewById(R.id.smValue);

            Typeface font1 = Typeface.createFromAsset(activity.getAssets(), "fonts/trebucbd.ttf");
            fields.deviceName.setTypeface(font1);
            Typeface font2 = Typeface.createFromAsset(activity.getAssets(), "fonts/trebuc.ttf");
            fields.batteryLV.setTypeface(font2);

            convertView.setTag(fields);
        } else {
            fields = (FieldReferences) convertView.getTag();
        }

        // set proper values into the view
        // final String name = mNames.get(position);
        // final String address = mAddresses.get(position);
        // final int rssi = mRSSIs.get(position);
        // byte[] ID1_byte = mRecords.get(position);

        final String name = mBleDevices.get(position).getmName();
        final String address = mBleDevices.get(position).getmAddress();
        final int rssi = mBleDevices.get(position).getmRSSI();
        byte[] ID1_byte = mBleDevices.get(position).getmRecord();
        final String tempID1 = mBleDevices.get(position).getmID1();
        String value = mBleDevices.get(position).getmValue();
        String rssiString = (rssi == 0) ? "N/A" : rssi + " dBm";
        // fields.waterImage.setImageDrawable(activity.getResources().getDrawable(R.drawable.water_drop));
        fields.smValue.setVisibility(View.VISIBLE);
        int batLV = 0;
        int sm = 0;
        // Log.e("name",name);

        //Log.e("DEVICE_LIST_REGIST_TAGGG", BackGroundService.regist_tag);

		/*if (!MainActivity.firstLoginCheck) {

				if (BackGroundService.regist_tag.contains(mBleDevices.get(position).getmAddress())) {
					mBleDevices.get(position).regist = true;
				} else
					mBleDevices.get(position).regist = false ;

			MainActivity.firstLoginCheck = true;
		}*/
        if (tellName(name)) {
            fields.waterImage.setImageDrawable(activity.getResources().getDrawable(R.drawable.water_drop));

            batLV = (int) (ID1_byte[2] & 0xff);
            sm = (int) (ID1_byte[4] & 0xff);
            value = sm + "";
            // int sm = (int) (ID1_byte[5] & 0xff);
            if (sm >= 85) {
                fields.smValue.setTextColor(Color.RED);
            } else {
                fields.smValue.setTextColor(0xff0080ff);
            }
            fields.smValue.setText(sm + "%");
        } else {

            fields.waterImage.setImageDrawable(activity.getResources().getDrawable(R.drawable.temp_sensor));
            batLV = (int) (ID1_byte[6] & 0xff);
            // String stringTemp = String.valueOf(ID1_byte[12] & 0xff) + ""
            // +
            // String.valueOf(ID1_byte[13] & 0xff);
			/*
			 * for (int i = 0 ; i < ID1_byte.length;i++){ Log.e("byte"+ i
			 * +"::::",ID1_byte[i]+""); }
			 */
            // Log.e("dataaaaaaaaaaaa"+ "::::",tempID1+"");

            fields.smValue.setText(value + "°C");

        }

        fields.batteryLV.setText(batLV + "%");
        fields.deviceName.setText(name);
        fields.deviceAddress.setText(address);
        fields.deviceRssi.setText(rssiString);

        return convertView;
    }

    private boolean tellName(String name) {
        // String n = "WetBeacon";
        // String n2 = "ThermoSensor";
        if (name.contains("WT") || name.contains("Wet")) {
            return true;
        } else if (name.contains("Thermo") || name.contains("Temp")) {
            return false;
        } else
            return false;
    }

    private boolean tellRegist(String address) {
        // String n = "WetBeacon";
        // String n2 = "ThermoSensor";
        if (BackGroundService.regist_tag.contains(address)) {
            return true;
        } else
            return false;
    }

    public class FieldReferences {
        ImageView waterImage;
        TextView deviceName;
        TextView deviceRssi;
        TextView deviceAddress;
        TextView batteryLV;
        TextView smValue;
    }
}
