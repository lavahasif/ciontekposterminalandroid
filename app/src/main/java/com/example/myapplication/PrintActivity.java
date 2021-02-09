package com.example.myapplication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;

import java.util.Timer;

import vpos.apipackage.PosApiHelper;
import vpos.apipackage.Print;
import vpos.apipackage.PrintInitException;
import vpos.apipackage.Sys;


/**
 * Created by Administrator on 2017/8/17.
 */

public class PrintActivity extends Activity {

    public String tag = "PrintActivity";

    final int RPINT_CONSUME = 0;
    final int PRINT_UNICODE = 1;
    final int PRINT_BMP = 2;
    final int PRINT_BARCODE = 4;
    final int PRINT_CYCLE = 5;
    final int PRINT_LONGER = 7;
    final int PRINT_OPEN = 8;

    private RadioGroup rg = null;
    private Timer timer;
    private Timer timer2;
    private BroadcastReceiver receiver;
    private IntentFilter filter;
    private int voltage_level;
    private int BatteryV;
    SharedPreferences preferences;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    private RadioButton rb_high;
    private RadioButton rb_middle;
    private RadioButton rb_low;
    private RadioButton radioButton_4;
    private RadioButton radioButton_5;
    private Button gb_test;
    private Button gb_unicode;
    private Button gb_barcode;
    private Button btnBmp;
    private final static int ENABLE_RG = 10;
    private final static int DISABLE_RG = 11;

    TextView textViewMsg = null;
    TextView textViewGray = null;
    int ret = -1;
    private boolean m_bThreadFinished = true;

    private boolean is_cycle = false;
    private int cycle_num = 0;

    private int RESULT_CODE = 0;
    //private Pos pos;

    int IsWorking = 0;

    PosApiHelper posApiHelper = PosApiHelper.getInstance();

    Intent mPrintServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //无title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_print);
        //linearLayout = (LinearLayout) this.findViewById(R.id.widget_layout_print);
        textViewMsg = (TextView) this.findViewById(R.id.textView_msg);
        textViewGray = (TextView) this.findViewById(R.id.textview_Gray);
        rg = (RadioGroup) this.findViewById(R.id.rg_Gray_type);
        rb_high = (RadioButton) findViewById(R.id.RadioButton_high);
        rb_middle = (RadioButton) findViewById(R.id.RadioButton_middle);
        rb_low = (RadioButton) findViewById(R.id.radioButton_low);
        radioButton_4 = (RadioButton) findViewById(R.id.radioButton_4);
        radioButton_5 = (RadioButton) findViewById(R.id.radioButton_5);
        gb_test = (Button) findViewById(R.id.button_consume);
        gb_unicode = (Button) findViewById(R.id.button_unicode);
        gb_barcode = (Button) findViewById(R.id.button_barcode);
        btnBmp = (Button) findViewById(R.id.btnBmp);
        //gb_printCycle = (Button) findViewById(R.id.printCycle);

