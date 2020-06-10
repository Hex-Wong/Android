package com.example.bdmap.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;


import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.baidu.mapapi.utils.route.BaiduMapRoutePlan;
import com.baidu.mapapi.utils.route.RouteParaOption;
import com.baidu.mapframework.commonlib.asynchttp.AsyncHttpClient;
import com.baidu.mapframework.commonlib.asynchttp.TextHttpResponseHandler;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNaviCommonParams;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNRoutePlanManager;
import com.baidu.navisdk.adapter.IBaiduNaviManager;
import com.example.bdmap.baidudemo.DrivingRouteOverlay;
import com.example.bdmap.baidudemo.KeybordUtil;
import com.example.bdmap.baidudemo.NormalUtils;


import com.example.bdmap.baidudemo.PoiListAdapter;
import com.example.bdmap.baidudemo.PoiOverlay;
import com.example.bdmap.R;
import com.example.bdmap.baidudemo.ForegroundService;
import com.example.bdmap.sqlite.ImportDB;
import com.getbase.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static com.baidu.mapapi.map.MarkerOptions.MarkerAnimateType.grow;



public class MapActivity extends AppCompatActivity implements OnGetSuggestionResultListener,OnGetPoiSearchResultListener,PoiListAdapter.OnGetChildrenLocationListener,OnGetGeoCoderResultListener{
    private static final String APP_FOLDER_NAME = "BNSDKSimpleDemo";
    private static final String TAG="MapActivity";
    private BaiduMap mBaiduMap;
    private MapView mMapView = null;
    public LocationClient mLocationClient=null;
    private Button set_plcae;
    private Button navi;
    private EditText editText;
    private TextView mTextView;
    private BDLocation location;
    private MyLocationData locData;//定位坐标
    private int mCurrentDirection = 0;
    private BitmapDescriptor myIconLocation1;//当前位置的箭头图标
    private MyLocationConfiguration.LocationMode locationMode;//定位图层显示方式
    private double mCurrentLat = 0.0;
    private double mCurrentLon = 0.0;
    private BNRoutePlanNode sNode = null;
    private BNRoutePlanNode eNode = null;
    private String mSDCardPath = null;
    private PoiSearch mPoiSearch;
    private PoiResult poiResult;
    private SuggestionSearch mSuggestionSearch;
    private RelativeLayout mPoiDetailView;
    private ListView mPoiList;
    private ListView msug_list;
    private List<PoiInfo> mAllPoi;
    private List<SuggestionResult.SuggestionInfo>mAllSug;
    private boolean is_first_click=true;
    private boolean is_first_locate=true;
    private LatLng mylocation;
    private LatLng click_location=null;
    private Marker marker;
    private ImportDB importDB;
    private RoutePlanSearch routePlanSearch;
    private List<String>cityad_list;
    private GeoCoder mCoder ;
    private int max_temp=-50,min_temp=50;
    private int max_wind=-1,min_wind=50;
    BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.end);//构建Marker图标
    private static final String[] authBaseArr = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private BDAbstractLocationListener BDAblistener = new BDAbstractLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //定位方向
            mCurrentLat = location.getLatitude();
            mCurrentLon = location.getLongitude();
            //骑手定位
            locData = new MyLocationData.Builder()
                    .direction(mCurrentDirection).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            if(is_first_locate)
            {
                is_first_locate=false;
                mylocation = new LatLng(locData.latitude,locData.longitude);
                MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(mylocation);
                mBaiduMap.setMapStatus(msu);
                sNode = new BNRoutePlanNode.Builder()
                        .latitude(mylocation.latitude)
                        .longitude(mylocation.longitude)
                        .coordinateType(BNRoutePlanNode.CoordinateType.BD09LL)
                        .build();
            }


        }
    };
    private static final int authBaseRequestCode = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        navi=findViewById(R.id.navi);
        set_plcae=findViewById(R.id.set_place);
        mPoiDetailView=findViewById(R.id.poi_detail);
        mPoiList = (ListView) findViewById(R.id.poi_list);
        msug_list=findViewById(R.id.sug_list);
        init_Permission();//初始化权限
        init_location();//定位
        init_view();//初始化地图
        init_search();//搜索
        init_listener();//地图事件监听
        initRoutePlanNode();//初始化导航位置信息
        mPoiSearch = PoiSearch.newInstance();//Poi控件
        mPoiSearch.setOnGetPoiSearchResultListener(this);
        mCoder=GeoCoder.newInstance();
        mCoder.setOnGetGeoCodeResultListener(this);
        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(this);
        if (initDirs()) {
            initNavi();
        }
        startService(new Intent(this, ForegroundService.class));


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissions = new ArrayList<>();
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            }
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }

            if (permissions.size() != 0) {
                requestPermissionsForM(permissions);
            }
        }
    }
    private void initRoutePlanNode() {

        sNode = new BNRoutePlanNode.Builder()
                .latitude(40.050969)
                .longitude(116.300821)
                .coordinateType(BNRoutePlanNode.CoordinateType.BD09LL)
                .build();
        eNode = new BNRoutePlanNode.Builder()
                .latitude(39.908749)
                .longitude(116.397491)
                .name("北京天安门")
                .description("北京天安门")
                .coordinateType(BNRoutePlanNode.CoordinateType.BD09LL)
                .build();
    }
    private void  set_eNode(LatLng new_eNode)
    {
        eNode = new BNRoutePlanNode.Builder()
                .latitude(new_eNode.latitude)
                .longitude(new_eNode.longitude)
                .coordinateType(BNRoutePlanNode.CoordinateType.BD09LL)
                .build();
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
    private void requestPermissionsForM(final ArrayList<String> per) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(per.toArray(new String[per.size()]), 1);
        }
    }
    private void init_view()//显示地图
    {

        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setTrafficEnabled(true);//开启交通地图
        mBaiduMap.setMyLocationEnabled(true);
        set_plcae.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(click_location==null)
                {
                    Toast.makeText(MapActivity.this.getApplicationContext(), "请先点击要设置的地点", Toast.LENGTH_SHORT).show();
                }
                else {
                    final EditText editText = new EditText(MapActivity.this);
                    AlertDialog.Builder inputDialog =
                            new AlertDialog.Builder(MapActivity.this);
                    inputDialog.setTitle("请输入备注信息").setView(editText);
                    inputDialog.setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String pname= editText.getText().toString();
                                    if(pname==null)
                                    {
                                        Toast.makeText(MapActivity.this, "备注不能为空", Toast.LENGTH_LONG).show();
                                    }
                                    else {
                                        importDB=new ImportDB(getApplicationContext());
                                               String path = importDB.initDB();
                                               SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);//读写方式打开
                                               String sql="insert into myplaces (pname,latitude,longitude)values('"+pname+"','"+click_location.latitude+"','"+click_location.longitude+"')";
                                               db.execSQL(sql);
                                               db.close();
                                                Toast.makeText(MapActivity.this, "添加成功", Toast.LENGTH_LONG).show();
                                    }
                                }
                            }).show();
                }
            }
        });
        navi = (Button)findViewById(R.id.navi);
        navi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // routePlanToNavi(sNode,eNode);
                Toast.makeText(MapActivity.this, "正在获取天气信息", Toast.LENGTH_LONG).show();
                routePlanSearch =RoutePlanSearch.newInstance();
                routePlanSearch.setOnGetRoutePlanResultListener(new OnGetRoutePlanResultListener() {
                    @Override
                    public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {

                    }

                    @Override
                    public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

                    }

                    @Override
                    public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

                    }

                    @Override
                    public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
                        if (drivingRouteResult.getRouteLines().size() > 0) {
                            //获取路径规划数据,(以返回的第一条路线为例）
                            //为DrivingRouteOverlay实例设置数据
                            final DrivingRouteLine drivingRouteLine=drivingRouteResult.getRouteLines().get(0);
                            int j=drivingRouteLine.getAllStep().size();
                            int i;
                            cityad_list=new ArrayList<>();
                            final AsyncHttpClient mAsyncHttpClient = new AsyncHttpClient();
                            for (i=0;i<j;i++) {
                               mCoder.reverseGeoCode(new ReverseGeoCodeOption().location(drivingRouteLine.getAllStep().get(i).getEntrance().getLocation()));
                            }
                            final Handler handler = new Handler();
                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    // TODO Auto-generated method stub
                                    // 在此处添加执行的代码
                                    final List<String>weather =new ArrayList<>();
                                    for (int i=0;i<cityad_list.size();i++) {
                                        String   url = "http://api.map.baidu.com/weather/v1/?district_id=" + cityad_list.get(i) + "&data_type=all&ak=m7oxj7S146AY7Yz34zkPliGB9t7UOGw6";
                                        mAsyncHttpClient.get(url, null, new TextHttpResponseHandler() {
                                            @Override
                                            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
                                                Toast.makeText(MapActivity.this.getApplicationContext(),
                                                        "无法连接到网络", Toast.LENGTH_SHORT).show();
                                            }
                                            @Override
                                            public void onSuccess(int i, Header[] headers, String s) {
                                                try {
                                                    JSONObject jsonObject=new JSONObject(s);
                                                    JSONObject result=jsonObject.getJSONObject("result");
                                                    JSONObject now=result.getJSONObject("now");
                                                    int temp=now.getInt("temp");
                                                    String wind_class=now.getString("wind_class");
                                                    wind_class=wind_class.substring(0,wind_class.length()-1);
                                                    int wind=Integer.parseInt(wind_class);
                                                    String w=now.getString("text");
                                                    if(max_temp<temp){
                                                        max_temp=temp;
                                                    }
                                                    if(min_temp>temp) {
                                                        min_temp=temp;
                                                    }
                                                    if(max_wind<wind) {
                                                        max_wind=wind;
                                                    }
                                                    if(min_wind>wind){
                                                        min_wind=wind;
                                                    }
                                                    if(!has(weather,w)) {
                                                        weather.add(w);
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    }
                                    final Handler handler2 = new Handler();
                                    Runnable runnable2=new Runnable() {
                                        @Override
                                        public void run() {
                                            String weathers ="";
                                            for (int i=0;i<weather.size();i++)
                                            {
                                                weathers=weathers+weather.get(i)+" ";
                                            }
                                            AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
                                            builder.setTitle("天气信息");
                                            builder.setMessage("可能遇到的天气：" + weathers + "\n温度变化范围：" + min_temp +"-"+max_temp+"℃"+ "\n风速变化范围：" + min_wind+"-"+max_wind+"级"+"\n请做好充足的准备");
                                            builder.setPositiveButton("开始导航", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    routePlanToNavi(sNode,eNode);
                                                }
                                            });
                                            builder.show();
                                        }
                                    };handler2.postDelayed(runnable2, 500);
                                }

                            };
                            handler.postDelayed(runnable, 500);
                        }
                    }

                    @Override
                    public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

                    }

                    @Override
                    public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

                    }
                });
                PlanNode stNode=PlanNode.withLocation(mylocation);
                LatLng end=new LatLng(eNode.getLatitude(),eNode.getLongitude());
                PlanNode enNode=PlanNode.withLocation(end);
                routePlanSearch.drivingSearch(new DrivingRoutePlanOption().from(stNode).to(enNode));
            }
        });
        //三个悬浮窗
        FloatingActionButton actionA=findViewById(R.id.action_a);//定位
        actionA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mylocation = new LatLng(locData.latitude,locData.longitude);
                MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(mylocation);
                mBaiduMap.setMapStatus(msu);

                sNode = new BNRoutePlanNode.Builder()
                        .latitude(mylocation.latitude)
                        .longitude(mylocation.longitude)
                        .coordinateType(BNRoutePlanNode.CoordinateType.BD09LL)
                        .build();

            }
        });
        FloatingActionButton actionB=findViewById(R.id.action_b);//常去地点
        actionB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(MapActivity.this, MyplaceActivity.class);
                it.putExtra("mylatitude",mCurrentLat);
                it.putExtra("mylongitude",mCurrentLon);
                startActivity(it);

            }
        });
        FloatingActionButton actionC=findViewById(R.id.action_c);//加油站
        actionC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPoiSearch.searchNearby(new PoiNearbySearchOption()
                        .location(new LatLng(mylocation.latitude, mylocation.longitude))
                        .radius(20000)
                        .keyword("加油站")
                        .pageNum(10));


            }
        });
        msug_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SuggestionResult.SuggestionInfo suggestionInfo=mAllSug.get(position);
                showSugListView(false);//关闭搜索提示
                KeybordUtil.closeKeybord(MapActivity.this);//关闭键盘
                LatLng sug_location=new LatLng(suggestionInfo.pt.latitude,suggestionInfo.pt.longitude);
                click_location=sug_location;
                addPoiLoction(sug_location);
                set_eNode(sug_location);
            }
        });
        mPoiList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PoiInfo poiInfo = mAllPoi.get(position);
                if (poiInfo.getLocation() == null) {
                    return;
                }
                click_location=poiInfo.getLocation();
                addPoiLoction(poiInfo.getLocation());
                set_eNode(poiInfo.getLocation());
            }
        });

    }

    private  void  init_search()
    {
        editText=findViewById(R.id.search);
        Drawable drawable=getResources().getDrawable(R.drawable.search);
        drawable.setBounds(0,0,60,60);
        editText.setCompoundDrawables(drawable,null,null,null);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            public boolean onEditorAction(TextView v, int actionId,KeyEvent event)  {

                if (actionId==EditorInfo.IME_ACTION_SEND ||(event!=null&&event.getKeyCode()== KeyEvent.KEYCODE_ENTER))

                {
                    mPoiSearch.searchNearby(new PoiNearbySearchOption()
                            .location(new LatLng(mylocation.latitude, mylocation.longitude))
                            .radius(20000)
                            .keyword(v.getText().toString())
                            .pageNum(10));
                    return true;

                }

                return false;

            }

        });
        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                //text  输入框中改变后的字符串信息
                //start 输入框中改变后的字符串的起始位置
                //before 输入框中改变前的字符串的位置 默认为0
                //count 输入框中改变后的一共输入字符串的数量
                if (text.length() <= 0) {
                    return;
                }
                mSuggestionSearch.requestSuggestion((new SuggestionSearchOption())
                        .keyword(text.toString()) // 关键字
                        .city("廊坊" +
                                "" +
                                " ")); // 城市
                showSugListView(true);

            }

            @Override
            public void beforeTextChanged(CharSequence text, int start, int count,int after) {
                //text  输入框中改变前的字符串信息
                //start 输入框中改变前的字符串的起始位置
                //count 输入框中改变前后的字符串改变数量一般为0
                //after 输入框中改变后的字符串与起始位置的偏移量
                System.out.println(text.toString());
            }

            @Override
            public void afterTextChanged(Editable edit) {
                //edit  输入结束呈现在输入框中的信息
            }
        });
    }
        @Override
        public void onGetPoiResult(final  PoiResult result) {
            if (result == null || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
                Toast.makeText(MapActivity.this, "未找到结果", Toast.LENGTH_LONG).show();
                return;
            }

            if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                showPoiDetailView(true);
                mBaiduMap.clear();
                // 监听 View 绘制完成后获取view的高度
                mPoiDetailView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int padding = 50;
                        // 添加poi
                        PoiOverlay overlay = new MyPoiOverlay(mBaiduMap);
                        mBaiduMap.setOnMarkerClickListener(overlay);
                        overlay.setData(result);
                        overlay.addToMap();
                        // 获取 view 的高度
                        int PaddingBootom = mPoiDetailView.getMeasuredHeight();
                        // 设置显示在规定宽高中的地图地理范围
                        overlay.zoomToSpanPaddingBounds(padding,padding,padding,PaddingBootom);
                        // 加载完后需要移除View的监听，否则会被多次触发
                        mPoiDetailView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });

                double latitude = mylocation.latitude;
                double longitude = mylocation.longitude;
                int radius = 20000;
                showNearbyArea(new LatLng(latitude, longitude), radius);

                mAllPoi = result.getAllPoi();
                PoiListAdapter poiListAdapter = new PoiListAdapter(this, mAllPoi);
                poiListAdapter.setOnGetChildrenLocationListener(this);
                mPoiList.setAdapter(poiListAdapter);
                showPoiDetailView(true);

                return;
            }

            if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {
                // 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
                String strInfo = "在";

                for (CityInfo cityInfo : result.getSuggestCityList()) {
                    strInfo += cityInfo.city;
                    strInfo += ",";
                }

                strInfo += "找到结果";
                Toast.makeText(MapActivity.this, strInfo, Toast.LENGTH_LONG).show();
            }


        }
        @Override
        public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) { }
        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) { }
        //废弃
        @Override
        public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) { }
         @Override
         public void onGetSuggestionResult(SuggestionResult suggestionResult) {
        if (suggestionResult == null || suggestionResult.getAllSuggestions() == null) {
            return;
        }

        List<HashMap<String, String>> suggest = new ArrayList<>();
        for (SuggestionResult.SuggestionInfo info : suggestionResult.getAllSuggestions()) {
            if (info.getKey() != null && info.getDistrict() != null && info.getCity() != null) {
                HashMap<String, String> map = new HashMap<>();
                map.put("key",info.getKey());
                map.put("city",info.getCity());
                map.put("dis",info.getDistrict());
                suggest.add(map);
                mAllSug=suggestionResult.getAllSuggestions();

            }
        }

        SimpleAdapter simpleAdapter = new SimpleAdapter(getApplicationContext(),
                suggest,
                R.layout.item_layout,
                new String[]{"key", "city","dis"},
                new int[]{R.id.sug_key, R.id.sug_city, R.id.sug_dis});

        msug_list.setAdapter(simpleAdapter);
        simpleAdapter.notifyDataSetChanged();
    }
         @Override
         public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) { }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
        if(!has(cityad_list,Integer.toString(reverseGeoCodeResult.getAdcode())))
        cityad_list.add(Integer.toString(reverseGeoCodeResult.getAdcode()));
    }

    private void showPoiDetailView(boolean whetherShow) {//显示poi列表
        if (whetherShow) {
            mPoiDetailView.setVisibility(View.VISIBLE);

        } else {
            mPoiDetailView.setVisibility(View.GONE);

        }
    }
    private void showSugListView(boolean whetherShow)//显示sug提示列表
    {
        if(whetherShow)
            msug_list.setVisibility(View.VISIBLE);
        else
            msug_list.setVisibility(View.INVISIBLE);
    }
    public void showNearbyArea(LatLng center, int radius) {
        BitmapDescriptor centerBitmap = BitmapDescriptorFactory.fromResource(R.drawable.end);
        MarkerOptions ooMarker = new MarkerOptions().position(center).icon(centerBitmap);
        mBaiduMap.addOverlay(ooMarker);

        OverlayOptions ooCircle = new CircleOptions().fillColor( 0xCCCCCC00 )
                .center(center)
                .stroke(new Stroke(5, 0xFFFF00FF ))
                .radius(radius);

        mBaiduMap.addOverlay(ooCircle);
        centerBitmap.recycle();
    }
    @Override
    public void getChildrenLocation(LatLng childrenLocation) {
        addPoiLoction(childrenLocation);
    }

    private  class MyPoiOverlay extends PoiOverlay {
        MyPoiOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public boolean onPoiClick(int index) {
            super.onPoiClick(index);
            PoiInfo poi = getPoiResult().getAllPoi().get(index);
            Toast.makeText(MapActivity.this,poi.address,Toast.LENGTH_LONG).show();
            return true;
        }
    }
    private void addPoiLoction(LatLng latLng){
        mBaiduMap.clear();
        showPoiDetailView(false);
        OverlayOptions markerOptions = new MarkerOptions().position(latLng).icon(bitmap);
        mBaiduMap.addOverlay(markerOptions);
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(latLng);
        builder.zoom(18);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
    }

    private void init_listener() {
        BaiduMap.OnMapClickListener listener = new BaiduMap.OnMapClickListener() {

            @Override
            public void onMapClick(final LatLng point) {
                mBaiduMap.clear();
                click_location=point;
                MapStatusUpdate click_center = MapStatusUpdateFactory.newLatLng(click_location); //地图中心变为点击位置
                mBaiduMap.setMapStatus(click_center);
                    //构建MarkerOption，用于在地图上添加Marker
                    OverlayOptions option = new MarkerOptions()
                            .position(point)
                            .icon(bitmap)
                            .animateType(grow)
                            .visible(true);
                    //在地图上添加Marker，并显示
                    marker=(Marker)mBaiduMap.addOverlay(option);
                    set_eNode(point);
                    navi.setVisibility(View.VISIBLE);//
            }
            @Override
            public void onMapPoiClick(MapPoi mapPoi) {
                BitmapDescriptor bitmap = BitmapDescriptorFactory
                        .fromResource(R.drawable.end);
                    mBaiduMap.clear();
                LatLng poi_point=mapPoi.getPosition();
                click_location=mapPoi.getPosition();
                OverlayOptions option = new MarkerOptions()
                        .position(poi_point)
                        .icon(bitmap)
                        .animateType(grow)
                        .visible(true);
                //在地图上添加Marker，并显示
                marker=(Marker)mBaiduMap.addOverlay(option);
                set_eNode(poi_point);

            }


        };
        //设置地图单击事件监听
        mBaiduMap.setOnMapClickListener(listener);
    }
    private void init_Permission() {
        String[] per = {Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA};

        ActivityCompat.requestPermissions(this, per, 100);
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
                        Toast.makeText(MapActivity.this.getApplicationContext(),
                                "百度导航引擎初始化开始", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void initSuccess() {
                        Toast.makeText(MapActivity.this.getApplicationContext(),
                                "百度导航引擎初始化成功", Toast.LENGTH_SHORT).show();
                        // 初始化tts
                        initTTS();
                    }

                    @Override
                    public void initFailed(int errCode) {
                        Toast.makeText(MapActivity.this.getApplicationContext(),
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
                                Toast.makeText(MapActivity.this.getApplicationContext(),
                                        "算路开始", Toast.LENGTH_SHORT).show();
                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_SUCCESS:
                                Toast.makeText(MapActivity.this.getApplicationContext(),
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
                                Toast.makeText(MapActivity.this.getApplicationContext(),
                                        "算路失败", Toast.LENGTH_SHORT).show();
                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_TO_NAVI:
                                Toast.makeText(MapActivity.this.getApplicationContext(),
                                        "算路成功准备进入导航", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(MapActivity.this,
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
    private boolean has(List<String>list,String s)
    {
        for (int i=0;i<list.size();i++)
        {
            if(list.get(i).equals(s))
                return true;
        }
        return false;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[]
            grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == authBaseRequestCode) {
            for (int ret : grantResults) {
                if (ret == 0) {
                    continue;
                } else {
                    Toast.makeText(MapActivity.this.getApplicationContext(),
                            "缺少导航基本的权限!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            initNavi();
        }
    }
    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mLocationClient.stop();
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
        stopService(new Intent(this, ForegroundService.class));
    }
}
