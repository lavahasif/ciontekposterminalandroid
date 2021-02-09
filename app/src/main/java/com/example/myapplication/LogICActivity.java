package com.example.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import vpos.apipackage.ByteUtil;
import vpos.apipackage.PosApiHelper;

public class LogICActivity extends Activity {

    private static final int OPCODE_4428_READ = 0;
    private static final int OPCODE_4428_WRITE = 1;
    private static final int OPCODE_4442_READ = 2;
    private static final int OPCODE_4442_WRITE = 3;

    private final static int SYS_INFO_FLAG = 1;

    private byte data[] = new byte[512];
    private byte outLen[] = new byte[2];
    private static final byte RSP_SELECT_0 = (byte) 0x90;
    private static final byte RSP_SELECT_1 = (byte) 0x00;
    private static final byte RSP_VERIFICTION_4428_0 = (byte) 0x90;
    private static final byte RSP_VERIFICTION_4428_1 = (byte) 0xFF;
    private static final byte RSP_VERIFICTION_4442_0 = (byte) 0x90;
    private static final byte RSP_VERIFICTION_4442_1 = (byte) 0x07;

    private String hexStr ="";


    private int length = 0;
    TextView tvMsg;
    EditText edContent;

    PosApiHelper posApiHelper = PosApiHelper.getInstance();

    static String inputPwd = "";
    int ret;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //无title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_logic);

        tvMsg = (TextView) findViewById(R.id.tvMsg);
        tvMsg.setMovementMethod(new ScrollingMovementMethod());

