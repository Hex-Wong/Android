package com.example.bdmap.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.baidu.navisdk.util.common.LogUtil;
import com.example.bdmap.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ImportDB {
    public final int BUFFER_SIZE = 10000;
    public static final String DB_NAME = "test"; //保存的数据库文件名
    public static final String PACKAGE_NAME = "com.example.bdmap";//工程包名
    public static final String DB_PATH = "/data"
            + Environment.getDataDirectory().getAbsolutePath() + "/"
            + PACKAGE_NAME+"/databases";  //在手机里存放数据库的位置
    public Context context;
    public ImportDB(Context context) {
        this.context = context;
    }
    public String initDB() {
        String DB_DIR_PATH = "/data/data/" + PACKAGE_NAME + "/databases";
        // 初始化数据库
        if (!new File(DB_DIR_PATH + "/test.db").exists()) {
            try {
                new File(DB_DIR_PATH).mkdir();
                FileOutputStream fos = new FileOutputStream(DB_DIR_PATH + "/test.db");
                InputStream is =context.getAssets().open("test.db");
                byte[] buf = new byte[1024];
                int len;
                while ((len = is.read(buf)) != -1) {
                    fos.write(buf, 0, len);
                }
                is.close();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return DB_DIR_PATH+"/test.db";
    }
}




