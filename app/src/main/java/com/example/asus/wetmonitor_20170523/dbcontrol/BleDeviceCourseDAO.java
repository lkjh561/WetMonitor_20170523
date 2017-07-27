package com.example.asus.wetmonitor_20170523.dbcontrol;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class BleDeviceCourseDAO {
	public static final String TAG = "BleDeviceCourseDAO";

	// ���W��
	public static final String TABLE_NAME = "bledevice_table";

	// �s��������W�١A�T�w����
	public static final String KEY_ID = "_id";

	// �䥦������W��
	public static final String FIELD_DEVICE_NAME = "device_name";
	public static final String FIELD_USER_ADDRESS = "device_address";

	// �ϥΤW���ŧi���ܼƫإߪ�檺SQL���O
	public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_NAME + " (" + KEY_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + FIELD_DEVICE_NAME
			+ " TEXT  DEFAULT NULL, " + FIELD_USER_ADDRESS
			+ " TEXT  DEFAULT NULL UNIQUE)";

	// ��Ʈw����
	private SQLiteDatabase db;

	// �غc�l�A�@�몺���γ����ݭn�ק�
	public BleDeviceCourseDAO(Context context) {
		db = com.example.asus.wetmonitor_20170523.dbcontrol.MyDBHelper.getDatabase(context);
	}

	// ������Ʈw�A�@�몺���γ����ݭn�ק�
	public void close() {
		db.close();
	}

	// �s�W�Ѽƫ��w������
	public BleDevice insert(BleDevice item) {
		// �إ߷ǳƷs�W��ƪ�ContentValues����
		ContentValues cv = new ContentValues();

		// �[�JContentValues����]�˪��s�W���
		// �Ĥ@�ӰѼƬO���W�١A �ĤG�ӰѼƬO��쪺���
		cv.put(FIELD_DEVICE_NAME, item.getmName());
		cv.put(FIELD_USER_ADDRESS, item.getmAddress());

		// �s�W�@����ƨè��o�s��
//		long id = db.replace(TABLE_NAME, null, cv);
		long id = db.insert(TABLE_NAME, null, cv);
		// �]�w�s��
		item.setId(id);
		// �^�ǵ��G
		return item;
	}

	// �ק�Ѽƫ��w������
	public boolean update(BleDevice item) {
		// �إ߷ǳƭק��ƪ�ContentValues����
		ContentValues cv = new ContentValues();
		Log.d(TAG,item.show());
		// �[�JContentValues����]�˪��ק���
		// �Ĥ@�ӰѼƬO���W�١A �ĤG�ӰѼƬO��쪺���
		cv.put(FIELD_DEVICE_NAME, item.getmName());
		cv.put(FIELD_USER_ADDRESS, item.getmAddress());

		// �]�w�ק��ƪ����󬰽s��
		String where = KEY_ID + "=" + item.getId();

		// ����ק��ƨæ^�ǭק諸��Ƽƶq�O�_���\
		return db.update(TABLE_NAME, cv, where, null) > 0;
	}

	// �R���Ѽƫ��w�s�������
	public boolean delete(long id) {
		// �]�w���󬰽s���A�榡���u���W��=��ơv
		String where = KEY_ID + "=" + id;
		// �R�����w�s����ƨæ^�ǧR���O�_���\
		return db.delete(TABLE_NAME, where, null) > 0;
	}

	// Ū���Ҧ��O�Ƹ��
	public List<BleDevice> getAll() {
		List<BleDevice> result = new ArrayList<BleDevice>();
		Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null,
				null, null);

		while (cursor.moveToNext()) {
			result.add(getRecord(cursor));
		}

		cursor.close();
		return result;
	}

	// ���o���w�s������ƪ���
	public BleDevice get(long id) {
		// �ǳƦ^�ǵ��G�Ϊ�����
		BleDevice item = null;
		// �ϥνs�����d�߱���
		String where = KEY_ID + "=" + id;
		// ����d��
		Cursor result = db.query(TABLE_NAME, null, where, null, null, null,
				null, null);

		// �p�G���d�ߵ��G
		if (result.moveToFirst()) {
			// Ū���]�ˤ@����ƪ�����
			item = getRecord(result);
		}

		// ����Cursor����
		result.close();
		// �^�ǵ��G
		return item;
	}

	// ���o���w�W�٪���ƪ���
	public BleDevice get(String address) {
		Log.d(address, "DAO_GET:" + address);
		BleDevice item = null;
		String where = FIELD_USER_ADDRESS + " = '" + address + "'";
		Cursor result = db.query(TABLE_NAME, null, where, null, null, null,
				null);

		if (result.getCount() > 0) {
			result.moveToNext();
			item = getRecord(result);
		}

		result.close();
		if (item != null)
			return item;
		else {
			item = new BleDevice();
			return item;
		}
	}
	
	// ���o���w�W�٪���ƪ���
		public BleDevice get(BleDevice tmpDevice) {
			BleDevice item = null;
			String where = FIELD_USER_ADDRESS + " = '" + tmpDevice.getmAddress() + "'";
			Cursor result = db.query(TABLE_NAME, null, where, null, null, null,
					null);

			if (result.getCount() > 0) {
				result.moveToNext();
				item = getRecord(result);
			}

			result.close();
			if (item != null)
				return item;
			else {
				return insert(tmpDevice);
			}
		}

	// ��Cursor�ثe����ƥ]�ˬ�����
	public BleDevice getRecord(Cursor cursor) {
		// �ǳƦ^�ǵ��G�Ϊ�����
		BleDevice result = new BleDevice();

		result.setId(cursor.getLong(0));
		result.setmName(cursor.getString(1));
		result.setmAddress(cursor.getString(2));

		// �^�ǵ��G
		return result;
	}

	// ���o��Ƽƶq
	public int getCount() {
		int result = 0;

		Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME, null);

		if (cursor.moveToNext()) {
			result = cursor.getInt(0);
		}

		return result;
	}
}
