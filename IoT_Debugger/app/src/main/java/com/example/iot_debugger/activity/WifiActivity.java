package com.example.iot_debugger.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import com.test.service.R;

//引用安卓的类

//创建MainActivity类，该类继承了TabActivity类，并实现了按钮监听事件
public class WifiActivity extends Activity implements
        OnClickListener {

    private EditText edtIP;
    private EditText edtPort;
    EditText edtSend;
    private EditText edtReceiver;

    private Button btnConn;
    private Button btnReturn;
    private Button btnSend;
    private Button btnClean;
    private Button Clean;
    private Button sendon;
    private Button receiveon;
    private byte[] msg = new byte[5];

    private String tag = "MainActivity";

    InputStream in;
    PrintWriter printWriter = null;
    BufferedReader reader;

    Socket mSocket = null;
    public boolean isConnected = false;
    private MyHandler myHandler;
    int count;
    Thread receiverThread;
    public String resulton;

    public static void hideInputManager(Context context, View view){
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (view !=null && imm != null){
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);  //强制隐藏
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        init();
    }

    //声明变量，private声明的变量，只有在本类中可以访问
    //实现Runnale接口的私有MyRecetverRunnable线程
    private class MyReceiverRunnable implements Runnable {

        public void run() {
            //线程要执行的操作
            while (true) {
                Log.i(tag, "---->>client receive....");
                if (isConnected) {
                    if (mSocket != null && mSocket.isConnected()) {
                        ///BUG 切换后要隔一次发送才更改
                        if (receiveon.getText().equals("16进制接收")) {
                            resulton = bytesToString(readFromInputStream(in)); //通过从输入流读取数据，返回给result
                        } else {
                            resulton = asciiToString(readFromInputStream(in)); //通过从输入流读取数据，返回给result
                        }
                        try {
                            if (!resulton.equals("")) {

                                Message msg = new Message();  //获取消息
                                msg.what = 1;                 //设置message
                                Bundle data = new Bundle();
                                data.putString("msg", resulton);
                                msg.setData(data);
                                myHandler.sendMessage(msg);   //发送消息
                            }
                        } catch (Exception e) //捕获异常
                        {
                            Log.e(tag, "--->>read failure!" + e.toString());
                        }
                    }
                }
                try {
                    Thread.sleep(100L);    //线程休眠
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            receiverData(msg.what);
            if (msg.what == 1) {
                String result = msg.getData().get("msg").toString();
                edtReceiver.append(result);
                edtReceiver.append("\n");
            }
        }
    }

    private void init() {

        edtIP = this.findViewById(R.id.id_edt_inputIP);
        edtIP.setText("127.0.0.1");//
        edtPort = this.findViewById(R.id.id_edt_inputport);
        edtPort.setText("8080");
        edtSend = this.findViewById(R.id.id_edt_sendArea);
        edtReceiver = findViewById(R.id.id_edt_input);

        btnSend = findViewById(R.id.id_btn_send);
        btnSend.setOnClickListener(this);
        btnConn = findViewById(R.id.id_btn_connect);
        btnConn.setOnClickListener(this);

        btnReturn = findViewById(R.id.id_btn_return);
        btnReturn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toast
                Intent intent = new Intent(WifiActivity.this, GridActivity.class);
                startActivity(intent);
            }
        });

        btnClean= findViewById(R.id.id_btn_clean);
        btnClean.setOnClickListener(this);
        Clean = findViewById(R.id.button_clean);
        Clean.setOnClickListener(this);
        sendon= findViewById(R.id.buttonfasong);
        sendon.setOnClickListener(this);
        receiveon= findViewById(R.id.buttonjieshou);
        receiveon.setOnClickListener(this);
        myHandler = new MyHandler();
    }

