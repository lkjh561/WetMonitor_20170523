package com.example.asus.wetmonitor_20170523.wetMonitor;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.asus.wetmonitor_20170523.R;
import com.example.asus.wetmonitor_20170523.dbcontrol.PushDevice;

import java.util.ArrayList;
import java.util.HashMap;
public class RegistListAdapter extends BaseAdapter {
	ArrayList<String> RegistTags ;
	static ArrayList<PushDevice> scanDeviceList = new ArrayList<>();
	public ArrayList<HashMap<String,String>> enable_item = new ArrayList<>();
	ArrayList<String> regist_item = new ArrayList<>();
	private LayoutInflater mInflater;
	public RegistListAdapter(Activity par){
		super();
		
		RegistTags = new ArrayList<String>();
		regist_item.add("123");
		regist_item.add("456");
		mInflater = par.getLayoutInflater();

	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return StartActivity.scanDeviceList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		FieldReferences fields;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.activity_pushing_items, null);
			fields = new FieldReferences();
			fields.TagAddress = (TextView) convertView.findViewById(R.id.push_address_tv);
			fields.TagValue = (TextView) convertView.findViewById(R.id.pushing_tag_value);
			fields.TagImage = (ImageView) convertView.findViewById(R.id.pushImage);
			convertView.setTag(fields);
		} else {
			fields = (FieldReferences) convertView.getTag();
		}

//		String name = MainActivity.mRegistListAdapter.regist_item.get(position);
		fields.TagAddress.setText(StartActivity.scanDeviceList.get(position).getDeviceMac());
		if(tellRegist(StartActivity.scanDeviceList.get(position).getDeviceMac())){
			fields.TagImage.setImageResource(R.drawable.bell_disable);
			fields.TagValue.setText("--");
			StartActivity.scanDeviceList.get(position).isEnable = false;
			StartActivity.scanDeviceList.get(position).isRegist = true;
			if(tellEnable(StartActivity.scanDeviceList.get(position).getDeviceMac())){
				fields.TagImage.setImageResource(R.drawable.bell_enable);
				StartActivity.scanDeviceList.get(position).isEnable = true;
				for(int i = 0 ; i < enable_item.size() ; i++){
					if(StartActivity.scanDeviceList.get(position).getDeviceMac().equals(enable_item.get(i).get("tag"))){
						String value = enable_item.get(i).get("value");

						fields.TagValue.setText(value+"%");
						if (Integer.parseInt(value) >= 70) {
							fields.TagValue.setTextColor(Color.RED);
						} else {
							fields.TagValue.setTextColor(0xff0080ff);
						}
						break;
					}
			}
		}
		}else{
			StartActivity.scanDeviceList.get(position).isEnable = false;
			StartActivity.scanDeviceList.get(position).isRegist = false;
			fields.TagImage.setImageResource(R.drawable.water_drop);
			fields.TagValue.setText("--");
		}





		//fields.TagAddress.setText(regist_item.get(position));

/*
		for(int i = 0 ; i < enable_item.size() ; i++){
			if(MainActivity.mRegistListAdapter.regist_item.get(position).equals(enable_item.get(i).get("tag"))){
				String value = enable_item.get(i).get("value");
				enable_item.get(i).put("enable","0");
				fields.TagValue.setText(value+"%");
				fields.TagImage.setImageResource(R.drawable.bell_enable);
				break;
			}else{
				enable_item.get(i).put("enable","1");
				fields.TagValue.setText("--");
				fields.TagImage.setImageResource(R.drawable.bell_disable);
			}

		}*/


		return convertView;
	}

	@Override
	public  void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}

	private boolean tellRegist(String address) {
		// String n = "WetBeacon";
		// String n2 = "ThermoSensor";
		if (BackGroundService.regist_tag.contains(address)) {
			return true;
		} else
			return false;
	}

	private boolean tellEnable(String address) {
		// String n = "WetBeacon";
		// String n2 = "ThermoSensor";
		for(int i = 0 ; i < enable_item.size() ; i++){
			if(enable_item.get(i).get("tag").equals(address)){
				return true;
			}
		}
			return false;
	}
	private class FieldReferences {
		TextView TagAddress;
		TextView TagValue;
		ImageView TagImage;
	}
}
