package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.example.myapplication.GridMenuLayout.GridAdapter
import vpos.apipackage.PosApiHelper

class MainActivity : AppCompatActivity() {

    var mContext: Context? = null

    //ITEM icc
    private val ITEM_CODE_ICC = 0

    //ITEM mcr
    private val ITEM_CODE_MCR = 1

    //ITEM print
    private val ITEM_CODE_PRINT = 2

    //ITEM sys
    private val ITEM_CODE_SYS = 3

    //ITEM LogIC
    private val ITEM_CODE_LOGIC = 4

    //ITEM NFC
    private val ITEM_CODE_NFC = 5

    // Used to load the 'native-lib' library on application startup.

    // Used to load the 'native-lib' library on application startup.
    private var mGridMenuLayout: GridMenuLayout? = null

    var MY_PERMISSIONS_STORAGE = arrayOf( //"ciontek.permission.sdcard"
        "android.permission.READ_EXTERNAL_STORAGE",
        "android.permission.WRITE_EXTERNAL_STORAGE",
        "android.permission.MOUNT_UNMOUNT_FILESYSTEMS"
    )

    val REQUEST_EXTERNAL_STORAGE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //无title
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        //全屏
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        //Determine if the current Android version is >=23
        // 判断Android版本是否大于23
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermission()
        } else {
            initViews()
        }
    }


    /**
     * @Description: Request permission
     * 申请权限
     */
    private fun requestPermission() {
        //检测是否有写的权限
        //Check if there is write permission
        val checkCallPhonePermission = ContextCompat.checkSelfPermission(
            this@MainActivity,
            "ciontek.permission.sdcard" /*Manifest.permission.WRITE_EXTERNAL_STORAGE*/
        )
        if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
            // 没有写文件的权限，去申请读写文件的权限，系统会弹出权限许可对话框
            //Without the permission to Write, to apply for the permission to Read and Write, the system will pop up the permission dialog
            ActivityCompat.requestPermissions(
                this@MainActivity,
                MY_PERMISSIONS_STORAGE,
                REQUEST_EXTERNAL_STORAGE
            )
        } else {
            initViews()
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
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initViews()
            } else {
//                Toast.makeText(MainActivity.this,R.string.title_permission,Toast.LENGTH_SHORT).show();
                requestPermission()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        //set Power ON
        PosApiHelper.getInstance().SysSetPower(1)
    }

    override fun onDestroy() {
        super.onDestroy()
//        PosApiHelper.getInstance().SysSetPower(0);
    }

    fun tintDrawable(drawable: Drawable?, colors: ColorStateList?): Drawable? {
        val wrappedDrawable = DrawableCompat.wrap(drawable!!)
        DrawableCompat.setTintList(wrappedDrawable, colors)
        return wrappedDrawable
    }

    private fun initViews() {
        setContentView(R.layout.main)
        mContext = this@MainActivity
        val itemImgs = arrayOf(
            resources.getDrawable(R.drawable.icc),
            resources.getDrawable(R.drawable.msr),
            resources.getDrawable(R.drawable.print),
            resources.getDrawable(R.drawable.sys),
            resources.getDrawable(R.drawable.logic),
            resources.getDrawable(R.drawable.more)
        )
        val itemTitles = arrayOf(
            getString(R.string.icc),
            getString(R.string.mcr),
            getString(R.string.print),
            getString(R.string.sys),
            getString(R.string.logic),
            getString(R.string.more)
        )
        val sizeWidth = resources.displayMetrics.widthPixels / 25
        mGridMenuLayout = findViewById<View>(R.id.myGrid) as GridMenuLayout
        mGridMenuLayout!!.setGridAdapter(object : GridAdapter {
            override fun getView(index: Int): View {
                val view = layoutInflater.inflate(R.layout.gridmenu_item, null)
                val gridItemImg = view.findViewById<View>(R.id.gridItemImg) as ImageView
                val gridItemTxt = view.findViewById<View>(R.id.gridItemTxt) as TextView
                gridItemImg.setImageDrawable(
                    tintDrawable(
                        itemImgs[index],
                        (mContext as MainActivity).getResources().getColorStateList(R.color.item_image_select)
                    )
                )
                gridItemTxt.text = itemTitles[index]
                gridItemTxt.textSize = sizeWidth.toFloat()
                return view
            }

            override fun getCount(): Int {
                return itemTitles.size
            }
        })
        mGridMenuLayout!!.setOnItemClickListener { v, index ->
            when (index) {
                ITEM_CODE_ICC -> {
                    val iccIntent = Intent(this@MainActivity, IccActivity::class.java)
                    startActivity(iccIntent)
                }
                ITEM_CODE_MCR -> {
                    val mcrIntent = Intent(this@MainActivity, McrActivity::class.java)
                    startActivity(mcrIntent)
                }
                ITEM_CODE_PRINT -> {
                    val printIntent = Intent(this@MainActivity, PrintActivity::class.java)
                    startActivity(printIntent)
                }
                ITEM_CODE_SYS -> {
                    val sysIntent = Intent(this@MainActivity, SysActivity::class.java)
                    startActivity(sysIntent)
                }
                ITEM_CODE_LOGIC -> {
                    val emvIntent = Intent(this@MainActivity, LogICActivity::class.java)
                    startActivity(emvIntent)
                }
                ITEM_CODE_NFC -> {
                    FileTools.write("liuhao.txt", 0, "123456789000001121212")
                    try {
                        Thread.sleep(2000)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                    Log.e("liuhao", FileTools.read("liuhao.txt", 0))
                }
                else -> {
                }
            }
        }
    }

}