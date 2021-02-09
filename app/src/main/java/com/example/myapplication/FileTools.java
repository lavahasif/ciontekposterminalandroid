package com.example.myapplication;

import android.os.Environment;

import java.io.File;
import java.io.RandomAccessFile;

public class FileTools {

    public static String DIR = Environment.getExternalStorageDirectory().getPath()+"/";

    public static void write(String fileName ,long seekto ,String content) {
     //   Log.e("liuhao dir", "11111111111");
        try {
            //判断实际是否有SD卡，且应用程序是否有读写SD卡的能力，有则返回true
            // 获取SD卡的目录
//                File sdCardDir = Environment.getExternalStorageDirectory();
            //String path = "/storage/emulated/0/Paypassconfig";

//            File file= new File(DIR +"PayPassConfig");//demo在这里表示一个相对于项目的路径
//            //判断文件是否存在，不存在就创建出来
//            if (!file.exists()) {
//                file.mkdirs();//mkdirs用于创建文件夹
//            }
         //   Log.e("heyp write","heyp write----0");
            File dir = new File(DIR + fileName);
          //  Log.e("liuhao dir =", dir.getAbsolutePath());
            if (!dir.exists()) {
                dir.createNewFile();
                //使用RandomAccessFile是在原有的文件基础之上追加内容，
                //而使用outputstream则是要先清空内容再写入
                RandomAccessFile raf = new RandomAccessFile(dir, "rw");
                //光标移到原始文件最后，再执行写入
                raf.seek(seekto);
                raf.write(content.getBytes());
                raf.close();
            }
        } catch (Exception e) {
           // Log.e("heyp write","heyp write----1");
            e.printStackTrace();
        }
    }

    public static String read(String fileName ,long seekto) {
        try {
            File dir = new File(DIR+fileName);
            if (!dir.exists()) {
                return "  ";
            }
            //使用RandomAccessFile是在原有的文件基础之上追加内容，
            //而使用outputstream则是要先清空内容再写入
            RandomAccessFile raf = new RandomAccessFile(dir, "rw");
            //光标移到原始文件最后，再执行写入
            byte[] bytes = new byte[(int)dir.length()];
            raf.seek(seekto);
            raf.read(bytes);
            raf.close();
            String str = new String(bytes);
          //  Log.e("heyp read","heyp read----1");
            return  str;
        } catch (Exception e) {
         //  Log.e("heyp read","heyp read----2");
            e.printStackTrace();
            return "";
        }
    }
}
