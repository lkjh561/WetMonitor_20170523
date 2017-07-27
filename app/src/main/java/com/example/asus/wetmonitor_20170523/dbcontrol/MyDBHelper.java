package com.example.asus.wetmonitor_20170523.dbcontrol;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDBHelper extends SQLiteOpenHelper {
	
	// ��Ʈw�W��
    public static final String DATABASE_NAME = "Sinopulsar";
    // ��Ʈw�����A��Ƶ��c���ܪ��ɭԭn���o�ӼƦr�A�q�`�O�[�@
    public static final int VERSION = 1;    
    // ��Ʈw����A�T�w������ܼ�
    private static SQLiteDatabase database;

	public MyDBHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(BleDeviceCourseDAO.CREATE_TABLE);
	}

	// �ݭn��Ʈw������I�s�o�Ӥ�k�A�o�Ӥ�k�b�@�몺���γ����ݭn�ק�
    public static SQLiteDatabase getDatabase(Context context) {
        if (database == null || !database.isOpen()) {
            database = new MyDBHelper(context, DATABASE_NAME, null, VERSION).getWritableDatabase();
        }
        return database;
    }


	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
	    // �R���즳�����
		db.execSQL("DROP TABLE IF EXISTS" + BleDeviceCourseDAO.TABLE_NAME );
		onCreate(db);
	}

}
