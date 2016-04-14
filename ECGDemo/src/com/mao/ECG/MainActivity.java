package com.mao.ECG;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;
import java.util.Random;


public class MainActivity extends Activity {

    private static final int MSG_DATA_CHANGE = 0x11;
    private ECGView _ECG;
    private Handler mHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _ECG = (ECGView) findViewById(R.id.electrocardiogram);
        _ECG.setMaxPointAmount(100);
        _ECG.setRemovedPointNum(2);
        _ECG.setEveryNPoint(5);
        _ECG.setEveryNPointRefresh(1);
        _ECG.setEffticeValue(400);
        _ECG.setMaxYNumber(6000f);
        _ECG.setAlarmMessage(4800, 1000, "ตอมห", "ธ฿มห");
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                switch (msg.what) {
                    case MSG_DATA_CHANGE:
                        _ECG.setLinePoint((int)(msg.arg2));
                        break;
                    default:
                        break;
                }
                super.handleMessage(msg);
            }
        };

        new Thread() {
            public void run() {
            	while(true){
            		Message message = new Message();
                    message.what = MSG_DATA_CHANGE;
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    message.arg2 =new Random().nextInt(5000);
                    mHandler.sendMessage(message);
                	
            	}
            }

            ;
        }.start();
    }

}