        edContent = (EditText) findViewById(R.id.edContent);

    }

    public void OnClick4428Write(View view) {
        if (mTestThread != null && mTestThread.IsThreadFinished() == false) {
            Log.e("", "return, return, return");
            return;
        }
        mTestThread = new TestThread(OPCODE_4428_WRITE);
        mTestThread.start();
    }

    public void OnClick4428Read(View view) {
        if (mTestThread != null && mTestThread.IsThreadFinished() == false) {
            Log.e("", "return, return, return");
            return;
        }
        mTestThread = new TestThread(OPCODE_4428_READ);
        mTestThread.start();
    }

    public void OnClick4442Write(View view) {
        if (mTestThread != null && mTestThread.IsThreadFinished() == false) {
            Log.e("", "return, return, return");
            return;
        }
        mTestThread = new TestThread(OPCODE_4442_WRITE);
        mTestThread.start();
    }

    public void OnClick4442Read(View view) {
        if (mTestThread != null && mTestThread.IsThreadFinished() == false) {
            Log.e("", "return, return, return");
            return;
        }
        mTestThread = new TestThread(OPCODE_4442_READ);
        mTestThread.start();
    }


    TestThread mTestThread = null;

    private class TestThread extends Thread {
        int mOpCode;
        boolean isThreadFinished = false;

        public TestThread(int mOpCode) {
            this.mOpCode = mOpCode;
        }

        public boolean IsThreadFinished() {
            return isThreadFinished;
        }

        @Override
        public void run() {
            super.run();

            synchronized (this) {
                isThreadFinished = false;
                switch (mOpCode) {
                    case OPCODE_4428_READ:

                        SendMsg("test 4428 read... ");

                        ret = posApiHelper.LogicPowerOn();

                        if (ret == 0) {
                            //select card CMD
                            data[0] = (byte) 0xFF;
                            data[1] = (byte) 0xA4;
                            data[2] = (byte) 0x00;
                            data[3] = (byte) 0x00;
                            data[4] = (byte) 0x01;
                            data[5] = (byte) 0x05;
                            length = 6;
                            ret = posApiHelper.LogicCardDispatcher(data, length, data, outLen);
                            if (ret != 0) {
                                SendMsg("uart comm failed");
                                isThreadFinished = true;
                                return;
                            }else {
                                if(!(data[0]== RSP_SELECT_0 &&data[1]== RSP_SELECT_1)){
                                    SendMsg(" select rsp failed !!!~~");
                                    isThreadFinished = true;
                                    return;
                                }
                            }

                            //read CMD
                            data[0] = (byte) 0xFF;
                            data[1] = (byte) 0xB0;
                            data[2] = (byte) 0x00;
                            data[3] = (byte) 0x00;
                            data[4] = (byte) 0x64;
                            length = 5;
                            ret = posApiHelper.LogicCardDispatcher(data, length, data, outLen);
                            if (ret != 0) {
                                SendMsg("memory card 4428 check Failed...");
                                isThreadFinished = true;
                                return;
                            } else {
                                int len = ByteUtil.bytesToInt(outLen);
                                Log.e("liuhao", "  len : " + len);
                                SendMsg("Read memory card 4428 success!!\n\n outLen : " + len + "\ndata : " + ByteUtil.bytearrayToHexString(data, len));
                            }
                        } else {
                            if (ret == -2043) {
                                SendMsg("Power ON failed...No Card~~ ret= " + ret);
                            } else {
                                SendMsg("Power ON failed... ret= " + ret);
                            }
                            isThreadFinished = true;

                            return;
                        }
                        break;

                    case OPCODE_4442_READ:

                        SendMsg("test 4442 Read... ");

                        ret = posApiHelper.LogicPowerOn();

                        if (ret == 0) {
                            //select card CMD
                            data[0] = (byte) 0xFF;
                            data[1] = (byte) 0xA4;
                            data[2] = (byte) 0x00;
                            data[3] = (byte) 0x00;
                            data[4] = (byte) 0x01;
                            data[5] = (byte) 0x06;
                            length = 6;
                            ret = posApiHelper.LogicCardDispatcher(data, length, data, outLen);

                            hexStr = ByteUtil.bytearrayToHexString(data,ByteUtil.bytesToInt(outLen));
                            hexStr = hexStr.substring(0,ByteUtil.bytesToInt(outLen)*2);
                            Log.e("liuhao","hexStr = " + hexStr);

                            if (ret != 0) {
                                SendMsg("uart comm failed");
                                isThreadFinished = true;
                                return;
                            }else {
                                if(!(data[0] == RSP_SELECT_0) && (data [1] == RSP_SELECT_1)){
                                    SendMsg(" select rsp failed !~~~");
                                    isThreadFinished = true;
                                    return;
                                }
                            }

                            //read CMD
                            data[0] = (byte) 0xFF;
                            data[1] = (byte) 0xB0;
                            data[2] = (byte) 0x00;
                            data[3] = (byte) 0x00;
                            data[4] = (byte) 0x64;
                            length = 5;
                            ret = posApiHelper.LogicCardDispatcher(data, length, data, outLen);
                            if (ret != 0) {
                                SendMsg("memory card 4442  Failed...");
                                isThreadFinished = true;
                                return;
                            } else {
                                int len = ByteUtil.bytesToInt(outLen);
                                Log.e("liuhao", "  len : " + len);
                                SendMsg("Read memory card 4442 success!!\n\n outLen : " + len + "\ndata : " + ByteUtil.bytearrayToHexString(data, len));
                            }
                        } else {
                            if (ret == -2043) {
                                SendMsg("Power ON failed...No Card~~ ret= " + ret);
                            } else {
                                SendMsg("Power ON failed... ret= " + ret);
                            }
                            isThreadFinished = true;
                            return;
                        }
                        break;

                    case OPCODE_4428_WRITE:

                        SendMsg("test ICC_4428 Write.. ");
                        final EditText pwd4428 = new EditText(LogICActivity.this);
                        pwd4428.setTextColor(getResources().getColor(R.color.accent));
                        pwd4428.setBackground(getResources().getDrawable(R.drawable.shape_msg));
                        pwd4428.setPadding(20,5,20,5);

                        final TextView tvTitle_4428 = new TextView(LogICActivity.this);
                        tvTitle_4428.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                        tvTitle_4428.setPadding(25, 15, 25, 15);
                        tvTitle_4428.setText("Please enter your password");
                        tvTitle_4428.setTextSize(22);
                        tvTitle_4428.setTextColor(getResources().getColor(R.color.black));
                        tvTitle_4428.setGravity(Gravity.CENTER);

                        runOnUiThread(new Runnable() {
                            public void run() {
                                AlertDialog dlgPwd = new AlertDialog.Builder(LogICActivity.this, R.style.mDlgTheme)
                                        .setCustomTitle(tvTitle_4428)
                                        .setView(pwd4428)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                inputPwd = pwd4428.getText().toString().trim();
                                                if (inputPwd.length() <= 0) {
                                                    SendMsg("write fail \n  Please enter your password !!!");
                                                    isThreadFinished = true;
                                                    return;
                                                } else {

                                                    ret = posApiHelper.LogicPowerOn();

                                                    if (ret == 0) {
                                                        //select card CMD
                                                        data[0] = (byte) 0xFF;
                                                        data[1] = (byte) 0xA4;
                                                        data[2] = (byte) 0x00;
                                                        data[3] = (byte) 0x00;
                                                        data[4] = (byte) 0x01;
                                                        data[5] = (byte) 0x05;
                                                        length = 6;
                                                        ret = posApiHelper.LogicCardDispatcher(data, length, data, outLen);
                                                        if (ret != 0) {
                                                            SendMsg("uart comm failed!!!~~");
                                                            isThreadFinished = true;
                                                            return;
                                                        }else {
                                                            if(!(data[0]== RSP_SELECT_0 &&data[1]== RSP_SELECT_1)){
                                                                SendMsg("select rsp failed !!!~~");
                                                                isThreadFinished = true;
                                                                return;
                                                            }
                                                        }
                                                        //verifiction card CMD
                                                        data[0] = (byte) 0xFF;
                                                        data[1] = (byte) 0x20;
                                                        data[2] = (byte) 0x00;
                                                        data[3] = (byte) 0x00;
                                                        data[4] = (byte) 0x02;
                                                        //password
                                                        byte[] dataTmp = ByteUtil.StringToHexBytes(inputPwd);
//                                                        Log.e("liuhao", "asc: " + );
                                                        Log.e("liuhao", "inputPwd len: " +dataTmp.length);

                                                        for (int i = 0; i < dataTmp.length; i++) {
                                                            data[4 + 1 + i] = dataTmp[i];
                                                        }
                                                        //
                                                        //  data[5] = (byte) 0xFF;
                                                        //data[6] = (byte) 0xFF;
                                                        length = 4 + 1 + dataTmp.length;
                                                        Log.e("liuhao", "data pwd cmd: " + ByteUtil.byte2String(data));

                                                        ret = posApiHelper.LogicCardDispatcher(data, length, data, outLen);

                                                        if (ret != 0) {
                                                            SendMsg("uart comm failed!!!~~~");
                                                            isThreadFinished = true;
                                                            return;
                                                        }else {
                                                            if(!(data[0]== RSP_VERIFICTION_4428_0 &&data[1]== RSP_VERIFICTION_4428_1)){
                                                                Log.e("liuhao", "data out: " + ByteUtil.byte2String(data));
                                                                SendMsg(" wrong password !!!~~");
                                                                isThreadFinished = true;
                                                                return;
                                                            }
                                                        }

                                                        //write content
                                                        String strData4428 = edContent.getText().toString().trim();
                                                        if (strData4428.length() <= 0) {
                                                            SendMsg(getResources().getString(R.string.writeTip) + "~~~");
                                                            runOnUiThread(new Runnable() {
                                                                public void run() {
                                                                    edContent.setFocusable(true);
                                                                }
                                                            });
                                                            isThreadFinished = true;
                                                            return;
                                                        }

                                                        data[0] = (byte) 0xFF;
                                                        data[1] = (byte) 0xD0;
                                                        data[2] = (byte) 0x00;
                                                        data[3] = (byte) 0x00;

                                                        //write data
                                                        dataTmp = ByteUtil.StringToHexBytes(strData4428);
                                                        //data len
                                                        data[4] = (byte) dataTmp.length;
                                                        for (int i = 0; i < dataTmp.length; i++) {
                                                            data[4 + 1 + i] = dataTmp[i];
                                                        }

                                                        length = 4 + 1 + dataTmp.length;
                                                        Log.e("liuhao", "data write: " + ByteUtil.byte2String(data));
                                                        ret = posApiHelper.LogicCardDispatcher(data, length, data, outLen);
                                                        if (ret != 0) {
                                                            SendMsg("memory card 4428  Failed...");
                                                            isThreadFinished = true;
                                                            return;
                                                        } else {
//                                                            String strOutLen = ByteUtil.bytearrayToHexString(outLen, 2).trim();
//                                                            int len = Integer.parseInt(strOutLen, 16);
                                                            SendMsg("memory card 4428 Write uccess!!\n\n ");
                                                        }
                                                    } else {
                                                        if (ret == -2043) {
                                                            SendMsg("Power ON failed...No Card~~ ret= " + ret);
                                                        } else {
                                                            SendMsg("Power ON failed... ret= " + ret);
                                                        }
                                                        isThreadFinished = true;
                                                        return;
                                                    }

                                                }
                                            }
                                        })
                                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                                isThreadFinished = true;
                                                return;
                                            }
                                        })
                                        .show();

                                dlgPwd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            }
                        });

                        break;

                    case OPCODE_4442_WRITE:

                        SendMsg("test ICC_4442 Write.. ");
                        final EditText pwd4442 = new EditText(LogICActivity.this);
                        pwd4442.setTextColor(getResources().getColor(R.color.accent));
                        pwd4442.setBackground(getResources().getDrawable(R.drawable.shape_msg));
                        pwd4442.setPadding(20,5,20,5);

                        final TextView tvTitle_4442 = new TextView(LogICActivity.this);
                        tvTitle_4442.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                        tvTitle_4442.setPadding(25, 15, 25, 15);
                        tvTitle_4442.setText("Please enter your password");
                        tvTitle_4442.setTextSize(22);
                        tvTitle_4442.setTextColor(getResources().getColor(R.color.black));
                        tvTitle_4442.setGravity(Gravity.CENTER);

                        runOnUiThread(new Runnable() {
                            public void run() {
                                AlertDialog dlgPwd = new AlertDialog.Builder(LogICActivity.this, R.style.mDlgTheme)
                                        .setCustomTitle(tvTitle_4442)
                                        .setView(pwd4442)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                inputPwd = pwd4442.getText().toString().trim();
                                                if (inputPwd.length() <= 0) {
                                                    SendMsg("write fail \n  Please enter your password !!!");
                                                    isThreadFinished = true;
                                                    return;
                                                } else {

                                                    ret = posApiHelper.LogicPowerOn();

                                                    Log.e("liuhao", "OPCODE_4442_WRITE LogicdPowerOn");

                                                    if (ret == 0) {
                                                        //select card CMD
                                                        data[0] = (byte) 0xFF;
                                                        data[1] = (byte) 0xA4;
                                                        data[2] = (byte) 0x00;
                                                        data[3] = (byte) 0x00;
                                                        data[4] = (byte) 0x01;
                                                        data[5] = (byte) 0x06;
                                                        length = 6;
                                                        ret = posApiHelper.LogicCardDispatcher(data, length, data, outLen);
                                                        if (ret != 0) {
                                                            SendMsg("uart comm failed!!!~~~");
                                                            isThreadFinished = true;
                                                            return;
                                                        }else {
                                                            if(!(data[0]== RSP_SELECT_0 &&data[1]== RSP_SELECT_1)){
                                                                SendMsg("select rsp failed!!!~~");
                                                                isThreadFinished = true;
                                                                return;
                                                            }
                                                        }
                                                        //verifiction card CMD
                                                        data[0] = (byte) 0xFF;
                                                        data[1] = (byte) 0x20;
                                                        data[2] = (byte) 0x00;
                                                        data[3] = (byte) 0x00;
                                                        data[4] = (byte) 0x03;
                                                        //password
                                                        byte[] dataTmp = ByteUtil.StringToHexBytes(inputPwd);
//                                                        Log.e("liuhao", "asc: " + );
                                                        Log.e("liuhao", "inputPwd len: " +dataTmp.length);

                                                        for (int i = 0; i < dataTmp.length; i++) {
                                                            data[4 + 1 + i] = dataTmp[i];
                                                        }
                                                       length = 4 + 1 + dataTmp.length;
//                                                        data[5] = (byte) 0xFF;
//                                                        data[6] = (byte) 0xFF;
//                                                        data[7] = (byte) 0xFF;
//                                                        length = 4+1 +3;
                                                        Log.e("liuhao", "data pwd cmd: " + ByteUtil.byte2String(data));

                                                        ret = posApiHelper.LogicCardDispatcher(data, length, data, outLen);

                                                        if (ret != 0) {
                                                            SendMsg("uart comm failed!!!~~~");
                                                            isThreadFinished = true;
                                                            return;
                                                        }else {
                                                            Log.e("liuhao", "data out: " + ByteUtil.byte2String(data));

                                                            if(!(data[0]== RSP_VERIFICTION_4442_0 &&data[1]== RSP_VERIFICTION_4442_1)){
                                                                SendMsg(" wrong password !!!~~");
                                                                isThreadFinished = true;
                                                                return;
                                                            }
                                                        }

                                                        //write content
                                                        String strData4442 = edContent.getText().toString().trim();
                                                        if (strData4442.length() <= 0) {
                                                            SendMsg(getResources().getString(R.string.writeTip) + "~~~");
                                                            runOnUiThread(new Runnable() {
                                                                public void run() {
                                                                    edContent.setFocusable(true);
                                                                }
                                                            });
                                                            isThreadFinished = true;
                                                            return;
                                                        }

                                                        data[0] = (byte) 0xFF;
                                                        data[1] = (byte) 0xD0;
                                                        data[2] = (byte) 0x00;
                                                        data[3] = (byte) 0x00;

                                                        //write data
                                                        dataTmp = ByteUtil.StringToHexBytes(strData4442);
                                                        //data len
                                                        data[4] = (byte) dataTmp.length;
                                                        for (int i = 0; i < dataTmp.length; i++) {
                                                            data[4 + 1 + i] = dataTmp[i];
                                                        }

                                                        length = 4 + 1 + dataTmp.length;
                                                        Log.e("liuhao", "data write: " + ByteUtil.byte2String(data));
                                                        ret = posApiHelper.LogicCardDispatcher(data, length, data, outLen);
                                                        if (ret != 0) {
                                                            SendMsg("memory card 4442  Failed...");
                                                            isThreadFinished = true;
                                                            return;
                                                        } else {
//                                                            String strOutLen = ByteUtil.bytearrayToHexString(outLen, 2).trim();
//                                                            int len = Integer.parseInt(strOutLen, 16);
                                                            SendMsg("memory card 4442 Write uccess!!\n\n ");
                                                        }
                                                    } else {
                                                        if (ret == -2043) {
                                                            SendMsg("Power ON failed...No Card~~ ret= " + ret);
                                                        } else {
                                                            SendMsg("Power ON failed... ret= " + ret);
                                                        }
                                                        isThreadFinished = true;
                                                        return;
                                                    }

                                                }
                                            }
                                        })
                                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                                isThreadFinished = true;
                                                return;
                                            }
                                        })
                                        .show();

                                dlgPwd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            }
                        });

                        break;

                }
                isThreadFinished = true;
            }
        }
    }

    public void SendMsg(String strInfo) {
        Message msg = new Message();
        msg.what = SYS_INFO_FLAG;
        Bundle b = new Bundle();
        b.putString("MSG", strInfo);
        msg.setData(b);
        handler.sendMessage(msg);
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SYS_INFO_FLAG:
                    Bundle b = msg.getData();
                    String strInfo = b.getString("MSG");
                    tvMsg.setText(strInfo);
                    break;
            }
        }
    };

    protected void onResume() {
        // TODO Auto-generated method stub
        disableFunctionLaunch(true);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onResume();
    }

    @Override
    protected void onPause() {
        disableFunctionLaunch(false);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onPause();
    }

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
