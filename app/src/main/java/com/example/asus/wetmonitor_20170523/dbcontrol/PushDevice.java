package com.example.asus.wetmonitor_20170523.dbcontrol;

/**
 * Created by ASUS on 2017/6/11.
 */
public class PushDevice {
    private String mAddress;
    private String mValue;
    public boolean isEnable = false;
    public boolean isRegist = false;
    public PushDevice(String address ){
        this.mAddress  = address;
    }
   public String getDeviceMac(){
        return this.mAddress;
    }
    void setValue(String value){
        mValue = value;
    }
}
