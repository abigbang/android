package com.example.yang.iotdevelop9;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static String MqttUserString = "yang";//用户名
    public static String MqttPwdString = "11223344";//密码
    public static String MqttIPString = "47.93.19.134";//IP地址
    public static int MqttPort = 1883;//端口号
    public static String SubscribeString = "/sub";//订阅的主题
    public static String PublishString = "/pub";//发布的主题

    private MyHandler myHandler;

    private SharedPreferences sharedPreferences;//存储数据
    private SharedPreferences.Editor editor;//存储数据

    TextView ReadTextView;
    EditText SendEditText;
    Button SendButton,ClearButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /**
         * 获取存储的MQTT配置数据
         */
        sharedPreferences = MainActivity.this.getSharedPreferences("mqttconfig",MODE_PRIVATE );
        MqttUserString = sharedPreferences.getString("MqttUser", "yang");
        MqttPwdString = sharedPreferences.getString("MqttPwd", "11223344");
        MqttIPString = sharedPreferences.getString("MqttIP", "47.93.19.134");
        MqttPort = sharedPreferences.getInt("MqttPort", 1883);
        SubscribeString = sharedPreferences.getString("MqttSub", "/sub");
        PublishString = sharedPreferences.getString("MqttPub", "/pub");

        myHandler = new MyHandler();

        ReadTextView = findViewById(R.id.textView3);
        ReadTextView.setMovementMethod(new ScrollingMovementMethod());

        SendEditText = findViewById(R.id.editText1);

        SendButton = findViewById(R.id.button2);
        SendButton.setOnClickListener(SendButtonClick);
        ClearButton = findViewById(R.id.button3);
        ClearButton.setOnClickListener(ClearButtonClick);
    }

    private View.OnClickListener ClearButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ReadTextView.setText("");
        }
    };

    /**
     * 发送
     */
    private View.OnClickListener SendButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String SendString = SendEditText.getText().toString().replace(" ","");

            if (SendString.length()>0)
            {
                Intent intent = new Intent();
                intent.setAction("ActivitySendMqttService");
                intent.putExtra("OtherActivitySend","SendData;;"+SendString);
                sendBroadcast(intent);
            }
        }
    };

    /*该类的广播接收程序*/
    private BroadcastReceiver MainActivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try
            {
                String msgString = intent.getStringExtra("MqttServiceSend");

                if(msgString != null)
                {
                    Message msg = myHandler.obtainMessage();
                    Bundle bundle = new Bundle();
                    bundle.putString("MqttServiceSend",msgString);
                    msg.setData(bundle);
                    myHandler.sendMessage(msg);
                }
                msgString = intent.getStringExtra("MqttServiceSendToast");
                if (msgString != null)
                {
                    Toast.makeText(MainActivity.this,msgString,Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
            }
        }
    };


    /**
     * 接受消息，处理消息 ，此Handl线程一块运行
     * */
    class MyHandler extends Handler {
        public MyHandler() {
        }

        public MyHandler(Looper L) {
            super(L);
        }

        // 子类必须重写此方法，接受数据
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            String StringData = bundle.getString("MqttServiceSend");//后台发过来的数据

            if (StringData != null)
            {
                ReadTextView.append(StringData);
                ReadTextView.post(new Runnable() {//让滚动条向下移动,永远显示最新的数据
                    @Override
                    public void run() {
                        final int scrollAmount = ReadTextView.getLayout().getLineTop(ReadTextView.getLineCount()) - ReadTextView.getHeight();
                        if (scrollAmount > 0)
                            ReadTextView.scrollTo(0, scrollAmount);
                        else
                            ReadTextView.scrollTo(0, 0);
                    }
                });
            }
        }
    }


    /**配置MQTT对话框*/
    private void MqttConfigAlertDialog(String Title)
    {
        AlertDialog.Builder MqttConfigAlertDialog = new AlertDialog.Builder(MainActivity.this);
        View MqttConfigView = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_mqtt_config, null);

        final EditText editTextMqttUser = (EditText) MqttConfigView.findViewById(R.id.editTextDialogMC1);//用户名
        final EditText editTextMqttPwd = (EditText) MqttConfigView.findViewById(R.id.editTextDialogMC2);//密码
        final EditText editTextMqttIP = (EditText) MqttConfigView.findViewById(R.id.editTextDialogMC3);//IP地址
        final EditText editTextMqttPort = (EditText) MqttConfigView.findViewById(R.id.editTextDialogMC4);//端口号
        final EditText editTextMqttSub = (EditText) MqttConfigView.findViewById(R.id.editTextDialogMC5);//订阅的主题
        final EditText editTextMqttPub = (EditText) MqttConfigView.findViewById(R.id.editTextDialogMC6);//发布的主题


        editTextMqttUser.setFocusable(true);
        editTextMqttUser.setFocusableInTouchMode(true);
        editTextMqttUser.requestFocus();//获取焦点 光标出现

        MqttConfigAlertDialog.setTitle(Title);


        MqttConfigAlertDialog.setPositiveButton("确定",null);//实现方法在下面,目的是点击按钮不关闭

        MqttConfigAlertDialog.setNegativeButton("默认",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MqttUserString = "yang";//用户名
                MqttPwdString = "11223344";//密码
                MqttIPString = "47.93.19.134";//IP地址
                MqttPort = 1883;//端口号
                SubscribeString = "/pub";//订阅的主题
                PublishString = "/sub";//发布的主题


                editor = sharedPreferences.edit();
                editor.putString("MqttUser", "yang");//用户名
                editor.putString("MqttPwd", "11223344");//密码
                editor.putString("MqttIP", "47.93.19.134");//IP地址
                editor.putInt("MqttPort",1883);//端口号
                editor.putString("MqttSub","/sub");//订阅的主题
                editor.putString("MqttPub","/pub");//发布的主题
                editor.commit();


                Intent intent = new Intent();
                intent.setAction("ActivitySendMqttService");
                intent.putExtra("OtherActivitySend","ResetMqtt;;");
                sendBroadcast(intent);
            }
        });

        MqttConfigAlertDialog.setView(MqttConfigView);//对话框加载视图
