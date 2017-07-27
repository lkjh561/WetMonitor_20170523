package com.example.asus.wetmonitor_20170523.wetMonitor;

import android.util.Log;

import com.example.asus.wetmonitor_20170523.dbcontrol.PushDevice;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class Sendtask extends TimerTask {
    String uriAPI = "";
    // String uriAPI = "http://120.105.161.183/u0233016/hospital/Accept.php";
    String strResult = "";
    String msg;
    Boolean get = false;
    int work_number = -1;
    PushDevice pushDevice;

    Sendtask(String send_data/* , String value, String mac, double distance */) {
        // int value = (int) (ID1_byte[4] & 0xff);
        uriAPI = "http://120.105.161.183/go2/Accept3.php";
        this.msg = send_data;
        work_number = 0;
    }

    Sendtask(String address, String ward) {
        uriAPI = "http://120.105.161.183/go2/monitor_change_ward.php";
        this.msg = address + "&" + ward;
        work_number = 1;
    }

    Sendtask(String account, String address, int useless) {
        uriAPI = "http://120.105.161.183/go2/check_regist.php";
        this.msg = account + "&" + address;
        work_number = 2;
    }

    Sendtask(String account, int useless) {
        uriAPI = "http://120.105.161.201/jpushex/GetRegistTagAddress.php";
        this.msg = account;
        work_number = 3;
    }

    public void run() {
        // Log.e("reply:::", sendPost());
        sendPost();
        if (get) {
            switch (work_number) {
                case 0:
                    close();
                    break;
                case 1:
                    close();
                    break;
                case 2:
                    close();
                    break;
                case 3:
                    //MainActivity.mRegistListAdapter.regist_item = new ArrayList<>();
                    //	StartActivity.scanDeviceList = new ArrayList<>();
                    BackGroundService.regist_tag = strResult.trim().replace("﻿", "");
                    Log.e("REGIST_TAG", BackGroundService.regist_tag);
                    String[] spiltTag = BackGroundService.regist_tag.split("&");
                    boolean isExist = false;
                    for (String mac : spiltTag) {
                        pushDevice = new PushDevice(mac);
                    //    Log.e("MAC", mac);
                        for (int i = 0; i < StartActivity.scanDeviceList.size(); i++) {
                            if (StartActivity.scanDeviceList.get(i).getDeviceMac().equals(mac)) {
                                isExist = true;
                                break;
                            }else
                                isExist = false;
                        }
                        if(!isExist){
                            if(!pushDevice.getDeviceMac().equals("false"))
                                StartActivity.scanDeviceList.add(pushDevice);
                        }

                   /*     if (StartActivity.scanDeviceList.contains(pushDevice) == false) {
                                                     Log.e("PushMAC", pushDevice.getDeviceMac());
                        } else
                            Log.e("SCANLIST", "NO CONTAIN");*/
                    }

				/*if(!BackGroundService.regist﻿_tag.equals("")){
                    StartActivity.progress_dialog.dismiss();
				}*/
                    close();
                    break;

            }

        }
    }

    public String sendPost() {
        // �إ�HTTP Post�s�u
        HttpPost httpRequest = new HttpPost(uriAPI);
        // Post�B�@�ǰe�ܼƥ�����NameValuePair[]�}�C�x�s
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("data", this.msg));
        try {
            // �o�XHTTP request
            httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            // ���oHTTP response
            HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
            // Log.e("ss", "AA");
            // �Y���A�X��200 ok
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                // ���X�^���r��
                strResult = EntityUtils.toString(httpResponse.getEntity());
                Log.e("sendtask", strResult);
                // �^�Ǧ^���r��
                get = true;
                return strResult;
            } else {
                Log.e("sendtask", httpResponse.getStatusLine().getStatusCode() + "");
            }
        } catch (ClientProtocolException e) {
            // Toast.makeText(this, e.getMessage().toString(),
            // Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            Log.e("ss", "BB");
        } catch (IOException e) {
            // Toast.makeText(this, e.getMessage().toString(),
            // Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            Log.e("ss", "C");
        } catch (Exception e) {
            // Toast.makeText(this, e.getMessage().toString(),
            // Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            Log.e("ss", e.getMessage());
        }
        return null;
    }

    public void close() {
        this.cancel();
        try {
            this.finalize();
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
};
