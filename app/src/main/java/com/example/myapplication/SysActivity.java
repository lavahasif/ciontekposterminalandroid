package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;

import vpos.apipackage.PosApiHelper;

/**
 * Created by Administrator on 2017/8/17.
 */

public class SysActivity extends Activity implements View.OnClickListener {

    public static final int OPCODE_BEEP_TEST = 0;
    public static final int OPCODE_POWER_ON = 1;
    public static final int OPCODE_POWER_OFF = 2;
    public static final int OPCODE_LED_ON = 3;
    public static final int OPCODE_LED_OFF = 4;
    public static final int OPCODE_VERSION = 5;

    public static String[] MY_PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.MOUNT_UNMOUNT_FILESYSTEMS"};

    public static final int REQUEST_EXTERNAL_STORAGE = 1;


    private final String TAG = "SysActivity";

    byte version[] = new byte[9];

    TextView tvMsg = null;

    Button btnBeep, btnVersion, btnUpdate, btnLedOn, btnLedOff, btnPowerOn, btnPowerOff;

    int ret = 0;

    PosApiHelper posApiHelper = PosApiHelper.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        PosApiHelper.getInstance().SysSetpower(1);

        //无title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_sys);

        tvMsg = (TextView) findViewById(R.id.textview);

//        btnBeep = (Button) findViewById(R.id.btnBeep);
        btnVersion = (Button) findViewById(R.id.btnVersion);
        btnUpdate = (Button) findViewById(R.id.btnUpdate);

        btnLedOn = (Button) findViewById(R.id.btnLedOn);
        btnLedOff = (Button) findViewById(R.id.btnLedOff);
        btnPowerOn = (Button) findViewById(R.id.btnPowerOn);
        btnPowerOff = (Button) findViewById(R.id.btnPowerOff);

//        btnBeep.setOnClickListener(this);
        btnVersion.setOnClickListener(this);
        btnUpdate.setOnClickListener(this);

        btnLedOn.setOnClickListener(this);
        btnLedOff.setOnClickListener(this);
        btnPowerOn.setOnClickListener(this);
        btnPowerOff.setOnClickListener(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        PosApiHelper.getInstance().SysSetPower(0);

        //close Led
        startTestSys(OPCODE_LED_OFF);
    }

    protected void onResume() {
        // TODO Auto-generated method stub
        disableFunctionLaunch(true);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        PosApiHelper.getInstance().SysSetPower(1);
    }

    @Override
    protected void onPause() {
        disableFunctionLaunch(false);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onPause();
    }

