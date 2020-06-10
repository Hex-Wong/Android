package com.example.bdmap.activity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.bdmap.R;


import com.example.bdmap.sqlite.ImportDB;
import com.example.bdmap.sqlite.Message;
import com.viewpagerindicator.TabPageIndicator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class DriverActivity extends Activity {
    private static final String[] CONTENT = new String[] { "当前城市", "其他城市" };
    private Button start_driving;
    private ImportDB importDB;
    public Context context;
    private ListView ls;
    private ListView ls_other;
    private List<Message>message_list;
    private boolean is_init=false;
    private String city;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉顶部应用名
        setContentView(R.layout.user_driver);
        Intent intent=getIntent();
        city=intent.getStringExtra("mycity");
        //获取实例
        context=getApplicationContext();
        ViewPager pager = (ViewPager)findViewById(R.id.pager);
        start_driving=findViewById(R.id.start_driving);
        start_driving.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DriverActivity.this,
                        MapActivity.class);
                startActivity(intent);
            }
        });
        init_Data();
        //设置viewpage的适配器
        MyAdapter adapter = new MyAdapter();
        pager.setAdapter(adapter);

        //把viewpage和TabPageIndicator关联
        TabPageIndicator indicator = findViewById(R.id.indicator);
        indicator.setViewPager(pager);
    }
    private void init_Data()
    {
        if(is_init==false) {
            importDB = new ImportDB(context);
            String path = null;
            path = importDB.initDB();
            System.out.println(path);
            SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);//只读方式打开
            Cursor cursor = db.rawQuery("select * from message", null);
            message_list=new ArrayList<>();
            while ((cursor.moveToNext())) {
                Message message = new Message();
                message.setMessage(cursor.getString(cursor.getColumnIndex("message")));
                message.setCity(cursor.getString(cursor.getColumnIndex("city")));
                message.setTime(cursor.getString(cursor.getColumnIndex("time")));
                message_list.add(message);
            }
            is_init=true;
        }

    }
    private class MyAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return 2;//告诉viewpage，我有多少条数据，要加载
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            // TODO Auto-generated method stub
            return arg0 == arg1;//官方推荐写法
        }

        /**
         * 页签显示数据的方法
         */
        @Override
        public CharSequence getPageTitle(int position) {
            return CONTENT[position];
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            // TODO Auto-generated method stub
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ls=new ListView(DriverActivity.this);
            final List<HashMap<String, String>> list = new ArrayList<>();
            if(position==0) {
                for (int i = 0; i < message_list.size(); i++) {
                    if(message_list.get(i).getCity().equals(city)){
                    HashMap<String, String> map = new HashMap<>();
                    map.put("time", message_list.get(i).getTime());
                    map.put("message", message_list.get(i).getMessage());
                    list.add(map);
                    }
                }
                Collections.reverse(list);//逆序
                SimpleAdapter simplead = new SimpleAdapter(getApplicationContext(), list, R.layout.message_item, new String[] { "time", "message" }, new int[] {R.id.message_time,R.id.message_message});
                ls.setAdapter(simplead);
                ls.setVisibility(View.VISIBLE);
                ls.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String message=list.get(position).get("message");
                        Toast.makeText(DriverActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                });
            }
            else if (position==1)
            {
                for (int i = 0; i < message_list.size(); i++) {
                    if(!message_list.get(i).getCity().equals(city)) {
                        HashMap<String, String> map = new HashMap<>();
                        map.put("city", message_list.get(i).getCity());
                        map.put("message", message_list.get(i).getMessage());
                        map.put("time",message_list.get(i).getTime());
                        list.add(map);
                    }
                }
                Collections.reverse(list);//逆序
                SimpleAdapter simplead = new SimpleAdapter(getApplicationContext(), list, R.layout.message2_item, new String[] { "city", "message","time" }, new int[] {R.id.message_city,R.id.message_message,R.id.message_time});
                ls.setAdapter(simplead);
                ls.setVisibility(View.VISIBLE);
                ls.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                       String message=list.get(position).get("message");
                        Toast.makeText(DriverActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                });
            }
                container.addView(ls);
            return ls;
        }

    }
}