//        MqttConfigAlertDialog.show();

        final AlertDialog mqttConfigAlertDialog  = MqttConfigAlertDialog.create();
//        mqttConfigAlertDialog.setCanceledOnTouchOutside(false);//点击外围不消失

        //初始化显示
        mqttConfigAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                editTextMqttUser.setText(MqttUserString);
                editTextMqttPwd.setText(MqttPwdString);
                editTextMqttIP.setText(MqttIPString);
                editTextMqttPort.setText(MqttPort+"");
                editTextMqttSub.setText(SubscribeString);
                editTextMqttPub.setText(PublishString);


                editTextMqttUser.setSelection(editTextMqttUser.getText().length());//将光标移至文字末尾
            }
        });

        mqttConfigAlertDialog.show();//必须先显示.....
        /*点击了确定按钮*/
        mqttConfigAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    String str1 = editTextMqttUser.getText().toString();
                    String str2 = editTextMqttPwd.getText().toString();
                    String str3 = editTextMqttIP.getText().toString();
                    String str4 = editTextMqttPort.getText().toString();
                    String str5 = editTextMqttSub.getText().toString();
                    String str6 = editTextMqttPub.getText().toString();

                    if (str1.length() == 0 || str2.length() == 0 ||str3.length() == 0 ||str4.length() == 0 ||
                            str5.length() == 0 ||str6.length() == 0) {
                        Toast.makeText(getApplicationContext(), "请检查输入",Toast.LENGTH_SHORT).show();
                        return;
                    }

                    MqttUserString = str1;//用户名
                    MqttPwdString = str2;//密码
                    MqttIPString = str3;//IP地址
                    MqttPort = Integer.parseInt(str4);//端口号
                    SubscribeString = str5;//订阅的主题
                    PublishString = str6;//发布的主题


                    editor = sharedPreferences.edit();
                    editor.putString("MqttUser", MqttUserString);//用户名
                    editor.putString("MqttPwd", MqttPwdString);//密码
                    editor.putString("MqttIP", MqttIPString);//IP地址
                    editor.putInt("MqttPort",MqttPort);//端口号
                    editor.putString("MqttSub",SubscribeString);//订阅的主题
                    editor.putString("MqttPub",PublishString);//发布的主题
                    editor.commit();

                    Intent intent = new Intent();
                    intent.setAction("ActivitySendMqttService");
                    intent.putExtra("OtherActivitySend","ResetMqtt;;");
                    sendBroadcast(intent);

                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "存储失败,请检查输入",Toast.LENGTH_SHORT).show();
                }


                mqttConfigAlertDialog.dismiss();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            MqttConfigAlertDialog("MQTT配置");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /** 当活动即将可见时调用 */
    @Override
    protected void onStart()
    {
        Intent startIntent = new Intent(getApplicationContext(), ServiceMqtt.class);
        startService(startIntent); //启动后台服务

        IntentFilter filter = new IntentFilter();//监听的广播
        filter.addAction("Broadcast.MqttServiceSend");
        registerReceiver(MainActivityReceiver, filter);
//        Log.e("err","onStart");
        super.onStart();
    }
    /** 当活动不再可见时调用 */
    @Override
    protected void onStop()
    {

        super.onStop();
    }
    /** 当活动注销时调用 */
    @Override
    protected void onDestroy()
    {
        unregisterReceiver(MainActivityReceiver);
        super.onDestroy();
    }

}