    void startTestSys(int OpCode) {
        switch (OpCode) {
            case OPCODE_BEEP_TEST:
                tvMsg.setText("Test Beep...");
                ret = posApiHelper.SysBeep();
                if(ret==0){
                    tvMsg.setText("Beep Success!");
                }else{
                    tvMsg.setText("Beep Fail!");
                }
                break;
            case OPCODE_LED_ON:
                tvMsg.setText("LED On Test....");
                startTestLed(1, 1);
                startTestLed(2, 1);
                startTestLed(3, 1);
                startTestLed(4, 1);
                break;
            case OPCODE_LED_OFF:
                tvMsg.setText("LED Off Test...");
                startTestLed(1, 0);
                startTestLed(2, 0);
                startTestLed(3, 0);
                startTestLed(4, 0);
                break;
            case OPCODE_POWER_ON:
                tvMsg.setText("Power On Testing...");
                ret = posApiHelper.SysSetPower(1);
                break;
            case OPCODE_POWER_OFF:
                tvMsg.setText("Power Off Testing...");
                ret = posApiHelper.SysSetPower(0);
                break;
            case OPCODE_VERSION:
                ret = posApiHelper.SysGetVersion(version);

                if (ret == 0) {
//                    if (version[6] == -1 && version[7] == -1 && version[8] == -1) {
//                        tvMsg.setText("MCU App  Version: V" + version[0] + "." + version[1] + "." + version[2] +
//                                "\nLib Version: V" + version[3] + "." + version[4] + "." + version[5] +
//                                "\nSucceed"
//                        );
//                    } else {
//                        tvMsg.setText("MCU App  Version: V" + version[0] + "." + version[1] + "." + version[2] +
//                                "\nLib Version: V" + version[3] + "." + version[4] + "." + version[5] +
//                                "\nSucceed");
//                    }

                    tvMsg.setText("MCU App  Version: V" + version[0] + "." + version[1] + "." + version[2] +
                                "\nLib Version: V" + version[3] + "." + version[4] + "." + version[5] +
                                "\nSucceed");

                } else {
                    tvMsg.setText("Get_Version Failed");
                }
                break;
            default:
                break;
        }
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void restartApp() {

        disableFunctionLaunch(false);
        android.os.Process.killProcess(android.os.Process.myPid());
//        Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
//        PendingIntent restartIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
//        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent); // 1秒钟后重启应用
//        System.exit(0);
    }

    ProgressDialog updateDlg = null;

    private void startUpdate() {
        Log.e(TAG, "startUpdate  ........ 00");

        disableFunctionLaunch(true);
        updateDlg = ProgressDialog.show(this, null, getString(R.string.isUpdating), false, false);

        new Thread() {
            @Override
            public void run() {
                super.run();
                int ret = posApiHelper.SysUpdate();
                Log.e(TAG, "SysUpdate ret = " + ret);
                if (ret == 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateDlg.cancel();
                            //升级成功 重启应用
                            tvMsg.setText(R.string.update_finish);
                        }
                    });

                    new Thread() {
                        public void run() {
                            try {
                                sleep(2000);
                                restartApp();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateDlg.cancel();
                            tvMsg.setText(R.string.update_fail);
                        }
                    });
                }
            }
        }.start();
    }

    /**
     * @Description: Request permission
     * 申请权限
     */
    private void requestPermission() {
        //检测是否有写的权限
        //Check if there is write permission
        int checkCallPhonePermission = ContextCompat.checkSelfPermission(SysActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
            // 没有写文件的权限，去申请读写文件的权限，系统会弹出权限许可对话框
            //Without the permission to Write, to apply for the permission to Read and Write, the system will pop up the permission dialog
            ActivityCompat.requestPermissions(SysActivity.this, MY_PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        } else {
            updateMcu();
        }
    }

    /**
     * a callback for request permission
     * 注册权限申请回调
     *
     * @param requestCode  申请码
     * @param permissions  申请的权限
     * @param grantResults 结果
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateMcu();
            }
        }
    }

    private void updateMcu() {

        tvMsg.setText("Update...");

        File file01 = null ,file1 = null,file02 = null,file2 = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            file01 = new File("/storage/emulated/0/CS10C_App.bin");
            file1 = new File("/storage/emulated/0/MCU_C_App.bin");
        } else {
            file01 = new File("/storage/sdcard0/CS10C_App.bin");
            file1 = new File("/storage/sdcard0/MCU_C_App.bin");
        }

        if (!file01.exists()&&!file1.exists()) {
            Toast.makeText(getApplicationContext(), getString(R.string.file_not_found), Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this).setTitle(R.string.update)
                .setMessage(R.string.update_or_not)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startUpdate();
                        dialog.cancel();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        dialog.cancel();
                    }
                })
                .show();
    }

    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.btnBeep:
//                startTestSys(OPCODE_BEEP_TEST);
//                break;
            case R.id.btnLedOn:
                startTestSys(OPCODE_LED_ON);
                break;
            case R.id.btnLedOff:
                startTestSys(OPCODE_LED_OFF);
                break;
            case R.id.btnPowerOn:
                startTestSys(OPCODE_POWER_ON);
                break;
            case R.id.btnPowerOff:
                startTestSys(OPCODE_POWER_OFF);
                break;
            case R.id.btnVersion:
                startTestSys(OPCODE_VERSION);
                break;
            case R.id.btnUpdate:
                //Determine if the current Android version is >=23
                // 判断Android版本是否大于23
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermission();
                } else {
                    updateMcu();
                }
                break;
        }
    }

    /*
     * @Date : 20171201
     * @Description : Setting LED state
     * 1 - > open led
     * 0 - > close led
     */
    private void startTestLed(final int testCode, final int mode) {

        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    ret = posApiHelper.SysSetLedMode(testCode, mode);
                    final String txt = mode == 1 ? "Open" : "Close";
                    if (ret == 0) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                tvMsg.setText("LED " + /* testCode + " " + */ txt + " Succeed");
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                tvMsg.setText("LED " + /* testCode + " " + */ txt + " Failed");
                            }
                        });
                    }
                    sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
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