        init_Gray();


        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {

                if (printThread != null && !printThread.isThreadFinished()) {

                    Log.e(tag, "Thread is still running...");
                    return;
                }

                String strGray = getResources().getString(R.string.selectGray);

                switch (checkedId) {
                    case R.id.radioButton_low:
                        textViewGray.setText(strGray + "3");
                        posApiHelper.PrintSetGray(3);
                        setValue(3);

                        break;
                    case R.id.RadioButton_middle:
                        textViewGray.setText(strGray + "2");
                        posApiHelper.PrintSetGray(2);
                        setValue(2);

                        break;
                    case R.id.RadioButton_high:
                        textViewGray.setText(strGray + "1");
                        posApiHelper.PrintSetGray(1);
                        setValue(1);
                        break;

                    case R.id.radioButton_4:
                        textViewGray.setText(strGray + "4");
                        posApiHelper.PrintSetGray(4);
                        setValue(4);
                        break;
                    case R.id.radioButton_5:
                        textViewGray.setText(strGray + "5");
                        posApiHelper.PrintSetGray(5);
                        setValue(5);
                        break;
                }
            }
        });
    }

    private void setValue(int val) {
        sp = getSharedPreferences("Gray", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("value", val);
        editor.commit();
    }

    private int getValue() {
        sp = getSharedPreferences("Gray", MODE_PRIVATE);
        int value = sp.getInt("value", 2);
        return value;
    }

    private void init_Gray() {
        int flag = getValue();
        posApiHelper.PrintSetGray(flag);

        String strGray = getResources().getString(R.string.selectGray);

        if (flag == 3) {
            rb_low.setChecked(true);
            textViewGray.setText(strGray + "3");
        } else if (flag == 2) {
            rb_middle.setChecked(true);
            textViewGray.setText(strGray + "2");
        } else if (flag == 1) {
            rb_high.setChecked(true);
            textViewGray.setText(strGray + "1");
        } else if (flag == 4) {
            radioButton_4.setChecked(true);
            textViewGray.setText(strGray + "4");
        } else if (flag == 5) {
            radioButton_5.setChecked(true);
            textViewGray.setText(strGray + "5");
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub

        disableFunctionLaunch(true);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        super.onResume();
        filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        receiver = new BatteryReceiver();
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        disableFunctionLaunch(false);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onPause();
        QuitHandler();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        stopService(mPrintServiceIntent);
//        unbindService(serviceConnection);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d("onKeyDown", "keyCode = " + keyCode);

//		if (keyCode == event.KEYCODE_BACK) {
//			if(m_bThreadFinished  == false)
//				return false;
//		}

        Log.d("onKeyDown", "keyCode = " + keyCode);
        Log.d("onKeyDown", "IsWorking== " + IsWorking);
        if (keyCode == event.KEYCODE_BACK) {
            if (IsWorking == 1)
                return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public void onClickConsume(View v) {
        if (printThread != null && !printThread.isThreadFinished()) {
            Log.e(tag, "Thread is still running...");
            return;
        }

        printThread = new Print_Thread(RPINT_CONSUME);
        printThread.start();
    }

    public void onClickUnicodeTest(View v) {
        if (printThread != null && !printThread.isThreadFinished()) {
            Log.e(tag, "Thread is still running...");
            return;
        }

        printThread = new Print_Thread(PRINT_UNICODE);
        printThread.start();

    }

    public void OnClickBarcode(View view) {
        if (printThread != null && !printThread.isThreadFinished()) {
            Log.e(tag, "Thread is still running...");
            return;
        }


        printThread = new Print_Thread(PRINT_BARCODE);
        printThread.start();
    }

    public void onClickBmp(View view) {
        if (printThread != null && !printThread.isThreadFinished()) {
            Log.e(tag, "Thread is still running...");
            return;
        }

        printThread = new Print_Thread(PRINT_BMP);
        printThread.start();

    }


    public void onClickCycle(View v) {
        if (printThread != null && !printThread.isThreadFinished()) {
            Log.e(tag, "Thread is still running...");
            return;
        }

        if (is_cycle == false) {
            is_cycle = true;
            preferences = getSharedPreferences("count", MODE_WORLD_READABLE);
            cycle_num = preferences.getInt("count", 0);
            SendMsg("total cycle num =" + cycle_num);

            handlers.postDelayed(runnable, 3000);

        }
    }


    public void onClickClean(View v) {
        textViewMsg.setText("");
        preferences = getSharedPreferences("count", MODE_WORLD_READABLE);
        cycle_num = preferences.getInt("count", 0);
        editor = preferences.edit();
        cycle_num = 0;
        editor.putInt("count", cycle_num);
        editor.commit();
        QuitHandler();
    }

    public void onClickPrnOpen(View v) {
        if (printThread != null && !printThread.isThreadFinished()) {
            Log.e(tag, "Thread is still running...");
            return;
        }

        printThread = new Print_Thread(PRINT_OPEN);
        printThread.start();

    }

    public void onClickLong(View v) {

        if (printThread != null && !printThread.isThreadFinished()) {
            Log.e(tag, "Thread is still running...");
            return;
        }
        printThread = new Print_Thread(PRINT_LONGER);
        printThread.start();

//        if (timer != null) {
//            timer.cancel();
//            // 一定设置为null，否则定时器不会被回收
//            timer = null;
//        }
//
//        if (timer2 != null) {
//            timer.cancel();
//            // 一定设置为null，否则定时器不会被回收
//            timer2 = null;
//        }
//        //  wakeLock.release();
//
//
//        //if(m_bThreadFinished)
//        QuitHandler();
//        finish();
    }


    public void QuitHandler() {
        is_cycle = false;
        gb_test.setEnabled(true);
        gb_barcode.setEnabled(true);
        btnBmp.setEnabled(true);
        gb_unicode.setEnabled(true);
        handlers.removeCallbacks(runnable);
    }


    Handler handlers = new Handler();
    Runnable runnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub

            Log.e(tag, "TIMER log...");
            printThread = new Print_Thread(PRINT_UNICODE);
            printThread.start();

            Log.e(tag, "TIMER log2...");
            if (RESULT_CODE == 0) {
                editor = preferences.edit();
                editor.putInt("count", ++cycle_num);
                editor.commit();
                Log.e(tag, "cycle num=" + cycle_num);
                SendMsg("cycle num =" + cycle_num);
            }
            handlers.postDelayed(this, 9000);

        }
    };

    Print_Thread printThread = null;

    public class Print_Thread extends Thread {

        String content = "1234567890";
        int type;

        public boolean isThreadFinished() {
            return m_bThreadFinished;
        }

        public Print_Thread(int type) {
            this.type = type;
        }

        public void run() {
            Log.d("Print_Thread[ run ]", "run() begin");
            Message msg = Message.obtain();
            Message msg1 = new Message();

            synchronized (this) {

                m_bThreadFinished = false;
                try {
                    ret = posApiHelper.PrintInit();
                } catch (PrintInitException e) {
                    e.printStackTrace();
                    int initRet = e.getExceptionCode();
                    Log.e(tag, "initRer : " + initRet);
                }

                Log.e(tag, "init code:" + ret);

                ret = getValue();
                Log.e(tag, "getValue():" + ret);

                posApiHelper.PrintSetGray(ret);

//                posApiHelper.PrintSetVoltage(BatteryV * 2 / 100);

                ret = posApiHelper.PrintCheckStatus();
                if (ret == -1) {
                    RESULT_CODE = -1;
                    Log.e(tag, "Lib_PrnCheckStatus fail, ret = " + ret);
                    SendMsg("Error, No Paper ");
                    m_bThreadFinished = true;
                    return;
                } else if (ret == -2) {
                    RESULT_CODE = -1;
                    Log.e(tag, "Lib_PrnCheckStatus fail, ret = " + ret);
                    SendMsg("Error, Printer Too Hot ");
                    m_bThreadFinished = true;
                    return;
                } else if (ret == -3) {
                    RESULT_CODE = -1;
                    Log.e(tag, "voltage = " + (BatteryV * 2));
                    SendMsg("Battery less :" + (BatteryV * 2));
                    m_bThreadFinished = true;
                    return;
                }
                /*
                else if (voltage_level < 5) {
                    RESULT_CODE = -1;
                    Log.e(tag, "voltage_level = " + voltage_level);
                    SendMsg("Battery capacity less : " + voltage_level);
                    m_bThreadFinished = true;
                    return;
                }*/
                else {
                    RESULT_CODE = 0;
                }

                switch (type) {

                    case PRINT_LONGER:
                        SendMsg("PRINT LONG");

                        msg.what = DISABLE_RG;
                        handler.sendMessage(msg);
                        Sys.Lib_LogSwitch(1);

//                        String stringg = " a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?";
//                        //	Print.Lib_PrnStr(string + "\n");
                        posApiHelper.PrintSetFont((byte) 24, (byte) 24, (byte) 0x00);
                        posApiHelper.PrintStr("a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >? a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >? a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?\n");

//                        posApiHelper.PrintStr("Немногие вернулись с поля!\n");
//                        posApiHelper.PrintStr("Не смеют, что ли, командиры!\n");
                        posApiHelper.PrintStr("The long string is finished !\n");
                        posApiHelper.PrintStr("- - - - - - - - - - - - - - - -\n");
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintStr("\n");

                        SendMsg("Printing... ");
                        ret = posApiHelper.PrintStart();

                        Log.e(tag, "PrintStart ret = " + ret);

                        msg1.what = ENABLE_RG;
                        handler.sendMessage(msg1);

                        if (ret != 0) {
                            RESULT_CODE = -1;
                            Log.e("liuhao", "Lib_PrnStart fail, ret = " + ret);
                            if (ret == -1) {
                                SendMsg("No Print Paper ");
                            } else if (ret == -2) {
                                SendMsg("too hot ");
                            } else if (ret == -3) {
                                SendMsg("low voltage ");
                            } else {
                                SendMsg("Print fail ");
                            }
                        } else {
                            RESULT_CODE = 0;
                            SendMsg("Print Finish ");
                        }

                        break;

                    case RPINT_CONSUME:
                        Sys.Lib_LogSwitch(1);
                        SendMsg("RPINT_CONSUME");
                        msg.what = DISABLE_RG;
                        handler.sendMessage(msg);
                        posApiHelper.PrintBmp(BitmapFactory.decodeResource(getResources(), R.drawable.cancel));
                        posApiHelper.PrintSetFont((byte) 16, (byte) 16, (byte) 0x00);
//                        posApiHelper.PrintStr("￡￡ABCDEFGHIJINKMNOPQRSTABCDEFGHIJINKMNOPQRSTABCDEFGHIJINKMNOPQRST1234667891234567890123\n");
//                        posApiHelper.PrintStr("Hello world\n");
//                        posApiHelper.PrintSetFont((byte)20, (byte)20, (byte)0x00);
//                        posApiHelper.PrintStr("￡￡ABCDEFGHIJINKMNOPQRSTABCDEFGHIJINKMNOPQRSTABCDEFGHIJINKMNOPQRST1234667891234567890123\n");
                        posApiHelper.PrintStr("Hasif good after noon \n nice to meet you world\n");
                        posApiHelper.PrintSetFont((byte) 24, (byte) 24, (byte) 0x00);
//                        posApiHelper.PrintStr("Pequeño Pequeño Pequeño Pequeño\n");
//                        posApiHelper.PrintStr("￡￡ABCDEFGHIJINKMNOPQRSTABCDEFGHIJINKMNOPQRSTABCDEFGHIJINKMNOPQRST1234667891234567890123\n");
//                        posApiHelper.PrintStr("Hello world\n");
//                        posApiHelper.PrintSetFont((byte)28, (byte)28, (byte)0x00);
//                        posApiHelper.PrintStr("￡￡ABCDEFGHIJINKMNOPQRSTABCDEFGHIJINKMNOPQRSTABCDEFGHIJINKMNOPQRST1234667891234567890123\n");
//                        posApiHelper.PrintStr("Hello world\n");  posApiHelper.PrintBmp(BitmapFactory.decodeResource(getResources(), R.drawable.cancel));
                        posApiHelper.PrintStr("Немногие вернулись с поля!\n");
                        posApiHelper.PrintStr("Не смеют, что ли, командиры!\n");
                        posApiHelper.PrintStr("Немногие вернулись с поля!\n");
                        posApiHelper.PrintStr("Не смеют, что ли, командиры!\n");
                        //					Print.Lib_PrnStr("ДОБРО ПОЖАЛОВАТЬ!");
                        posApiHelper.PrintStr("***************************");
                        posApiHelper.PrintStr("ООО \"БЛЕК МАШРУМ\"");
                        posApiHelper.PrintStr("Московская обл., г. Балашиха, ул");
                        posApiHelper.PrintStr("Заречная, д 26, кв 4.");
                        posApiHelper.PrintStr("room 115");
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintStr("ПАО \"БИНБАНК\" ");
                        posApiHelper.PrintStr("Терминал#          04206819");
                        posApiHelper.PrintStr("MasterCard");
                        posApiHelper.PrintStr("************2305");
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintStr("ОПЛАТА");
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintStr("Чек:     00034");
                        posApiHelper.PrintStr("Дата: 18.09.2018");
                        posApiHelper.PrintStr("Время: 19:41:42");
                        posApiHelper.PrintStr("RRN: 091811827290");
                        posApiHelper.PrintStr("Код авторизации: 315522113213");
                        posApiHelper.PrintStr("AID: A000000000041010");
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintStr("Сумма: 1.0");
                        posApiHelper.PrintStr("Итого: 1.0");
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintStr("≡201.00\n");
                        posApiHelper.PrintStr("Подпись клиента не требуется");
                        SendMsg("Printing... ");
                        ret = posApiHelper.PrintStart();

                        msg1.what = ENABLE_RG;
                        handler.sendMessage(msg1);

                        Log.d("", "Lib_PrnStart ret = " + ret);

                        if (ret != 0) {
                            RESULT_CODE = -1;
                            Log.e("liuhao", "Lib_PrnStart fail, ret = " + ret);
                            if (ret == -1) {
                                SendMsg("No Print Paper ");
                            } else if (ret == -2) {
                                SendMsg("too hot ");
                            } else if (ret == -3) {
                                SendMsg("low voltage ");
                            } else {
                                SendMsg("Print fail ");
                            }
                        } else {
                            RESULT_CODE = 0;
                            SendMsg("Print Finish ");
                        }

                        break;

                    case PRINT_CYCLE:
                        SendMsg("PRINT_CYCLE");
                        msg.what = DISABLE_RG;
                        handler.sendMessage(msg);
                        posApiHelper.PrintSetFont((byte) 24, (byte) 24, (byte) 0x00);
                        for (long dd = 0; dd < 100; dd++) {
                            posApiHelper.PrintStr("0 1 2 3 4 5 6 7 8 9 A B C D E\n");
                        }

                        SendMsg("Printing... ");
                        ret = posApiHelper.PrintStart();


                        msg1.what = ENABLE_RG;
                        handler.sendMessage(msg1);

                        Log.d("", "Lib_PrnStart ret = " + ret);
                        if (ret != 0) {
                            RESULT_CODE = -1;
                            Log.e("liuhao", "Lib_PrnStart fail, ret = " + ret);
                            if (ret == -1) {
                                SendMsg("No Print Paper ");
                            } else if (ret == -2) {
                                SendMsg("too hot ");
                            } else if (ret == -3) {
                                SendMsg("low voltage ");
                            } else {
                                SendMsg("Print fail ");
                            }
                        } else {
                            RESULT_CODE = 0;
                            SendMsg("Print Finish ");
                        }

                        break;

                    case PRINT_UNICODE:
                        SendMsg("PRINT_UNICODE");
                        msg.what = DISABLE_RG;
                        handler.sendMessage(msg);
                        posApiHelper.PrintSetFont((byte) 24, (byte) 24, (byte) 0x00);

                        posApiHelper.PrintStr("中文:你好，好久不见。\n");
                        posApiHelper.PrintStr("英语:Hello, Long time no see\n");
                        posApiHelper.PrintStr("Pequeño:España, ¡Hola! Cuánto tiempo sin verte!\n");
                        posApiHelper.PrintStr("阿拉伯语:مرحبا! وقت طويل لا رؤية\n");
                        posApiHelper.PrintStr("法语:Bonjour! Ça fait longtemps!\n");
                        posApiHelper.PrintStr("意大利 :Ciao, non CI vediamo da Molto Tempo.\n");

                        SendMsg("Printing... ");
//                        ret = posApiHelper.PrintStart();
                        ret = posApiHelper.PrintCtnStart();

                        for (int i = 1; i < 3; i++) {
                            posApiHelper.PrintSetFont((byte) 24, (byte) 24, (byte) 0x33);
                            posApiHelper.PrintStr("打印第：" + i + "次\n");
                            posApiHelper.PrintStr("Print times: " + i + "\n");
                            posApiHelper.PrintSetFont((byte) 16, (byte) 16, (byte) 0x33);
                            posApiHelper.PrintStr("- - - - - - - - - - - - - - - - - - - - - - - -\n");
                            posApiHelper.PrintSetFont((byte) 24, (byte) 24, (byte) 0x00);
                            posApiHelper.PrintStr("商户编号(MERCHANT NO):\n");
                            posApiHelper.PrintStr("    001420183990573\n");
                            posApiHelper.PrintStr("- - - - - - - - - - - - - - - -\n");
                            //	posApiHelper.PrintStr("\n");
                            posApiHelper.PrintStr("卡号(CARD NO):\n");
                            posApiHelper.PrintStr("    9558803602109503920\n");
                            posApiHelper.PrintStr("交易类型(TXN. TYPE):消费/SALE\n");
                            posApiHelper.PrintStr("卡有效期(EXP. DATE):2013/08\n");
                            posApiHelper.PrintStr("- - - - - - - - - - - - - - - -\n");
                            //	posApiHelper.PrintStr("\n");
                            posApiHelper.PrintStr("批次号(BATCH NO)  :000023\n");
                            posApiHelper.PrintStr("凭证号(VOUCHER NO):000018\n");
                            posApiHelper.PrintStr("授权号(AUTH NO)   :987654\n");
                            posApiHelper.PrintStr("日期/时间(DATE/TIME):\n");
                            posApiHelper.PrintStr("    2018/01/28 16:46:32\n");
                            posApiHelper.PrintStr("金额(AMOUNT):  RMB:2.55\n");
                            posApiHelper.PrintStr("- - - - - - - - - - - - - - - -\n");
                            //	posApiHelper.PrintStr("\n");
                            posApiHelper.PrintStr("备注/REFERENCE\n");
                            posApiHelper.PrintStr("- - - - - - - - - - - - - - - -\n");
                            posApiHelper.PrintSetFont((byte) 16, (byte) 16, (byte) 0x00);
                            posApiHelper.PrintStr("持卡人签名(CARDHOLDER SIGNATURE)\n");
                            posApiHelper.PrintStr("\n");
                            posApiHelper.PrintStr("- - - - - - - - - - - - - - - - - - - - - - - -\n");
                            //	posApiHelper.PrintStr("\n");
                            posApiHelper.PrintStr("  本人确认以上交易，同意将其计入本卡帐户\n");
                            posApiHelper.PrintStr("  I ACKNOWLEDGE SATISFACTORY RECEIPT\n");

                            ret = posApiHelper.PrintCtnStart();

                            if (ret != 0) break;
                        }

                        msg1.what = ENABLE_RG;
                        handler.sendMessage(msg1);
                        Log.d("", "Lib_PrnStart ret = " + ret);
                        if (ret != 0) {
                            RESULT_CODE = -1;
                            Log.e("liuhao", "Lib_PrnStart fail, ret = " + ret);
                            if (ret == -1) {
                                SendMsg("No Print Paper ");
                            } else if (ret == -2) {
                                SendMsg("too hot ");
                            } else if (ret == -3) {
                                SendMsg("low voltage ");
                            } else {
                                SendMsg("Print fail ");
                            }
                        } else {
                            RESULT_CODE = 0;
                            SendMsg("Print Finish ");
                        }

                        ret = posApiHelper.PrintCheckStatus();
                        Log.e("liuhao", "PrintCheckStatus = " + ret);

                        ret = posApiHelper.PrintClose();

                        break;

                    case PRINT_OPEN:
                        SendMsg("PRINT_OPEN");
                        msg.what = DISABLE_RG;
                        handler.sendMessage(msg);

                        SendMsg("Print Open... ");
                        try {
                            ret = posApiHelper.PrintOpen();
                        } catch (PrintInitException e) {
                            e.printStackTrace();
                        }

                        Print.Lib_PrnStr(">-?-?-ü-?-?-ü-?-€-@-$-<");
                        posApiHelper.PrintStr("ပြည်ထောင်စုသမ္မတမြန်မာနိုင်ငံတော်123!\\n");//缅甸文
                        posApiHelper.PrintStr("ကျွ,န်ုပ်တို့၏ယူနစ်\n");
                        posApiHelper.PrintStr("ကျွန်ုပ်တို့၏အချိန်,တရုတ်မင်္ဂလာပါပြည်သူ့သမ္မတနိုင်ငံ\n");
                        posApiHelper.PrintStr("င်္ဂလာ\n");//1004103a103a 还未解决
                        posApiHelper.PrintStr("မ္မ\n");//1039叠加测试
                        posApiHelper.PrintStr("ဒီတော့ဒီနှစ်ခုမြန်မာကိုဘာသာပြန်ချက်အပြောင်းအလဲနဲ့အင်ဂျင်ဇာတ်ကောင်ကွဲပြားခြားနားသည်။ကျနော်တို့ကဒီမှာသာတရားဝင်ဘာသာစကားဆွေးနွေးရန်\n");
                        posApiHelper.PrintStr("ငြ့်အရေးဆိုင်ရာယွမ်လုပ်ဖို့ဗဟိုဘဏ်ကယွမ်ထုတ်ဝေ၏ပဉ္စမကိုထုတ်ဖို့ဆုံးဖြတ်\n");
                        posApiHelper.PrintStart();


//                        posApiHelper.PrintSetFont((byte) 24, (byte) 24, (byte) 0x00);
//                        posApiHelper.PrintStr("      一品佳港大店       \n\n");
//                        posApiHelper.PrintStr("#" + getEditTextString(R.id.edNo) + "  收银1  " + getEditTextString(R.id.edMonth) + "月" + getEditTextString(R.id.edDay) + "日"+" "+getEditTextString(R.id.edTime)+"\n");
////                        posApiHelper.PrintStr("#35152  收银1  03月26日    18:28\n");
//                        posApiHelper.PrintStr("-------------------------------\n");
//                        posApiHelper.PrintStr("自选快餐       1     "+getEditTextString(R.id.edSum)+".00\n");
////                        posApiHelper.PrintStr("自选快餐       1     20.00\n");
//                        posApiHelper.PrintStr("-------------------------------\n");
//                        posApiHelper.PrintSetFont((byte) 24, (byte) 24, (byte) 0x33);
//                        if (getEditTextString(R.id.edType).equals("w")){
//                            posApiHelper.PrintStr("微信     "+getEditTextString(R.id.edSum)+".00\n");
//                        }else {
//                            posApiHelper.PrintStr("支付宝    "+getEditTextString(R.id.edSum)+".00\n");
//                        }
////                        posApiHelper.PrintStr("微信  20.00\n");
//                        posApiHelper.PrintSetFont((byte) 24, (byte) 24, (byte) 0x00);
//                        posApiHelper.PrintStr("                                \n");
//                        posApiHelper.PrintStr("           多谢惠顾        \n");
//                        posApiHelper.PrintStart();

                        msg1.what = ENABLE_RG;
                        handler.sendMessage(msg1);

                        Log.d("", "Lib_PrnStart ret = " + ret);
                        if (ret != 0) {
                            RESULT_CODE = -1;
                            Log.e("liuhao", "Lib_PrnStart fail, ret = " + ret);
                            if (ret == -1) {
                                SendMsg("No Print Paper ");
                            } else if (ret == -2) {
                                SendMsg("too hot ");
                            } else if (ret == -3) {
                                SendMsg("low voltage ");
                            } else {
                                SendMsg("Print fail ");
                            }
                        } else {
                            RESULT_CODE = 0;
                            SendMsg("Print Open Finish ");
                        }

                        break;

                    case PRINT_BMP:
                        SendMsg("PRINT_BMP");
                        msg.what = DISABLE_RG;
                        handler.sendMessage(msg);

                        //0 left，1 middle ，2 right
//                        Print.Lib_PrnSetAlign(0);
                        Bitmap bmp = BitmapFactory.decodeResource(PrintActivity.this.getResources(), R.mipmap.huac);
                        ret = posApiHelper.PrintBmp(bmp);
                        posApiHelper.PrintStr("中   文 ：Bitmap 打印完毕!\n");
                        posApiHelper.PrintStr("English : Bitmap Print Finished！\n");
                        if (ret == 0) {
                            posApiHelper.PrintStr("\n\n\n");
                            posApiHelper.PrintStr("                                         \n");
                            posApiHelper.PrintStr("                                         \n");

                            SendMsg("Printing... ");
                            ret = posApiHelper.PrintStart();

                            msg1.what = ENABLE_RG;
                            handler.sendMessage(msg1);

                            Log.d("", "Lib_PrnStart ret = " + ret);
                            if (ret != 0) {
                                RESULT_CODE = -1;
                                Log.e("liuhao", "Lib_PrnStart fail, ret = " + ret);
                                if (ret == -1) {
                                    SendMsg("No Print Paper ");
                                } else if (ret == -2) {
                                    SendMsg("too hot ");
                                } else if (ret == -3) {
                                    SendMsg("low voltage ");
                                } else {
                                    SendMsg("Print fail ");
                                }
                            } else {
                                RESULT_CODE = 0;
                                SendMsg("Print Finish ");
                            }
                        } else {
                            RESULT_CODE = -1;
                            SendMsg("Lib_PrnBmp Failed");
                        }
                        break;

                    case PRINT_BARCODE:
                        SendMsg("PRINT_BARCODE");
                        msg.what = DISABLE_RG;
                        handler.sendMessage(msg);
                        posApiHelper.PrintBarcode(content, 360, 120, BarcodeFormat.CODE_128);
                        posApiHelper.PrintStr("CODE_128 : " + content + "\n");
                        //0 Left ,1 Middle ,2 Right
                        Print.Lib_PrnSetAlign(1);
                        posApiHelper.PrintBarcode(content, 240, 240, BarcodeFormat.QR_CODE);

                        posApiHelper.PrintBmp(BitmapFactory.decodeResource(getResources(), R.drawable.cancel));
                        Print.Lib_PrnSetAlign(0);

                        posApiHelper.PrintStr("                                        \n");
                        posApiHelper.PrintStr("\n");

                        SendMsg("Printing... ");
                        ret = posApiHelper.PrintStart();

                        msg1.what = ENABLE_RG;
                        handler.sendMessage(msg1);

                        Log.d("", "Lib_PrnStart ret = " + ret);
                        if (ret != 0) {
                            RESULT_CODE = -1;
                            Log.e("liuhao", "Lib_PrnStart fail, ret = " + ret);
                            if (ret == -1) {
                                SendMsg("No Print Paper ");
                            } else if (ret == -2) {
                                SendMsg("too hot ");
                            } else if (ret == -3) {
                                SendMsg("low voltage ");
                            } else {
                                SendMsg("Print fail ");
                            }
                        } else {
                            RESULT_CODE = 0;
                            SendMsg("Print Finish ");
                        }

                        break;

                    default:
                        break;
                }
                m_bThreadFinished = true;

                Log.e(tag, "goToSleep2...");
            }
        }
    }

    public String getEditTextString(int id) {
        EditText txt = (EditText) findViewById(id);
        return txt.getText().toString().trim();
    }


    public void SendMsg(String strInfo) {
        Message msg = new Message();
        Bundle b = new Bundle();
        b.putString("MSG", strInfo);
        msg.setData(b);
        handler.sendMessage(msg);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case DISABLE_RG:
                    IsWorking = 1;
                    rb_high.setEnabled(false);
                    rb_middle.setEnabled(false);
                    rb_low.setEnabled(false);
                    radioButton_4.setEnabled(false);
                    radioButton_5.setEnabled(false);
                    break;

                case ENABLE_RG:
                    IsWorking = 0;
                    rb_high.setEnabled(true);
                    rb_middle.setEnabled(true);
                    rb_low.setEnabled(true);
                    radioButton_4.setEnabled(true);
                    radioButton_5.setEnabled(true);

                    break;
                default:
                    Bundle b = msg.getData();
                    String strInfo = b.getString("MSG");
                    textViewMsg.setText(strInfo);

                    break;
            }
        }
    };

    public class BatteryReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            voltage_level = intent.getExtras().getInt("level");// ��õ�ǰ����
            Log.e("wbw", "current  = " + voltage_level);
            BatteryV = intent.getIntExtra("voltage", 0);  //电池电压
            Log.e("wbw", "BatteryV  = " + BatteryV);
            Log.e("wbw", "V  = " + BatteryV * 2 / 100);
            //	m_voltage = (int) (65+19*voltage_level/100); //放大十倍
            //   Log.e("wbw","m_voltage  = " + m_voltage );
        }
    }

    // disable the power key when the device is boot from alarm but not ipo boot
    private static final String DISABLE_FUNCTION_LAUNCH_ACTION = "android.intent.action.DISABLE_FUNCTION_LAUNCH";

    private void disableFunctionLaunch(boolean state) {
        Intent disablePowerKeyIntent = new Intent(DISABLE_FUNCTION_LAUNCH_ACTION);
        if (state) {
            disablePowerKeyIntent.putExtra("state", true);
        } else {
            disablePowerKeyIntent.putExtra("state", false);
        }
        sendBroadcast(disablePowerKeyIntent);
    }

    // 在Activity中，我们通过ServiceConnection接口来取得建立连接与连接意外丢失的回调

    //    ServiceConnection serviceConnection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
