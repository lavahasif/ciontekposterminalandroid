//package com.example.myapplication;
//
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.content.res.ColorStateList;
//import android.graphics.drawable.Drawable;
//import android.os.Build;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.view.Window;
//import android.view.WindowManager;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//import androidx.core.graphics.drawable.DrawableCompat;
//
//import vpos.apipackage.PosApiHelper;
//
//
//public class MainActivity extends AppCompatActivity {
//
//    Context mContext;
//
//    //ITEM icc
//    private static final int ITEM_CODE_ICC = 0;
//    //ITEM mcr
//    private static final int ITEM_CODE_MCR = 1;
//    //ITEM print
//    private static final int ITEM_CODE_PRINT = 2;
//    //ITEM sys
//    private static final int ITEM_CODE_SYS = 3;
//    //ITEM LogIC
//    private static final int ITEM_CODE_LOGIC = 4;
//    //ITEM NFC
//    private static final int ITEM_CODE_NFC = 5;
//
//    // Used to load the 'native-lib' library on application startup.
//
//    private GridMenuLayout mGridMenuLayout;
//
//    public static String[] MY_PERMISSIONS_STORAGE = {
//            //"ciontek.permission.sdcard"
//            "android.permission.READ_EXTERNAL_STORAGE",
//            "android.permission.WRITE_EXTERNAL_STORAGE",
//            "android.permission.MOUNT_UNMOUNT_FILESYSTEMS"
//
//    };
//
//    public static final int REQUEST_EXTERNAL_STORAGE = 1;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        //无title
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        //全屏
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
//
//        //Determine if the current Android version is >=23
//        // 判断Android版本是否大于23
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            requestPermission();
//        } else {
//            initViews();
//        }
//
//    }
//
//
//    /**
//     * @Description: Request permission
//     * 申请权限
//     */
//    private void requestPermission() {
//        //检测是否有写的权限
//        //Check if there is write permission
//        int checkCallPhonePermission = ContextCompat.checkSelfPermission(MainActivity.this, "ciontek.permission.sdcard"/*Manifest.permission.WRITE_EXTERNAL_STORAGE*/);
//
//        if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
//            // 没有写文件的权限，去申请读写文件的权限，系统会弹出权限许可对话框
//            //Without the permission to Write, to apply for the permission to Read and Write, the system will pop up the permission dialog
//            ActivityCompat.requestPermissions(MainActivity.this, MY_PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
//        } else {
//            initViews();
//        }
//    }
//
//    /**
//     * a callback for request permission
//     * 注册权限申请回调
//     *
//     * @param requestCode  申请码
//     * @param permissions  申请的权限
//     * @param grantResults 结果
//     */
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                initViews();
//            } else {
////                Toast.makeText(MainActivity.this,R.string.title_permission,Toast.LENGTH_SHORT).show();
//                requestPermission();
//            }
//        }
//
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        //set Power ON
//        PosApiHelper.getInstance().SysSetPower(1);
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
////        PosApiHelper.getInstance().SysSetPower(0);
//    }
//
//    public static Drawable tintDrawable(Drawable drawable, ColorStateList colors) {
//        final Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
//        DrawableCompat.setTintList(wrappedDrawable, colors);
//        return wrappedDrawable;
//    }
//
//    private void initViews() {
//
//        setContentView(R.layout.main);
//
//        mContext = MainActivity.this;
//
//        final Drawable[] itemImgs = {
//                getResources().getDrawable(R.drawable.icc),
//                getResources().getDrawable(R.drawable.msr),
//                getResources().getDrawable(R.drawable.print),
//                getResources().getDrawable(R.drawable.sys),
//                getResources().getDrawable(R.drawable.logic),
//                getResources().getDrawable(R.drawable.more)
//        };
//
//        final String[] itemTitles = {
//                getString(R.string.icc)
//                , getString(R.string.mcr)
//                , getString(R.string.print)
//                , getString(R.string.sys)
//                , getString(R.string.logic)
//                , getString(R.string.more)
//        };
//
//        final int sizeWidth = getResources().getDisplayMetrics().widthPixels / 25;
//
//        mGridMenuLayout = (GridMenuLayout) findViewById(R.id.myGrid);
//        mGridMenuLayout.setGridAdapter(new GridMenuLayout.GridAdapter() {
//
//            @Override
//            public View getView(int index) {
//                View view = getLayoutInflater().inflate(R.layout.gridmenu_item, null);
//                ImageView gridItemImg = (ImageView) view.findViewById(R.id.gridItemImg);
//                TextView gridItemTxt = (TextView) view.findViewById(R.id.gridItemTxt);
//
//                gridItemImg.setImageDrawable(tintDrawable(itemImgs[index], mContext.getResources().getColorStateList(R.color.item_image_select)));
//
//                gridItemTxt.setText(itemTitles[index]);
//                gridItemTxt.setTextSize(sizeWidth);
//
//                return view;
//            }
//
//            @Override
//            public int getCount() {
//                return itemTitles.length;
//            }
//        });
//
//        mGridMenuLayout.setOnItemClickListener(new GridMenuLayout.OnItemClickListener() {
//
//            public void onItemClick(View v, int index) {
//                switch (index) {
//                    case ITEM_CODE_ICC:
//                        Intent iccIntent = new Intent(MainActivity.this, IccActivity.class);
//                        startActivity(iccIntent);
//                        break;
//                    case ITEM_CODE_MCR:
//                        Intent mcrIntent = new Intent(MainActivity.this, McrActivity.class);
//                        startActivity(mcrIntent);
//                        break;
//                    case ITEM_CODE_PRINT:
//                        Intent printIntent = new Intent(MainActivity.this, PrintActivity.class);
//                        startActivity(printIntent);
//                        break;
//                    case ITEM_CODE_SYS:
//                        Intent sysIntent = new Intent(MainActivity.this, SysActivity.class);
//                        startActivity(sysIntent);
//                        break;
//                    case ITEM_CODE_LOGIC:
//                        Intent emvIntent = new Intent(MainActivity.this, LogICActivity.class);
//                        startActivity(emvIntent);
//                        break;
//                    case ITEM_CODE_NFC:
//
//                        FileTools.write("liuhao.txt", 0, "123456789000001121212");
//
//                        try {
//                            Thread.sleep(2000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//
//                        Log.e("liuhao", FileTools.read("liuhao.txt", 0));
//
//                        break;
//                    default:
//                        break;
//                }
//            }
//        });
//    }
//
//}
