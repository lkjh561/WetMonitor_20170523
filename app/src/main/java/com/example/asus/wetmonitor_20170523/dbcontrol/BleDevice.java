package com.example.asus.wetmonitor_20170523.dbcontrol;

import android.util.Log;


public class BleDevice implements Comparable<BleDevice> {
	
	private long id;
	private String mName;
	private String mAddress;
	private Integer mRSSI;
	private byte[] mRecord;
	private String mID1;
	private String mValue;
	public BleDevice() {}
	public boolean regist = false;
	public BleDevice(String mName, String mAddress, Integer mRSSI,
			byte[] mRecord ,String ID1) {
		// TODO Auto-generated constructor stub
		this.mName = mName;
		this.mAddress = mAddress;
		this.mRSSI = mRSSI;
		this.mRecord = mRecord;
		this.mID1 = ID1;
		
		/* if(BackGroundService.regist_tag.contains(getmAddress())){
			this.regist = true;
		}else
			this.regist = false;*/
	
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getmName() {
		return mName;
	}
	public String ID1() {
		return this.mID1;
	}
	public void setmName(String mName) {
		this.mName = mName;
	}
	
	public String getmAddress() {
		return mAddress;
	}
	public void setmValue(String ID1) {
		this.mValue = transValue(ID1);
	}
	public void setmAddress(String mAddress) {
		this.mAddress = mAddress;
	}
	public void setmID1(String ID1) {
		this.mID1 = ID1;
	}
	public Integer getmRSSI() {
		return mRSSI;
	}

	public void setmRSSI(Integer mRSSI) {
		this.mRSSI = mRSSI;
	}
	public String getmValue() {
		return this.mValue;
	}
	public String getmID1() {
		return mID1;
	}
	public byte[] getmRecord() {
		return mRecord;
	}
	public void setmRecord(byte[] mRecord) {
		this.mRecord = mRecord;
	}

	public String show() {
		return "ID=" + getId() + "/Name=" + getmName() + "/Address="
				+ getmAddress();
	}
	
	public void clear() {
		this.mName = "";
		this.mAddress = "";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof BleDevice))
			return false;
		BleDevice other = (BleDevice) obj;
		if (mAddress == null) {
			if (other.mAddress != null)
				return false;
		} else if (!mAddress.equals(other.getmAddress()))
			return false;

		return true;
	}
	
	
	@Override
	public int compareTo(BleDevice o) {
		// TODO Auto-generated method stub
		if ((getmRecord()[4] & 0xff) == (o.getmRecord()[4] & 0xff))
			return 0;
		if (getmRecord() == null)
			return -1;
		if (o.getmRecord() == null)
			return 1;
		if ((getmRecord()[4] & 0xff) > (o.getmRecord()[4] & 0xff))
			return -1;

		return 1;
	}
	
	///-------------------
	public String transValue(String ID1){
		Log.e("ID1_transValue", ID1);
		byte data1 = (byte) (Integer.parseInt(ID1.substring(24, 26),16) & 0xff); 
		//Byte.parseByte(ID1.substring(24, 26) );
		//Log.e("data1"+ "::::",String.valueOf(data1) +"");
		byte data2 =  (byte) (Integer.parseInt(ID1.substring(26, 28),16) & 0xff); 
		byte data3 =  (byte) (Integer.parseInt(ID1.substring(28, 30),16) & 0xff); 
		byte data4 =  (byte) (Integer.parseInt(ID1.substring(30, 32),16) & 0xff); 
		/*Log.e("data1"+ "::::",data1 +"");
		Log.e("data2"+ "::::",data2+"");
		Log.e("data3"+ "::::",data3+"");
		Log.e("data4"+ "::::",data4+"");*/
		float tem = bytesToFloat(data1,data2,data3,data4);//decodeTempLevel(data,0);
		Log.e("dataaaaaaaaaaaa"+ "::::",+tem+"");	
		return tem+"";
	}
	// transform data
    public static float decodeTempLevel(final byte[] data, final int start) {
        return bytesToFloat(data[start], data[start + 1], data[start + 2], data[start + 3]);
    }

    /**
     * Convert signed bytes to a 32-bit short float value.
     */
    public static float bytesToFloat(byte b0, byte b1, byte b2, byte b3) {
        int mantissa = unsignedToSigned(unsignedByteToInt(b0) + (unsignedByteToInt(b1) << 8) + (unsignedByteToInt(b2) << 16), 24);
        return (float) (mantissa * Math.pow(10, b3));
    }

    /**
     * Convert a signed byte to an unsigned int.
     */
    public static int unsignedByteToInt(byte b) {
        return b & 0xFF;
    }

    /**
     * Convert an unsigned integer value to a two's-complement encoded signed value.
     */
    public static int unsignedToSigned(int unsigned, int size) {
        if ((unsigned & (1 << size - 1)) != 0) {
            unsigned = -1 * ((1 << size - 1) - (unsigned & ((1 << size - 1) - 1)));
        }
        return unsigned;
    }
}
