package com.example.bdmap.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNaviCommonParams;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNRoutePlanManager;
import com.baidu.navisdk.adapter.IBaiduNaviManager;
import com.example.bdmap.R;
import com.example.bdmap.baidudemo.NormalUtils;
import com.example.bdmap.sqlite.ImportDB;
import com.example.bdmap.sqlite.Plcae;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MyplaceActivity extends AppCompatActivity {
    private List<Plcae>place_items;
    private ListView place_list;
    private ImportDB importDB;
    private double mylatitude=0.0;
    private double mylongitude=0.0;
    private String mSDCardPath = null;
    private static final String APP_FOLDER_NAME = "BNSDKSimpleDemo";
    private static final int authBaseRequestCode = 1;
    private static final String[] authBaseArr = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myplace);
        place_list=findViewById(R.id.place_list);
        Intent it=getIntent();
        mylatitude=it.getDoubleExtra("mylatitude",0.0);
        mylongitude=it.getDoubleExtra("mylongitude",0.0);
        init_data();
        init_view();
        initNavi();
        place_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final String[] items = { "删除","开始导航"};
                AlertDialog.Builder listDialog = new AlertDialog.Builder(MyplaceActivity.this);
                listDialog.setTitle("请选择");
                listDialog.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(items[which].equals("删除")){
                            String path=importDB.initDB();
                            SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);//读写方式打开
                            String sql="delete from myplaces where id="+place_items.get(position).getId();
                            db.execSQL(sql);
                            db.close();
                            init_data();
                            init_view();
                            Toast.makeText(MyplaceActivity.this,"删除成功",Toast.LENGTH_LONG).show();
                        }
                        else if(items[which].equals("开始导航")){
                        String la=place_items.get(position).getLatitude();
                        String lo=place_items.get(position).getLongitude();
                        double latitude=Double.parseDouble(la);
                        double longitude=Double.parseDouble(lo);
                        BNRoutePlanNode   sNode = new BNRoutePlanNode.Builder()
                                    .latitude(mylatitude)
                                    .longitude(mylongitude)
                                    .coordinateType(BNRoutePlanNode.CoordinateType.BD09LL)
                                    .build();
                        BNRoutePlanNode   eNode = new BNRoutePlanNode.Builder()
                                    .latitude(latitude)
                                    .longitude(longitude)
                                    .coordinateType(BNRoutePlanNode.CoordinateType.BD09LL)
                                    .build();
                        routePlanToNavi(sNode,eNode);
                        }
                    }
                });listDialog.show();
            }
        });
    }
    private void init_data()
    {
        place_items=new ArrayList<>();
        importDB=new ImportDB(this);
        String path=importDB.initDB();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);//只读方式打开
        Cursor cursor = db.rawQuery("select * from myplaces", null);
        if(cursor!=null&&cursor.getCount()>=1) {
            cursor.moveToFirst();
            do {
                Plcae plcae = new Plcae();
                plcae.setId(cursor.getInt(0));
                plcae.setPname(cursor.getString(1));
                plcae.setLatitude(cursor.getString(2));
                plcae.setLongitude(cursor.getString(3));
                place_items.add(plcae);

            } while (cursor.moveToNext());
            db.close();
        }
        else Toast.makeText(MyplaceActivity.this,"您还没有添加过地点",Toast.LENGTH_LONG).show();
    }
    private void init_view()
    {

        List<HashMap<String, String>> list = new ArrayList<>();
        for (int i=0; i< place_items.size(); i++) {
            HashMap<String, String> map = new HashMap<>();
            map.put("pname", place_items.get(i).getPname());
            list.add(map);
        }
        SimpleAdapter simplead = new SimpleAdapter(this, list,
                R.layout.item_place, new String[] { "pname" },
                new int[] {R.id.place_name});
        place_list.setAdapter(simplead);

    }
    private void initNavi() {
        // 申请权限
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (!hasBasePhoneAuth()) {
                this.requestPermissions(authBaseArr, authBaseRequestCode);
                return;
            }
        }

        if (BaiduNaviManagerFactory.getBaiduNaviManager().isInited()) {
            return;
        }

        BaiduNaviManagerFactory.getBaiduNaviManager().init(this,
                mSDCardPath, APP_FOLDER_NAME, new IBaiduNaviManager.INaviInitListener() {

                    @Override
                    public void onAuthResult(int status, String msg) {
                        String result;
                        if (0 == status) {
                            result = "key校验成功!";
                        } else {
                            result = "key校验失败, " + msg;
                        }
                    }

                    @Override
                    public void initStart() {
                        Toast.makeText(MyplaceActivity.this.getApplicationContext(),
                                "百度导航引擎初始化开始", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void initSuccess() {
                        Toast.makeText(MyplaceActivity.this.getApplicationContext(),
                                "百度导航引擎初始化成功", Toast.LENGTH_SHORT).show();
                        // 初始化tts
                        initTTS();
                    }

                    @Override
                    public void initFailed(int errCode) {
                        Toast.makeText(MyplaceActivity.this.getApplicationContext(),
                                "百度导航引擎初始化失败 " + errCode, Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private boolean hasBasePhoneAuth() {
        PackageManager pm = this.getPackageManager();
        for (String auth : authBaseArr) {
            if (pm.checkPermission(auth, this.getPackageName()) != PackageManager
                    .PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    private void initTTS() {
        // 使用内置TTS
        BaiduNaviManagerFactory.getTTSManager().initTTS(getApplicationContext(),
                getSdcardDir(), APP_FOLDER_NAME, NormalUtils.getTTSAppID());
    }
    private String getSdcardDir() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }
    private boolean initDirs() {
        mSDCardPath = getSdcardDir();
        if (mSDCardPath == null) {
            return false;
        }
        File f = new File(mSDCardPath, APP_FOLDER_NAME);
        if (!f.exists()) {
            try {
                f.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
    private void routePlanToNavi(BNRoutePlanNode sNode, BNRoutePlanNode eNode) {
        List<BNRoutePlanNode> list = new ArrayList<>();
        list.add(sNode);
        list.add(eNode);

        BaiduNaviManagerFactory.getRoutePlanManager().routeplanToNavi(
                list,
                IBNRoutePlanManager.RoutePlanPreference.ROUTE_PLAN_PREFERENCE_DEFAULT,
                null,
                new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        switch (msg.what) {
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_START:
                                Toast.makeText(MyplaceActivity.this.getApplicationContext(),
                                        "算路开始", Toast.LENGTH_SHORT).show();
                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_SUCCESS:
                                Toast.makeText(MyplaceActivity.this.getApplicationContext(),
                                        "算路成功", Toast.LENGTH_SHORT).show();
                                // 躲避限行消息
                                Bundle infoBundle = (Bundle) msg.obj;
                                if (infoBundle != null) {
                                    String info = infoBundle.getString(
                                            BNaviCommonParams.BNRouteInfoKey.TRAFFIC_LIMIT_INFO
                                    );
                                    Log.d("OnSdkDemo", "info = " + info);
                                }
                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_FAILED:
                                Toast.makeText(MyplaceActivity.this.getApplicationContext(),
                                        "算路失败", Toast.LENGTH_SHORT).show();
                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_TO_NAVI:
                                Toast.makeText(MyplaceActivity.this.getApplicationContext(),
                                        "算路成功准备进入导航", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(MyplaceActivity.this,
                                        DemoGuideActivity.class);

                                startActivity(intent);
                                break;
                            default:
                                // nothing
                                break;
                        }
                    }
                });
    }
}