public byte[] readFromInputStream(InputStream in) {
    int count = 0;
    byte[] inDatas = null;
    try {
        while (count == 0) {
            count = in.available();
        }
        inDatas = new byte[count];
        in.read(inDatas);
    } catch (Exception e) {
        e.printStackTrace();
    }
    return inDatas ;
}

    //监听按钮事件，判断是那个按钮按下，随后执行相应的线程

    @Override
    public void onClick(View v) {

        switch (v.getId()) {


            case R.id.id_btn_connect:
                connectThread();
                break;

            case R.id.id_btn_send:
               sendData();
                break;
            case R.id.button_clean:
            edtReceiver.setText("");
                break;
            case R.id.id_btn_clean:
                edtSend.setText("");
                break;
            case R.id.buttonfasong:
                if (sendon.getText().equals("16进制发送")) {
                    sendon.setText("HEX发送");
                } else {
                    sendon.setText("16进制发送");
                }
                break;
            case R.id.buttonjieshou:
                if (receiveon.getText().equals("16进制接收")) {
                    receiveon.setText("HEX接收");
                } else {
                    receiveon.setText("16进制接收");
                }
                break;
        }
    }


    private void receiverData(int flag) {


        if (flag == 2) {
            receiverThread = new Thread(new MyReceiverRunnable());
            receiverThread.start();
            Log.i(tag, "--->>socket 可以通信!");
            btnConn.setText("已连接");
            edtIP.setEnabled(false);
            edtPort.setEnabled(false);
            isConnected = true;
        }

    }

    //数据发送
    private void sendData() {

        try {
            OutputStream socketWriter = mSocket.getOutputStream();
            System.out.println("开始发送");

            if (sendon.getText().equals("16进制发送")) {
                msg = stringToBytes(getHexString());
            }
            else {
                msg = edtSend.getText().toString().trim().getBytes();
            }
            socketWriter.write(msg);
            socketWriter.flush();
            Log.i(tag, "--->> client send data!");
        } catch (Exception e) {
            Log.e(tag, "--->> send failure!" + e.toString());
            Toast.makeText(WifiActivity.this, "提示：发送格式为01 02 03", Toast.LENGTH_SHORT).show();
        }
    }


    /////////////////////////////////////////////
    //建立连接按钮的线程
    private void connectThread() {
        if (!isConnected) {
            new Thread(new Runnable() {
                public void run() {
                    Looper.prepare();
                    Log.i(tag, "---->> connect/close server!");
                    connectServer(edtIP.getText().toString(), edtPort.getText()
                            .toString());
                    isConnected=true;
                }
            }).start();
        } else {
            try {
                if (mSocket != null) {
                    mSocket.close();
                    mSocket = null;
                    Log.i(tag, "--->>重新连server.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            btnConn.setText("断开连接");
            edtIP.setEnabled(true);
            edtPort.setEnabled(true);
            isConnected = false;
        }
    }



    //与服务器连接
    private void connectServer(String ip, String port) {
        try {
            Log.e(tag, "--->>start connect  server !" + ip + ":" + port);

            mSocket = new Socket(ip, Integer.parseInt(port));
            Log.e(tag, "--->>end connect  server!");

            OutputStream outputStream = mSocket.getOutputStream();

            printWriter = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(outputStream,
                            Charset.forName("gb2312"))));

            in = mSocket.getInputStream();
            myHandler.sendEmptyMessage(2);

            showInfo("连接服务器成功");
        } catch (Exception e) {
            isConnected = false;
            showInfo("连接服务器失败");
            Log.e(tag, "exception:" + e.toString());
        }

    }


    private void showInfo(String msg) {
        Toast.makeText(WifiActivity.this, msg, Toast.LENGTH_SHORT).show();
    }


    private String getHexString() {
        String s = edtSend.getText().toString();
        StringBuilder sb = new StringBuilder();
        for (count = 0; count < s.length(); count++) {
            char c = s.charAt(count);
            if (('0' <= c && c <= '9') || ('a' <= c && c <= 'f') ||
                    ('A' <= c && c <= 'F')) {
                sb.append(c);
            }
        }
        if ((sb.length() % 2) != 0) {
            sb.deleteCharAt(sb.length());
        }
        return sb.toString();
    }

    private byte[] stringToBytes(String s) {
        byte[] buf = new byte[s.length() / 2];
        for (int i = 0; i < buf.length; i++) {
            try {
                buf[i] = (byte) Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

        }
        return buf;
    }
    public String bytesToString(byte[] bytes) {
        try {
            final char[] hexArray = "0123456789ABCDEF".toCharArray();
            char[] hexChars = new char[bytes.length * 2];
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                int v = bytes[i] & 0xFF;
                hexChars[i * 2] = hexArray[v >>> 4];
                hexChars[i * 2 + 1] = hexArray[v & 0x0F];

                sb.append(hexChars[i * 2]);
                sb.append(hexChars[i * 2 + 1]);
                sb.append(' ');
            }

            return sb.toString();
        }catch (Exception e) {
            Log.e(tag, "--->> send failure!" + e.toString());

        }
        return null;
    }
    public String asciiToString(byte[] bytes) {
        char[] buf = new char[bytes.length];
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < buf.length; i++) {
            buf[i] = (char) bytes[i];

            sb.append(buf[i]);
        }
        return sb.toString();
    }

}
