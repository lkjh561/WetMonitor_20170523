package com.example.asus.wetmonitor_20170523.wetMonitor;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus.wetmonitor_20170523.R;
import com.example.asus.wetmonitor_20170523.dbcontrol.DBcontract;
import com.example.asus.wetmonitor_20170523.dbcontrol.TorkenDBhelper;

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
import java.util.ArrayList;

import static com.example.asus.wetmonitor_20170523.wetMonitor.MainActivity.token1;


public class Login_dialog extends Activity {
    Button login_btn, regist_btn, logout_btn;
    EditText account_ed, password_ed;
    static String username = "";
    String userpassword = "";
    String rid = StartActivity.RID;
    TextView rid_textView , account_TextView , password_TextView;
    String token = "";
    SharedPreferences preferences;
    MainActivity main = new MainActivity();
    // 87888788787878878878878878887888

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login_dialog);
        SysApplication.getInstance().addActivity(this);
        login_btn = (Button) findViewById(R.id.loginbutton);
        regist_btn = (Button) findViewById(R.id.registbutton);
        //  logout_btn = (Button)findViewById(R.id.logutbutton);
        account_ed = (EditText) findViewById(R.id.account_edit);
        password_ed = (EditText) findViewById(R.id.pwd_edit);
        rid_textView = (TextView) findViewById(R.id.rid_tv);
        rid_textView.setText(rid);
        account_TextView = (TextView)findViewById(R.id.account_tv);
        password_TextView = (TextView)findViewById(R.id.password_tv);
        account_TextView.setText(R.string.login_page_ac);//帳號
        password_TextView.setText(R.string.login_page_pw);//密碼
        login_btn.setOnClickListener(login);
        regist_btn.setOnClickListener(register);
        login_btn.setText(R.string.login_page_login_button);
        regist_btn.setText(R.string.login_page_register_button);
        preferences = getSharedPreferences("UserAccount", 0);
        username = preferences.getString("account", "");
        userpassword = preferences.getString("password", "");
       // rid = JPushInterface.getRegistrationID(getApplicationContext());
        if (!username.equals("") && !userpassword.equals("")) {
            //   startActivity(new Intent(Login_dialog.this,MainActivity.class));
            finish();
        }

    }

    @Override
    public void finish() {
        super.finish();
        if (username.equals("") || userpassword.equals("")) {
            //   startActivity(new Intent(Login_dialog.this,MainActivity.class));
            SysApplication.getInstance().exit();
        }

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
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    //處理少量資訊或UI
                    //textView.setText(msg.obj.toString());
                        Toast.makeText(Login_dialog.this, msg.obj.toString() , Toast.LENGTH_SHORT).show();
                    //        Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
                    break;

                case 2:
                    //處理少量資訊或UI
                    //textView.setText("jpush:資料更新!!");
                    Toast.makeText(Login_dialog.this, "jpush:資料更新!!", Toast.LENGTH_SHORT).show();
                    //        Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
                    break;

                case 3:

                    String tokentmp = msg.obj.toString();

//////////////////////////////////////////////////insert/////////////////
                    TorkenDBhelper mdbhelper = new TorkenDBhelper(getApplicationContext());
                    SQLiteDatabase db = mdbhelper.getWritableDatabase();


                    String count = "SELECT * FROM rid";
                    Cursor mcursor = db.rawQuery(count, null);
                    mcursor.moveToFirst();
                    int icount = mcursor.getCount();
                    if (icount > 0) {
                        String id = "1"; //修改id為1的資料

                        ContentValues values = new ContentValues();

                        values.put(DBcontract.DBcol.COLUMN_Rrgister_ID, tokentmp);


                        db.update(DBcontract.DBcol.TABLE_NAME, values, DBcontract.DBcol._ID + "=" + id, null);
                    } else {
                        // Create a new map of values, where column names are the keys
                        ContentValues values = new ContentValues();
                        values.put(DBcontract.DBcol.COLUMN_Rrgister_ID, tokentmp);

                        db.insert(
                                DBcontract.DBcol.TABLE_NAME,
                                null,
                                values);

                        Log.e("sql:", "insert");
                    }


//////////////////////////////////////////////////insert/////////////////
//////////////////////////////////////////////////select/////////////////
                    db = mdbhelper.getReadableDatabase();
                    Cursor c = db.rawQuery("SELECT * FROM " + DBcontract.DBcol.TABLE_NAME + " WHERE _id = 1", null);

                    c.moveToFirst();
                    String itemrId = c.getString(
                            c.getColumnIndexOrThrow(DBcontract.DBcol.COLUMN_Rrgister_ID)
                    );

                    Log.e("sql:", itemrId);
//////////////////////////////////////////////////select/////////////////
                    token = itemrId;
                    //textView.setText("登入成功");
                    Toast.makeText(Login_dialog.this, getResources().getText(R.string.login_success_toast) + "Token:"+token, Toast.LENGTH_SHORT).show();

                    getSharedPreferences("UserAccount", 0).edit()
                            .putString("account", username)
                            .putString("password", userpassword)
                            .putString("rid", rid)
                            .putString("token", token).commit();

                    // startActivity(new Intent(Login_dialog.this,MainActivity.class));
                    //  SysApplication.getInstance().exit();
                    startActivity(new Intent(Login_dialog.this, MainActivity.class));

                    //tokentext.setText("token:"+token);
                    break;
            }
        }
    };


    View.OnClickListener login = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            if (haveInternet()) {
                if (!account_ed.getText().toString().equals("") && !password_ed.getText().toString().equals("")) {
                    username = account_ed.getText().toString();
                    userpassword = password_ed.getText().toString();
                    login(username, userpassword);
                /*account = account_ed.getText().toString();
                finish();*/
                } else {
                    Toast.makeText(Login_dialog.this, R.string.login_can_not_null, Toast.LENGTH_SHORT).show();
                }
            } else
                Toast.makeText(Login_dialog.this,R.string.check_internet, Toast.LENGTH_LONG).show();


        }
    };

    View.OnClickListener register = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (haveInternet()) {
                if (!account_ed.getText().toString().equals("") && !password_ed.getText().toString().equals("")) {
                    String username = account_ed.getText().toString();
                    String password = password_ed.getText().toString();
                    register(username, password);
                    //   rid = JPushInterface.getRegistrationID(getApplicationContext());
                    // rid_textView.setText(rid);
                } else {
                    Toast.makeText(Login_dialog.this, R.string.login_can_not_null, Toast.LENGTH_SHORT).show();
                }
            }else
                Toast.makeText(Login_dialog.this, R.string.check_internet, Toast.LENGTH_LONG).show();
        }

    };

    private void register(final String username, final String password) {

        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();

                String postParameter = "username=" + username + "&password=" + password;
                Log.e("string=", postParameter);
                URL url = null;

                try {
                    url = new URL("http://120.105.161.201/jpushex/user_register.php?" + postParameter);
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
                    JSONObject jObject = new JSONObject(res);
                    String ret = jObject.getString("RetCode");
                    Log.e("ret", ret);
                    Log.e("res:", res);

                    if (ret.equals("0")) {
                        Message msg = new Message();
                        msg.what = 1;
                        msg.obj = getResources().getText(R.string.regist_account_success);
                        mHandler.sendMessage(msg);
                    } else if (ret.equals("1")) {
                        Message msg = new Message();
                        msg.what = 1;
                        msg.obj =getResources().getText(R.string.regist_account_fail);
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

    private void login(final String username, final String password) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                String token2=MainActivity.settings.getString(token1,"");
              //  rid = JPushInterface.getRegistrationID(getApplicationContext());
                String language = (String) getResources().getText(R.string.language);
                String postParameter = "username=" + username + "&password=" + password + "&registrationid=" + token2 + "&language="+language;
                Log.e("string=", postParameter);
                URL url = null;

                try {
                    url = new URL("http://120.105.161.201/jpushex/login.php?" + postParameter);
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
                        String token = jObject.getString("Token");
                        JSONArray jsonArr = jObject.getJSONArray("Devices");
                        MainActivity.mRegistListAdapter.regist_item = new ArrayList<>();
                        for (int i = 0; i < jsonArr.length(); i++) {  // **line 2**
                            //HashMap<String, String> login_map = new HashMap<>();
                            JSONObject childJSONObject = jsonArr.getJSONObject(i);
                            String mac = childJSONObject.getString("mac_addr");
                            String enable = childJSONObject.getString("Enable");
                            //  login_map.put("mac",mac);
                            // MainActivity.mRegistListAdapter.regist_item.add(mac);
                            //login_map.put("enable",enable);
                            //  MainActivity.enableList.add(login_map);
                            Log.e("enable", enable);
                            Log.e("mac", mac);
                        }
                        Message msg = new Message();
                        msg.what = 3;
                        msg.obj = token;
                        mHandler.sendMessage(msg);


                    } else if (ret.equals("1")) {
                        String RetMsg = jObject.getString("RetMsg");

                        Message msg = new Message();
                        msg.what = 1;
                        msg.obj = getResources().getText(R.string.login_account_not_exist_toast);
                        mHandler.sendMessage(msg);
                    } else if (ret.equals("2")) {
                        String RetMsg = jObject.getString("RetMsg");
                        Message msg = new Message();
                        msg.what = 1;

                        msg.obj = getResources().getText(R.string.login_password_not_correct_toast);
                        mHandler.sendMessage(msg);
                    } else if (ret.equals("3")) {
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
    }


}
