package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

import vpos.apipackage.PosApiHelper;

/**
 * Created by Administrator on 2017/8/17.
 */

public class McrActivity extends Activity {

    public byte track1[] = new byte[250];
    public byte track2[] = new byte[250];

    public byte track3[] = new byte[250];

    private final static int MSG_MSR_OPEN_FLAG = 0;
    private final static int MSG_MSR_INFO_FLAG = 1;
    private final static int MSG_MSR_CLOSE_FLAG = 2;
    private final static String MSG_MSR_INFO = "msg_msr_info";

    public String tag = "McrActivity";

    TextView textViewMsg = null;
    RelativeLayout progressBar = null;

    boolean isQuit = false;
    boolean isOpen = false;
    int ret = -1;
    int checkCount = 0;
    int successCount = 0;
    int failCount = 0;
    int temp = 0;
    private int RESULT_CODE = 0;

    PosApiHelper posApiHelper=PosApiHelper.getInstance();

    private Context mContext;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //无title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,
                WindowManager.LayoutParams. FLAG_FULLSCREEN);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_mcr);
        textViewMsg = (TextView) this.findViewById(R.id.textView_msg);
        progressBar = (RelativeLayout) this.findViewById(R.id.mcr_bar_progress);

        mContext=McrActivity.this;
    }
    protected void onPause() {
        // TODO Auto-generated method stub
        disableFunctionLaunch(false);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onPause();

        isQuit = true;
    }

    protected void onResume() {
        disableFunctionLaunch(true);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onResume();
        isQuit = false;
        isOpen = false;
        m_MSRThread = new MSR_Thread();
        m_MSRThread.start();
        Log.d("onResume", "m_MSRThread.start()");
    }
    protected void onDestroy() {
        super.onDestroy();
        m_MSRThread.interrupt();
        isOpen = false;

        posApiHelper.McrClose();

        Log.d("onDestroy", "VaMsrCard.Close()");
    }

    private void updateUI(){
        progressBar.setVisibility(View.GONE);
        textViewMsg.setVisibility(View.VISIBLE);
        //linearLayout.setVisibility(View.VISIBLE);
        textViewMsg.setText(R.string.swipe);
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ) {//SCREEN_ORIENTATION_PORTRAIT
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);	//SCREEN_ORIENTATION_LANDSCAPE
        }
    }

    // MSR
    MSR_Thread m_MSRThread = null;
    public class MSR_Thread extends Thread {
        private boolean m_bThreadFinished = false;

        public boolean isThreadFinished() {
            return m_bThreadFinished;
        }

        public void run() {
            Log.e("MSR_Thread[ run ]", "run() begin");
            synchronized (this) {
                if(!isOpen){
                    int reset1 = posApiHelper.McrOpen();   //02 c1 01
                    int reset2 = posApiHelper.McrReset();

                    Log.e("liuhao " ,"reset1 ="+reset1 +" reset2 = "+reset2);
                    if(reset1==0&&reset2==0){
                        Message msg = new Message();
                        msg.what=MSG_MSR_OPEN_FLAG;
                        handler.sendMessage(msg);
                        Log.i("MSR_Thread[ run ]", "msr open and reset success");
                        isOpen = true;
                    }
                    else
                    {
                        Message msg = new Message();
                        msg.what=MSG_MSR_CLOSE_FLAG;
                        handler.sendMessage(msg);
                        Log.i("MSR_Thread[ run ]", "msr open and reset failed");
                    }
                }
                while(!isQuit && isOpen){
                    temp=posApiHelper.McrCheck();
                    Log.d("MSR_Thread[ run ]", "Lib_McrCheck="+temp);
                    while(temp != 0 && !isQuit){   //C1 05
                        try {
                            Thread.sleep(10);
                            Log.d("MSR_Thread[ run ]", "Lib_McrCheck..="+temp);
                            temp=posApiHelper.McrCheck();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if(isQuit){
                        return;
                    }
                    checkCount++;
                    Arrays.fill(track1, (byte) 0x00);
                    Arrays.fill(track2, (byte) 0x00);
                    Arrays.fill(track3, (byte) 0x00);
                    ret = posApiHelper.McrRead((byte)0, (byte)0, track1, track2, track3);  //c1  07
                    Log.e(tag, "Lib_McrRead ret = " + ret);
                    if (ret > 0) {
                        RESULT_CODE = 0;
                        successCount++;
                        Message msg = new Message();
                        Bundle b = new Bundle();
                        String string = "";
                        Log.d("", "ret = " + ret);
                        if(ret <= 7){
                            if((ret & 0x01) == 0x01) {
                                string = "track1:" + new String(track1).trim();
                            }
                            if((ret & 0x02) == 0x02) {
                                string = string + "\n\ntrack2:" + new String(track2).trim();
                            }
                            if((ret & 0x04) == 0x04) {
                                string = string + "\n\ntrack3:" + new String(track3).trim();
                            }
                        }else{
                            RESULT_CODE = -1;
                            string = "Lib_MsrRead check data error";
                            failCount++;
                        }

                        b.putString(MSG_MSR_INFO, string);
                        msg.setData(b);
                        msg.what=MSG_MSR_INFO_FLAG;
                        handler.sendMessage(msg);
                        Log.i("MSR_Thread[ run ]", "Lib_MsrRead succeed!");
                        posApiHelper.SysBeep();
                    } else {
                        RESULT_CODE = -1;
                        failCount++;
                        Message msg = new Message();
                        Bundle b = new Bundle();
                        b.putString(MSG_MSR_INFO, "Lib_MsrRead fail");
                        msg.setData(b);
                        msg.what=MSG_MSR_INFO_FLAG;
                        handler.sendMessage(msg);
                        Log.e("MSR_Thread[ run ]", "Lib_MsrRead failed!");
                    }
                }
            }
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){

                case MSG_MSR_CLOSE_FLAG:

                    Toast.makeText(mContext,R.string.mcrTitle,Toast.LENGTH_SHORT).show();
                    McrActivity.this.finish();

                    break;
                case MSG_MSR_OPEN_FLAG:
                    updateUI();
                    break;
                case MSG_MSR_INFO_FLAG:
                    Bundle b = msg.getData();
                    String strInfo = b.getString(MSG_MSR_INFO);
                    //textViewMsg.setText(strInfo + "\n\ncheckCount = " + checkCount + "\nsuccessCount = " + successCount + "\nfailCount = " + failCount);
                    if (RESULT_CODE == -1) {
                        textViewMsg.setText(strInfo + "\nFailed");
                    } else {
                        textViewMsg.setText(strInfo + "\nSucceed");
                    }
                    Log.d("Msr", strInfo);
                    break;
                default:
                    break;
            }
        }
    };

    // disable the power key when the device is boot from alarm but not ipo boot
    private static final String DISABLE_FUNCTION_LAUNCH_ACTION = "android.intent.action.DISABLE_FUNCTION_LAUNCH";
    private void disableFunctionLaunch(boolean state) {
        Intent disablePowerKeyIntent = new Intent(
                DISABLE_FUNCTION_LAUNCH_ACTION);
        if (state) {
            disablePowerKeyIntent.putExtra("state", true);
        } else {
            disablePowerKeyIntent.putExtra("state", false);
        }
        sendBroadcast(disablePowerKeyIntent);
    }
}