////            MyService.MyBinder binder = (MyService.MyBinder)service;
////            binder.getService();// 获取到的Service即MyService
//            MyService.Binder binder = (MyService.Binder) service;
//            MyService myService = binder.getService();
//
//            myService.setCallback(new MyService.CallBackPrintStatus() {
//                @Override
//                public void printStatusChange(String strStatus) {
//                    SendMsg(strStatus);
//                }
//            });
//
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//
//        }
//    };
//
    public void OnClickPrintSimpleApiTest(View view) {

//        mPrintServiceIntent=new Intent(PrintActivity.this, MyService.class);
//        startService(mPrintServiceIntent);

        //绑定目标Service
//        bindService(mPrintServiceIntent,serviceConnection,Context.BIND_AUTO_CREATE);

        if (printThread != null && !printThread.isThreadFinished()) {
            Log.e(tag, "Thread is still running...");
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {

                Message msg = Message.obtain();
                Message msg1 = Message.obtain();

                msg.what = DISABLE_RG;
                handler.sendMessage(msg);

                ret = posApiHelper.PrintInit(2, 24, 24, 0);
                /*
                 *  or
                 *  No parameter defaults Api
                 */

                /*
                try {
                    ret= posApiHelper.PrintInit();
                } catch (PrintInitException e) {
                    e.printStackTrace();
                    int initRet = e.getExceptionCode();
                    Log.e(TAG,"initRer : "+initRet);
                }
                */

                if (ret != 0) {
                    return;
                }

                ret = getValue();
                Log.e(tag, "getValue():" + ret);

                posApiHelper.PrintSetGray(ret);

                posApiHelper.PrintStr("Print Tile\n");
                posApiHelper.PrintStr("\n");
//                ret = posApiHelper.PrintSetFont((byte)24, (byte)24, (byte)0);
                ret = posApiHelper.PrintSetFont((byte) 16, (byte) 16, (byte) 0x33);

                Log.e(tag, "initRer PrintSetFont: " + ret);

                if (ret != 0) {
                    return;
                }
                posApiHelper.PrintStr("- - - - - - - - - - - - - - - - - - - - - - - -\n");
                //	posApiHelper.PrintStr("\n");
                posApiHelper.PrintStr("  Print Str1 \n");
                posApiHelper.PrintStr("  Print Str2 \n");
                posApiHelper.PrintBarcode("123456789", 360, 120, BarcodeFormat.CODE_128);
                posApiHelper.PrintBarcode("123456789", 240, 240, BarcodeFormat.QR_CODE);
                posApiHelper.PrintStr("CODE_128 : " + "123456789" + "\n\n");
                posApiHelper.PrintStr("QR_CODE : " + "123456789" + "\n\n");
                posApiHelper.PrintStr("                                        \n");
                posApiHelper.PrintStr("\n");
                posApiHelper.PrintStr("\n");

                SendMsg("Printing... ");
                ret = posApiHelper.PrintStart();

                Log.e(tag, "Lib_PrnStart ret = " + ret);

                msg1.what = ENABLE_RG;
                handler.sendMessage(msg1);

                if (ret != 0) {
                    RESULT_CODE = -1;
                    Log.e("liuhao", "Lib_PrnStart fail, ret = " + ret);
                    if (ret == -1) {
                        SendMsg("No Print Paper ");
                    } else if (ret == -2) {
                        SendMsg("too hot ");
                    } else if (ret == -3) {
                        SendMsg("low voltage ");
                    } else {
                        SendMsg("Print fail ");
                    }
                } else {
                    RESULT_CODE = 0;
                    SendMsg("Print Finish ");
                }
            }
        }).start();

    }

}
