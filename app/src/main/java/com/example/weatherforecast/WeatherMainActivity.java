package com.example.weatherforecast;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.example.weatherforecast.database.LikesCityDBHelper;
import com.example.weatherforecast.enity.CityInfo;
import com.example.weatherforecast.enity.NowWeather;
import com.example.weatherforecast.util.WeatherUtils;

import com.example.weatherforecast.view.HorizontalListView;
import com.kongzue.dialogx.DialogX;
import com.kongzue.dialogx.dialogs.MessageDialog;
import com.kongzue.dialogx.dialogs.PopTip;
import com.kongzue.dialogx.dialogs.TipDialog;
import com.kongzue.dialogx.dialogs.WaitDialog;
import com.kongzue.dialogx.interfaces.OnDialogButtonClickListener;
import com.kongzue.dialogx.style.MIUIStyle;
import com.zaaach.citypicker.CityPicker;
import com.zaaach.citypicker.adapter.OnPickListener;
import com.zaaach.citypicker.model.City;
import com.zaaach.citypicker.model.HotCity;
import com.zaaach.citypicker.model.LocateState;
import com.zaaach.citypicker.model.LocatedCity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WeatherMainActivity extends AppCompatActivity implements View.OnClickListener {

    private final static int FUTURE_SEVEN_DAY = 100;
    private final static int CURRENT_WEATHER = 101;
    private final static int GET_LOCATION_ID = 102;
    private final static int TWENTY_FOUR = 103;
    private static String locationId = "";

    List<Map<String, Object>> list;
    private NowWeather nowWeather;

    private TextView windDir;
    private TextView humidity;
    private TextView windSpeed;
    private TextView vis;
    private TextView feel_like;
    private TextView weather_condition;
    private TextView temp_range;
    private TextView current_temp;
    private TextView life_index_text;
    private LinearLayout bg;

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CURRENT_WEATHER:
                    nowWeather = (NowWeather) msg.obj;
                    // 如果返回的NowWeather对象的状态码是404，代表不支持该城市
                    if (!"200".equals(nowWeather.getCode()) && "404".equals(nowWeather.getCode())) {
                        nowWeather.setText("获取失败");
                        nowWeather.setLifeIndexText("获取失败");
                        nowWeather.setWindDir("");
                        nowWeather.setWindSpeed("");
                        nowWeather.setWindScale("");
                        nowWeather.setTemp("获取失败");
                        nowWeather.setVis("");
                        nowWeather.setTempMin("");
                        nowWeather.setTempMax("");
                        weather_condition.setText(nowWeather.getText());
                        //添加确定按钮
//                        new AlertDialog.Builder(WeatherMainActivity.this)
//                                .setTitle("不支持该城市")//设置对话框标题
//                                .setMessage("不支持“" + sp.getString("currentCity", "广安") + "”的天气，点击确定重新选择")
//                                .setPositiveButton("确定", (dialog, which) -> city_btn.callOnClick())//确定按钮的响应事件
//                                .show();//在显示此对话框

                        MessageDialog.show("不支持该城市", "点击确定重新选择", "确定")
                                .setOkButton(new OnDialogButtonClickListener<MessageDialog>() {
                                    @Override
                                    public boolean onClick(MessageDialog baseDialog, View v) {
                                        city_btn.callOnClick();
                                        return false;
                                    }
                                });


                    }
                    // 根据当前天气情况设置对应的背景
                    switch (nowWeather.getText()) {
                        case "多云":
                            // OK
                            bg.setBackgroundResource(R.drawable.duoyun_bg);
                            break;
                        case "阴":
                            // OK
                            bg.setBackgroundResource(R.drawable.yintian_bg);
                            break;
                        case "晴":
                            // OK
                            bg.setBackgroundResource(R.drawable.qintian_bg);
                            break;
                        case "小雨":
                            // OK
                            bg.setBackgroundResource(R.drawable.xiaoyu_bg);
                            break;
                        case "阵雨":
                            // OK
                            bg.setBackgroundResource(R.drawable.zhenyu_bg);
                            break;
                        case "小雪":
                            // OK
                            bg.setBackgroundResource(R.drawable.xiaoxue_bg);
                            break;
                        case "中雪":
                            // OK
                            bg.setBackgroundResource(R.drawable.zhongxue_bg);
                            break;
                        case "阵雪":
                            // OK
                            bg.setBackgroundResource(R.drawable.zhenxue_bg);
                            break;
                        case "雾":
                            bg.setBackgroundResource(R.drawable.wu_bg);
                            break;
                        case "霾":
                            bg.setBackgroundResource(R.drawable.mai_bg);
                            break;
                        case "扬沙":
                            bg.setBackgroundResource(R.drawable.yangsha_bg);
                            break;
                    }

                    // 设置天气描述
                    weather_condition.setText(nowWeather.getText());

                    // 设置风力等级
                    windDir.setText(nowWeather.getWindDir() + " " + nowWeather.getWindScale() + "级");

                    // 设置湿度
                    humidity.setText(nowWeather.getHumidity());

                    // 设置风速
                    windSpeed.setText(nowWeather.getWindSpeed());

                    // 设置可见度
                    vis.setText(nowWeather.getVis());

                    // 设置体感温度
                    feel_like.setText(nowWeather.getFeelsLike());

                    current_temp.setText(nowWeather.getTemp());

                    temp_range.setText(nowWeather.getTempMin() + " ~ " + nowWeather.getTempMax());

                    // 设置生活指数建议
                    life_index_text.setText(nowWeather.getLifeIndexText());
                    Log.d("当前天气数据", "加载完成");
                    break;
                case FUTURE_SEVEN_DAY:
                    // 判断返回的是否为NowWeather的对象，如果是则代表不支持此城市，显示错误信息
                    list = null;
                    if (msg.obj instanceof NowWeather) {
                        list = new ArrayList<>();
                        Map<String, Object> map = new HashMap<>();
                        map.put("fxDate", "获取失败");
                        map.put("textDay", "获取失败");
                        map.put("tempMin", "");
                        map.put("tempMax", "");
                        map.put("weatherImg", R.drawable.error_icon);
                        list.add(map);
                    } else {
                        list = (List<Map<String, Object>>) msg.obj;
                    }

                    // 根据天气情况分配不同的图标
                    if (list != null) {
                        for (Map<String, Object> map : list) {
                            String textDay = (String) map.get("textDay");
                            switch (textDay) {
                                case "多云":
                                    map.put("weatherImg", R.drawable.duoyun_icon);
                                    break;
                                case "阴":
                                    map.put("weatherImg", R.drawable.yintian_icon);
                                    break;
                                case "晴":
                                    map.put("weatherImg", R.drawable.qintian_icon);
                                    break;
                                case "小雨":
                                    map.put("weatherImg", R.drawable.xiaoyu_icon);
                                    break;
                                case "阵雨":
                                    map.put("weatherImg", R.drawable.zhengyu_icon);
                                    break;
                                case "小雪":
                                    map.put("weatherImg", R.drawable.xiaoxue_icon);
                                    break;
                                case "阵雪":
                                    map.put("weatherImg", R.drawable.xiaoxue_icon);
                                    break;
                                case "大雪":
                                    map.put("weatherImg", R.drawable.daxue_icon);
                                    break;
                                case "雨夹雪":
                                    map.put("weatherImg", R.drawable.daxue_icon);
                                    break;
                                case "扬沙":
                                    map.put("weatherImg", R.drawable.yangsha_icon);
                                    break;
                                default:
                                    map.put("weatherImg", R.drawable.error_icon);
                                    break;
                            }
                        }
                    }
                    ListView forecast_temp_list = findViewById(R.id.forecast_temp_list);
                    SimpleAdapter futureSevenAdapter = new SimpleAdapter(WeatherMainActivity.this, list,
                            R.layout.future_weather_item,
                            new String[]{"fxDate", "textDay", "tempMin", "tempMax", "weatherImg"},
                            new int[]{R.id.temp_date, R.id.temp_condition, R.id.minTemp, R.id.maxTemp, R.id.temp_image}
                    );
                    forecast_temp_list.setAdapter(futureSevenAdapter);
                    Log.d("未来七天天气数据", "加载完成");
                    break;
                // 处理未来二十四小时的数据
                case TWENTY_FOUR:
                    List<Map<String, Object>> twentyFour = (List<Map<String, Object>>) msg.obj;
                    SimpleAdapter twentyFourAdapter = new SimpleAdapter(WeatherMainActivity.this, twentyFour,
                            R.layout.twenty_four_item,
                            new String[]{"weatherImg", "fxTime"},
                            new int[]{R.id.twenty_four_icon, R.id.twenty_four_text}
                    );
                    forecast_temp_twentyFour_list.setAdapter(twentyFourAdapter);
                    break;
            }
        }
    };


    private ImageButton like_city_list_btn;
    private Button city_btn;
    private SharedPreferences sp;
    private LikesCityDBHelper dbHelper;
    private SQLiteDatabase readLink;
    private SQLiteDatabase writeLink;
    private ImageButton like_city_btn;
    private ActivityResultLauncher<Intent> register;
    private HorizontalListView forecast_temp_twentyFour_list;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_main);

        // 初始化对话框
        DialogX.init(this);
        //设置为MIUI主题
        DialogX.globalStyle = new MIUIStyle();

        // 设置状态栏和导航栏为透明背景
        setStatusBarTranslation();
        // 隐藏标题栏
        getSupportActionBar().hide();
        // 获取应用必要权限
        initPermission();
        // 初始化屏幕组件
        InitializeComponents();


        // 给按钮绑定单击事件
        like_city_list_btn.setOnClickListener(this);
        city_btn.setOnClickListener(this);
        like_city_btn.setOnClickListener(this);

        // 创建数据库对象
        dbHelper = LikesCityDBHelper.getInstance(this);
        readLink = dbHelper.openReadLink();
        writeLink = dbHelper.openWriteLink();


        // 创建软件临时配置文件，用于保存当前选择的城市等信息
        sp = getSharedPreferences("weather_info", MODE_PRIVATE);
        if (!sp.contains("currentCity")) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("currentCity", "广安");
            editor.putString("currentCityLocationId", "101270801");
            editor.apply();
        }

        city_btn.setText(sp.getString("currentCity", "广安"));

        // 注册收藏页面的收藏项被点击后返回结果的处理
        register = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {


            @Override
            public void onActivityResult(ActivityResult result) {
                Intent data = result.getData();
                if (data != null) {
                    Bundle bundle = data.getExtras();
                    String cityName = bundle.getString("cityName");
                    CityInfo cityInfo = dbHelper.queryLikeCityByName(cityName);
                    String locationId = cityInfo.getLocation_id();
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("currentCity", cityName);
                    editor.putString("currentCityLocationId", locationId);
                    editor.apply();
                    Log.d("返回到主页面了", cityName);
                    // 初始化DialogX
                    DialogX.init(WeatherMainActivity.this);

                    if (isNetworkConnected(WeatherMainActivity.this)) {
                        // 获取当前实况天气
                        WeatherUtils.getCurrentWeather(locationId, handler, CURRENT_WEATHER);

                        // 获取未来7天的天气
                        WeatherUtils.getFutureSevenDay(locationId, handler, FUTURE_SEVEN_DAY);

                        // 获取未来24小时的天气
                        WeatherUtils.getFutureTwentyFourHour(locationId, handler, TWENTY_FOUR);

                        city_btn.setText(cityName);
                        like_city_btn.setBackgroundResource(R.drawable.likes);

                        // 显示正在加载动画
                        WaitDialog.show("正在加载...");
                        new Handler(Looper.getMainLooper()).postDelayed(WaitDialog::dismiss, 1000);

                    } else {
                        TipDialog.show("加载失败！当前无网络链接！", WaitDialog.TYPE.ERROR, 2000);
                    }

                }

            }
        });
        // 判断当前是否有网络连接
        if (isNetworkConnected(this)) {
            // 获取当前实况天气
            WeatherUtils.getCurrentWeather(sp.getString("currentCityLocationId", "101270801"), handler, CURRENT_WEATHER);
            // 获取未来7天的天气
            WeatherUtils.getFutureSevenDay(sp.getString("currentCityLocationId", "101270801"), handler, FUTURE_SEVEN_DAY);
            // 获取未来24小时的天气
            WeatherUtils.getFutureTwentyFourHour(sp.getString("currentCityLocationId", "101270801"), handler, TWENTY_FOUR);


            try {
                //主线程阻塞1秒，等待网络请求线程执行完毕
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 加载的动画
            WaitDialog.show("正在加载...");
            new Handler(Looper.getMainLooper()).postDelayed(WaitDialog::dismiss, 1000);
            Log.d(sp.getString("currentCity", "广安") + "天气数据", "加载完成");
        } else {
            // 如果没有网络连接，则显示错误信息
            TipDialog.show("加载失败！当前无网络链接！", WaitDialog.TYPE.ERROR, 2000);
            Log.d(sp.getString("currentCity", "广安") + "天气数据", "加载失败！当前无网络链接！");
        }


    }

    @Override
    protected void onStart() {
        super.onStart();
        // 第一次打开或返回到该页面时，查询数据库是否收藏了当前城市
        String cityName = sp.getString("currentCity", "广安");
        Log.d("当前城市", cityName);
        CityInfo cityInfo = dbHelper.queryLikeCityByName(cityName);
        if (cityInfo != null) {
            like_city_btn.setBackgroundResource(R.drawable.likes);
        } else {
            like_city_btn.setBackgroundResource(R.drawable.like);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isNetworkConnected(this)) {
            // 如果没有网络连接，则显示错误信息
            TipDialog.show("加载失败！当前无网络链接！", WaitDialog.TYPE.ERROR, 2000);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.closeLink();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.like_city_list_btn:
                Intent intent = new Intent(this, CityLikeActivity.class);
                register.launch(intent);
                break;
            case R.id.city_btn:
                showCitySelector();
                break;
            case R.id.like_city_btn:
                String cityName = sp.getString("currentCity", null);
                String locationId = sp.getString("currentCityLocationId", null);

                // 如果当前天气NowWeather对象的状态码为404，则表示不支持该城市
                if ("404".equals(nowWeather.getCode())) {
//                    Toast.makeText(getApplicationContext(), "收藏失败", Toast.LENGTH_SHORT).show();
                    PopTip.show("收藏失败").autoDismiss(1000);
                    Log.d("收藏失败", cityName + "\t" + locationId);
                    break;
                }

                CityInfo cityInfo = dbHelper.queryLikeCityByName(cityName);
                if (cityInfo != null) {
                    long deleteCount = dbHelper.deleteByCityName(cityName);
                    if (deleteCount != -1) {
//                        Toast.makeText(getApplicationContext(), "取消收藏成功", Toast.LENGTH_SHORT).show();
                        PopTip.show("取消收藏成功").autoDismiss(1000);
                        like_city_btn.setBackgroundResource(R.drawable.like);
                        Log.d("取消收藏成功", cityName + "\t" + locationId);
                    } else {
//                        Toast.makeText(getApplicationContext(), "取消收藏失败", Toast.LENGTH_SHORT).show();
                        PopTip.show("取消收藏失败").autoDismiss(1000);
                        Log.d("取消收藏失败", cityName + "\t" + locationId);
                    }
                } else {
                    long insert = dbHelper.insert(new CityInfo(cityName, locationId));

                    if (insert != -1) {
//                        Toast.makeText(getApplicationContext(), "收藏成功", Toast.LENGTH_SHORT).show();
                        PopTip.show("收藏成功").autoDismiss(1000);
                        like_city_btn.setBackgroundResource(R.drawable.likes);
                        Log.d("收藏成功", cityName + "\t" + locationId);


                    } else {
//                        Toast.makeText(getApplicationContext(), "收藏失败", Toast.LENGTH_SHORT).show();
                        PopTip.show("收藏失败").autoDismiss(1000);
                        Log.d("收藏失败", cityName + "\t" + locationId);

                    }
                }

                break;

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PERMISSION_GRANTED) {
                    // 申请成功
                    Log.d("获取应用必要权限", "成功");
                } else {
                    // 申请失败
//                    Toast.makeText(this, "请在设置中更改定位权限", Toast.LENGTH_SHORT).show();
                    PopTip.show("请在设置中更改定位权限").autoDismiss(1000);
                }
            }
        }
    }

    private void initPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
                // 检查权限状态
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    /**
                     * 用户彻底拒绝授予权限，一般会提示用户进入设置权限界面
                     * 第一次授权失败之后，退出App再次进入时，再此处重新调出允许权限提示框
                     */
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                } else {
                    /**
                     * 用户未彻底拒绝授予权限
                     * 第一次安装时，调出的允许权限提示框，之后再也不提示
                     */
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
            }
        }
    }


    private void showCitySelector() {
        List<HotCity> hotCities = new ArrayList<>();
        hotCities.add(new HotCity("广安", "四川", "101270801")); //code为城市代码
        hotCities.add(new HotCity("北京", "北京", "101010100"));
        hotCities.add(new HotCity("上海", "上海", "101020100"));
        hotCities.add(new HotCity("广州", "广东", "101280101"));
        hotCities.add(new HotCity("深圳", "广东", "101280601"));
        hotCities.add(new HotCity("杭州", "浙江", "101210101"));
        CityPicker.from(this) //activity或者fragment
                .enableAnimation(true) //启用动画效果，默认无
                //APP自身已定位的城市，传null会自动定位（默认）
//                .setLocatedCity(new LocatedCity("杭州", "浙江", "101210101"))
                .setLocatedCity(null) // 使用自动定位
                .setHotCities(hotCities)  //指定热门城市
                .setOnPickListener(new OnPickListener() {
                    @Override
                    public void onPick(int position, City data) {
                        String cityName = data.getName();
                        city_btn.setText(cityName);

                        // 如果已经收藏过选择的城市，则将收藏按钮改为实心
                        CityInfo cityInfo = dbHelper.queryLikeCityByName(cityName);
                        if (cityInfo != null) {
                            like_city_btn.setBackgroundResource(R.drawable.likes);
                        } else {
                            like_city_btn.setBackgroundResource(R.drawable.like);
                        }

//                        Toast.makeText(getApplicationContext(), cityName, Toast.LENGTH_SHORT).show();

                        String cityLocationId = data.getCode();
                        // 更新当前选择的城市名称和位置Id，以便下次打开软件时获取天气
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("currentCity", cityName);
                        editor.putString("currentCityLocationId", cityLocationId);
                        editor.apply();

                        // 判断当前网络状态
                        if (isNetworkConnected(WeatherMainActivity.this)) {
                            // 获取当前实况天气
                            WeatherUtils.getCurrentWeather(cityLocationId, handler, CURRENT_WEATHER);

                            // 获取未来7天的天气
                            WeatherUtils.getFutureSevenDay(cityLocationId, handler, FUTURE_SEVEN_DAY);

                            // 获取未来24小时的天气
                            WeatherUtils.getFutureTwentyFourHour(cityLocationId, handler, TWENTY_FOUR);
                            Log.d(cityName + "天气数据", "加载完成");
                            // 加载的动画
                            WaitDialog.show("正在加载...");
                            new Handler(Looper.getMainLooper()).postDelayed(WaitDialog::dismiss, 1000);
                        } else {
                            Log.d(cityName + "天气数据", "加载失败");
                            TipDialog.show("加载失败！当前无网络链接！", WaitDialog.TYPE.ERROR, 2000);
                        }


                    }

                    @Override
                    public void onCancel() {
//                        Toast.makeText(getApplicationContext(), "取消选择", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onLocate() {
                        //定位接口，需要APP自身实现，这里模拟一下定位
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // 获取位置，为list赋值
                                List<Address> addressList = getLocation();

                                if (addressList != null) {
                                    Address address = addressList.get(0);
                                    String province = address.getAdminArea();
                                    province = province.substring(0, province.length() - 1);
                                    String city = address.getLocality();
                                    city = city.substring(0, city.length() - 1);

                                    getLocationIdByCityName(city);

                                    // 不断阻塞当前线程1毫秒，直到静态属性locationId不为空串为止
                                    while ("".equals(WeatherMainActivity.locationId)) {
                                        try {
                                            //线程阻塞，等待网络请求线程执行完毕
                                            TimeUnit.MILLISECONDS.sleep(1);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    Log.d("定位得到的locationId", WeatherMainActivity.locationId);

                                    //定位完成之后更新数据
                                    CityPicker.from(WeatherMainActivity.this).locateComplete(new LocatedCity(city, province, WeatherMainActivity.locationId), LocateState.SUCCESS);
                                }
                            }
                        }, 2000);
                    }
                })
                .show();
    }

    // 初始化屏幕组件
    private void InitializeComponents() {
        windDir = findViewById(R.id.windDir);
        humidity = findViewById(R.id.humidity);
        windSpeed = findViewById(R.id.windSpeed);
        vis = findViewById(R.id.vis);
        feel_like = findViewById(R.id.feel_like);
        weather_condition = findViewById(R.id.weather_condition);
        temp_range = findViewById(R.id.temp_range);
        current_temp = findViewById(R.id.current_temp);
        life_index_text = findViewById(R.id.life_index_text);
        bg = findViewById(R.id.bg);
        // 我的收藏按钮
        like_city_list_btn = findViewById(R.id.like_city_list_btn);
        // 城市按钮
        city_btn = findViewById(R.id.city_btn);
        // 收藏城市按钮
        like_city_btn = findViewById(R.id.like_city_btn);

        forecast_temp_twentyFour_list = findViewById(R.id.forecast_temp_twentyFour_list);

    }

    List<Address> addressList = null;

    // 获取定位
    private List<Address> getLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(false);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        String providerName = "";
        List<String> providerList = locationManager.getProviders(true);


        if (providerList.contains(LocationManager.NETWORK_PROVIDER)) {
            providerName = LocationManager.NETWORK_PROVIDER;
        } else if (providerList.contains(LocationManager.GPS_PROVIDER)) {
            providerName = LocationManager.GPS_PROVIDER;
        } else {
//            Toast.makeText(WeatherMainActivity.this, "应用未取得定位权限", Toast.LENGTH_SHORT).show();
            PopTip.show("应用未取得定位权限").autoDismiss(1000);
            return addressList;
        }
        // 权限复验
        if (ActivityCompat.checkSelfPermission(WeatherMainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(WeatherMainActivity.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PERMISSION_GRANTED) {
//            Toast.makeText(WeatherMainActivity.this, "应用未取得定位权限", Toast.LENGTH_SHORT).show();
            PopTip.show("应用未取得定位权限").autoDismiss(1000);
            return addressList;
        }

        Location location = locationManager.getLastKnownLocation(providerName);
        if (location != null) {
            final double longitude = location.getLongitude();// 经度
            final double latitude = location.getLatitude();// 纬度
            Log.e("TAG", "经度longitude = " + longitude);
            Log.e("TAG", "纬度latitude = " + latitude);

            try {
                Geocoder geocoder = new Geocoder(WeatherMainActivity.this, Locale.getDefault());
                addressList = geocoder.getFromLocation(latitude, longitude, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
//            Toast.makeText(WeatherMainActivity.this, "UHello 定位失败", Toast.LENGTH_SHORT).show();
            PopTip.show("定位失败").autoDismiss(2000);
        }
        return addressList;
    }

    public static void getLocationIdByCityName(String cityName) {
        locationId = "";
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient okHttpClient = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(WeatherUtils.LOCATION_URL + cityName)
                        .method("GET", null)
                        .build();
                Call call = okHttpClient.newCall(request);
                String body = null;
                try {
                    Response response = call.execute();
                    body = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                JSONObject parse = JSONObject.parse(body);
                String code = parse.getString("code");
                if ("200".equals(code)) {
                    JSONArray location = parse.getObject("location", JSONArray.class);
                    Map<String, String> locationMap = location.getObject(0, Map.class);
                    String locationId = locationMap.get("id");
                    WeatherMainActivity.locationId = locationId;
                } else if ("402".equals(code)) {
                    Log.d("返回状态", code + "当日调用次数超啦！");
                } else if ("404".equals(code)) {
                    Log.d("返回状态", code + "不存在该城市！");
                }
            }
        }).start();
    }

    // 设置状态栏和导航栏为透明背景
    private void setStatusBarTranslation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // 设置状态栏透明
            getWindow().setStatusBarColor(getResources().getColor(R.color.transparent));
            //设置导航栏透明
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 设置状态栏透明
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //设置导航栏透明
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    // 判断当前是否联网
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }


}