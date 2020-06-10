package com.example.bdmap.activity;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapframework.commonlib.asynchttp.AsyncHttpClient;
import com.baidu.mapframework.commonlib.asynchttp.TextHttpResponseHandler;
import com.example.bdmap.R;
import com.example.bdmap.sqlite.ImportDB;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private Button diver;
    private Button master;
    private double mCurrentLat = 0.0;
    private double mCurrentLon = 0.0;
    private String City;
    private String city_aode;//城市编码
    public LocationClient mLocationClient=null;
    private String url;
    private AsyncHttpClient mAsyncHttpClient;
    private BDLocation bdLocation;
    private boolean is_get=false;
    private String js=null;
    private TextView text_location;
    private TextView text_weather;
    private TextView text_wind;
    private TextView text_temp;
    private TextView text_message_1;
    private TextView text_message_2;
    private ImageView image_weather;
    private String city_name;
    private String weather;
    private String wind;
    private String temp;
    private String message_1;
    private String message_2;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
         super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text_location=findViewById(R.id.text_location);
        text_weather=findViewById(R.id.text_weather);
        text_wind=findViewById(R.id.text_wind);
        text_temp=findViewById(R.id.texd_temp);
        text_message_1=findViewById(R.id.text_message_1);
        text_message_2=findViewById(R.id.text_message_2);
        image_weather=findViewById(R.id.imag_weather);
        diver=findViewById(R.id.driver);
        master=findViewById(R.id.master);
        init_Permission();
        init_location();
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                    // 在此处添加执行的代码
                if(!is_get) {
                    mAsyncHttpClient = new AsyncHttpClient();
                    url = "http://api.map.baidu.com/weather/v1/?district_id=" + bdLocation.getAdCode() + "&data_type=all&ak=m7oxj7S146AY7Yz34zkPliGB9t7UOGw6";
                    mAsyncHttpClient.get(url, null, new TextHttpResponseHandler() {
                        @Override
                        public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
                            Toast.makeText(MainActivity.this.getApplicationContext(),
                                    "无法连接到网络", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onSuccess(int i, Header[] headers, String s) {
                            doingdata(s);
                        }
                    });
                    is_get=true;
                }
            }

        };
        handler.postDelayed(runnable, 1000);

        diver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,
                        DriverActivity.class);
                        intent.putExtra("mycity",City);
                startActivity(intent);
            }
        });
        master.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,
                        LoginActivity.class);
                startActivity(intent);
            }
        });
    }
    private void init_location() {
        mLocationClient = new LocationClient(this);
        //MyLocationListener myLocationListener = new MyLocationListener();
        //注册监听
        mLocationClient.registerLocationListener(BDAblistener);
        //配置定位
        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");//坐标类型
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//打开Gps
        option.setScanSpan(1000);//1000毫秒定位一次
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        mLocationClient.setLocOption(option);
        mLocationClient.start();

    }
    BDAbstractLocationListener BDAblistener = new BDAbstractLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if(location!=null) {
                bdLocation=location;
                mCurrentLat = location.getLatitude();
                mCurrentLon = location.getLongitude();
                City=location.getCity();
                city_aode=location.getAdCode();
            }
        }
    };
    private void init_Permission() {
        String[] per = {Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA};

        ActivityCompat.requestPermissions(this, per, 100);
    }

    private void doingdata(String s)
    {
        JSONObject js;
        try {
            js=new JSONObject(s);
            JSONObject result=js.getJSONObject("result");
            JSONObject location=result.getJSONObject("location");
            JSONObject now=result.getJSONObject("now");
            city_name=location.getString("city")+location.getString("name");
            weather=now.getString("text");
            wind=now.getString("wind_dir")+":"+now.getString("wind_class");
            temp=now.getString("temp")+"℃";
            text_location.setText(city_name);
            text_weather.setText(weather);
            text_wind.setText(wind);
            text_temp.setText(temp);
            set_png(weather);
            set_message(weather,now.getString("wind_class"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void set_png(String s){
        switch (s)
        {
            case "晴":image_weather.setImageResource(R.drawable.big_blue_00);break;
            case"多云":image_weather.setImageResource(R.drawable.big_blue_01);break;
            case"阴":image_weather.setImageResource(R.drawable.big_blue_02);break;
            case"阵雨":image_weather.setImageResource(R.drawable.big_blue_03);break;
            case"雷阵雨":image_weather.setImageResource(R.drawable.big_blue_04);break;
            case"雷阵雨伴有冰雹":image_weather.setImageResource(R.drawable.big_blue_05);break;
            case"雨夹雪":image_weather.setImageResource(R.drawable.big_blue_06);break;
            case"小雨":image_weather.setImageResource(R.drawable.big_blue_07);break;
            case"中雨":image_weather.setImageResource(R.drawable.big_blue_08);break;
            case"大雨":image_weather.setImageResource(R.drawable.big_blue_09);break;
            case"暴雨":image_weather.setImageResource(R.drawable.big_blue_10);break;
            case"大暴雨":image_weather.setImageResource(R.drawable.big_blue_11);break;
            case"特大暴雨":image_weather.setImageResource(R.drawable.big_blue_12);break;
            case"阵雪":image_weather.setImageResource(R.drawable.big_blue_13);break;
            case"小雪":image_weather.setImageResource(R.drawable.big_blue_14);break;
            case"中雪":image_weather.setImageResource(R.drawable.big_blue_15);break;
            case"大雪":image_weather.setImageResource(R.drawable.big_blue_16);break;
            case"暴雪":image_weather.setImageResource(R.drawable.big_blue_17);break;
            case"雾":image_weather.setImageResource(R.drawable.big_blue_18);break;
            case"冻雨":image_weather.setImageResource(R.drawable.big_blue_19);break;
            case"沙尘暴":image_weather.setImageResource(R.drawable.big_blue_20);break;
            case"浮尘":image_weather.setImageResource(R.drawable.big_blue_29);break;
            case"扬沙":image_weather.setImageResource(R.drawable.big_blue_30);break;
            case"强沙尘暴":image_weather.setImageResource(R.drawable.big_blue_31);break;
            case"强浓雾":image_weather.setImageResource(R.drawable.big_blue_49);break;
            case"霾":image_weather.setImageResource(R.drawable.big_blue_53);break;
            case"中度霾":image_weather.setImageResource(R.drawable.big_blue_54);break;
            case"重度霾":image_weather.setImageResource(R.drawable.big_blue_55);break;
            case"严重霾":image_weather.setImageResource(R.drawable.big_blue_56);break;
            case"大雾":image_weather.setImageResource(R.drawable.big_blue_57);break;
        }
    }
    private void set_message(String weather,String wind_clss){
        String w=weather;
        if(w.equals("小雨")||w.equals("中雨")){
            message_1="      注意路滑，打开雨刷，小心行驶!";
            text_message_1.setText(message_1);
            text_message_1.setTextColor(Color.parseColor("#FFFF00"));
        }
        else if(w.equals("大雨")||w.equals("暴雨")){
            message_1="      雨很大，打开车灯，低速行驶，尽量不要超车与急刹车！";
            text_message_1.setText(message_1);
            text_message_1.setTextColor(Color.parseColor("#FF6600"));
        }
        else if(w.equals("大暴雨")||w.equals("特大暴雨")) {
            message_1="      雨非常大，容易引发交通事故，不建议开车出行";
            text_message_1.setText(message_1);
            text_message_1.setTextColor(Color.parseColor("#FF0000"));
        }
        else if(w.equals("小雪")||w.equals("中雪")|| w.equals("冻雨")) {
            message_1="      注意路滑，保持车距，小心行驶！";
            text_message_1.setText(message_1);
            text_message_1.setTextColor(Color.parseColor("#FFFF00"));
        }
        else if(w.equals("大雪") || w.equals("暴雪")) {
            message_1="      路很滑，尽量不要超车，视线受阻，打开车灯，低速行驶！";
            text_message_1.setText(message_1);
            text_message_1.setTextColor(Color.parseColor("#FF6600"));
        }
        else if(w.equals("雾") || w.equals("霾") || w.equals("中度霾")) {
            message_1="      可见度比较低，打开车灯，降低车速，小心行驶！";
            text_message_1.setText(message_1);
            text_message_1.setTextColor(Color.parseColor("#FFFF00"));
        }
        else if(w.equals("重度霾") || w.equals("大雾")) {
            message_1="      可见度很低，打开车灯，保持间距，尽量不要超车，低速行驶！";
            text_message_1.setText(message_1);
            text_message_1.setTextColor(Color.parseColor("#FF6600"));
        }
        else if(w.equals("严重霾") || w.equals("特强浓雾")){
            message_1="      可见度非常低，打开车灯，慢速行驶，保持间距，不要超车！";
            text_message_1.setText(message_1);
            text_message_1.setTextColor(Color.parseColor("#FF0000"));
        }
        else if(w.equals("沙尘暴") || w.equals("强沙尘暴") || w.equals("浮尘") || w.equals("扬沙")) {
            message_1="      可能会在开车的途中受到飞来物，影响视线，车速不宜过高！";
            text_message_1.setText(message_1);
            text_message_1.setTextColor(Color.parseColor("#FFFF00"));
        }
       else {
            message_1="      天气对开车影响较小，注意安全！";
            text_message_1.setText(message_1);
            text_message_1.setTextColor(Color.parseColor("#00FF33"));
        }
        String s=wind_clss;
        s=s.substring(0,s.length()-1);
        int level=Integer.parseInt(s);
        if(level<6)
        {
            message_2="      风速对驾驶影响较小，注意安全！";
            text_message_2.setText(message_2);
            text_message_2.setTextColor(Color.parseColor("#00FF33"));
        }
        else if(level>=6&&level<8)
        {
            message_2="      风较大，车速不宜过高，尽量不要上高速！";
            text_message_2.setText(message_2);
            text_message_2.setTextColor(Color.parseColor("#FFFF00"));
        }
        else if(level>=8&&level<10)
        {
            message_2="     风很大，请低速行驶，不要上高速，注意行人！";
            text_message_2.setText(message_2);
            text_message_2.setTextColor(Color.parseColor("#FF6600"));
        }
        else {
            message_2="     风太大了，不适合开车，尽量不要出门！";
            text_message_2.setText(message_2);
            text_message_2.setTextColor(Color.parseColor("#FF0000"));
        }
    }
}
