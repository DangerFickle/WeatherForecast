package com.example.weatherforecast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.weatherforecast.database.LikesCityDBHelper;
import com.example.weatherforecast.enity.CityInfo;
import com.example.weatherforecast.util.WeatherUtils;
import com.kongzue.dialogx.dialogs.MessageDialog;
import com.kongzue.dialogx.dialogs.PopTip;
import com.kongzue.dialogx.dialogs.TipDialog;
import com.kongzue.dialogx.dialogs.WaitDialog;
import com.kongzue.dialogx.interfaces.OnDialogButtonClickListener;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CityLikeActivity extends AppCompatActivity {

    private LikesCityDBHelper dbHelper;
    private SQLiteDatabase readLink;

    private TextView city_like_tag;
    private ListView weather_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_like);
        // 设置状态栏和导航栏为透明背景
        setStatusBarTranslation();

        // 隐藏标题栏
        getSupportActionBar().hide();

        // 设置全局点击缩放效果

        // 设置背景及透明度
        LinearLayout likes_layout = findViewById(R.id.likes_layout);
        likes_layout.getBackground().setAlpha(200);

        // 获取数据库对象
        dbHelper = LikesCityDBHelper.getInstance(this);

        // 获取屏幕控件
        city_like_tag = findViewById(R.id.city_like_tag);
        weather_list = findViewById(R.id.weather_list);


        // 判断当前是否链接网络
        if(WeatherMainActivity.isNetworkConnected(this)) {
            // 刷新收藏页面的数据
            refreshLikeData();
        } else {
            TipDialog.show("加载失败！当前无网络链接！", WaitDialog.TYPE.ERROR, 2000);
        }

        weather_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView city_name = view.findViewById(R.id.city_name);
                String cityNameText = (String) city_name.getText();
                Log.d("city_nameText", cityNameText);

                Intent intent = new Intent(CityLikeActivity.this, WeatherMainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("cityName", cityNameText);
                intent.putExtras(bundle);
                setResult(Activity.RESULT_OK, intent);

                // 缩放动画
//                AnimationSet aset_3=new AnimationSet(true);
//                ScaleAnimation aa_3=new ScaleAnimation(1, 0.1f, 1, 0.1f, Animation.RELATIVE_TO_SELF,1f,Animation.RELATIVE_TO_SELF,1f);
//                aa_3.setDuration(2000);
//                aset_3.addAnimation(aa_3);
//                view.startAnimation(aset_3);


                finish();
            }
        });


        weather_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView textView = view.findViewById(R.id.city_name);
                String cityName = textView.getText().toString();

                MessageDialog.build()
                        .setTitle("取消收藏")
                        .setMessage("是否取消收藏“" + cityName + "”？")
                        .setOkButton("确定", new OnDialogButtonClickListener<MessageDialog>() {
                            @Override
                            public boolean onClick(MessageDialog baseDialog, View v) {
                                long deleteCount = dbHelper.deleteByCityName(cityName);
                                if (deleteCount != -1) {
                                    PopTip.show("取消收藏成功").autoDismiss(1000);
                                    // 刷新收藏页面的数据
                                    refreshLikeData();
                                } else {
                                    PopTip.show("取消收藏失败，数据库中没有该城市").autoDismiss(1000);
                                }
                                return false;
                            }
                        })
                        .setCancelButton("取消")
                        .show();
                return true;
            }
        });

    }

    class LikesSimpleAdapter extends SimpleAdapter {
        // 用于保存每项的背景图片资源id
        List<Integer> bg_images = new ArrayList<>();

        public LikesSimpleAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
            // 遍历传入的list，获取每项的背景，以便单独设置LinearLayout的背景
            for (Map<String, ?> map : data) {
                Integer bg_img = (Integer) map.get("bg");
                bg_images.add(bg_img);
            }
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            // 当适配器获取了view之后，单独为LinearLayout设置背景
            Integer bg_resource = bg_images.get(position);
            view.findViewById(R.id.bg).setBackgroundResource(bg_resource);

            view.getBackground().setAlpha(235);

            return view;
        }
    }



    // 获取适配器所需的数据
    public List<Map<String, Object>> getAdapterList() {
        List<CityInfo> likedCityList = dbHelper.queryLikeCityAll();
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (CityInfo cityInfo : likedCityList) {
            new Thread(() -> {
                String cityName = cityInfo.getCity_name();
                String locationId = cityInfo.getLocation_id();
                Map<String, Object> nowMap = WeatherUtils.getCurrentWeather(locationId);
                nowMap.put("cityName", cityName);
                String weatherText = (String) nowMap.get("text");
                switch (weatherText) {
                    case "多云":
                        // OK
                        nowMap.put("bg", R.drawable.duoyunmp);
                        nowMap.put("icon", R.drawable.duoyun_icon);
                        break;
                    case "阴":
                        // OK
                        nowMap.put("bg", R.drawable.yinmp);
                        nowMap.put("icon", R.drawable.yintian_icon);
                        break;
                    case "晴":
                        // OK
                        nowMap.put("bg", R.drawable.qintianmp);
                        nowMap.put("icon", R.drawable.qintian_icon);
                        break;
                    case "小雨":
                        nowMap.put("bg", R.drawable.xiaoyump);
                        nowMap.put("icon", R.drawable.xiaoyu_icon);
                        break;
                    case "阵雨":
                        nowMap.put("bg", R.drawable.zhenyump);
                        nowMap.put("icon", R.drawable.dayu_icon);
                        break;
                    case "小雪":
                        nowMap.put("bg", R.drawable.xiaoxuemp);
                        nowMap.put("icon", R.drawable.xiaoxue_icon);
                        break;
                    case "中雪":
                        nowMap.put("bg", R.drawable.zhongxuemp);
                        nowMap.put("icon", R.drawable.xiaoxue_icon);
                        break;
                    case "阵雪":
                        nowMap.put("bg", R.drawable.zhenxuemp);
                        nowMap.put("icon", R.drawable.xiaoxue_icon);
                        break;
                    case "霾":
                        nowMap.put("bg", R.drawable.maimp);
                        nowMap.put("icon", R.drawable.mai_icon);
                        break;
                    case "雾":
                        nowMap.put("bg", R.drawable.wump);
                        nowMap.put("icon", R.drawable.wu_icon);
                        break;
                    case "扬沙":
                        nowMap.put("bg", R.drawable.yangshamp);
                        nowMap.put("icon", R.drawable.yangsha_icon);
                        break;
                    default:
                        nowMap.put("bg", R.drawable.unadpat);
                        nowMap.put("icon", R.drawable.error_icon);
                        break;
                }

//                    String text = (String) nowMap.get("text");
//                    Log.d("text", text);
                mapList.add(nowMap);
            }).start();
        }
        return mapList;
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

    // 刷新收藏页面的数据
    private void refreshLikeData() {
        List<CityInfo> likedCityList = dbHelper.queryLikeCityAll();
        List<Map<String, Object>> adapterList = getAdapterList();

        // 加载的动画
        WaitDialog.show("正在加载...");
//        new Handler(Looper.getMainLooper()).postDelayed(WaitDialog::dismiss, 1000);
        long start = System.currentTimeMillis();
        while (adapterList.size() != likedCityList.size()) {
            try {
                //主线程不断阻塞1毫秒，直到adapterList的长度等于数据库中收藏城市的个数
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        long end = System.currentTimeMillis();
        Log.d("收藏页面阻塞了", (end - start) + "毫秒");
        if (!adapterList.isEmpty()) {
            Log.d("城市收藏列表加载成功", "当前共收藏" + adapterList.size() + "个城市");
            city_like_tag.setText("当前共收藏" + adapterList.size() + "个城市");
            LikesSimpleAdapter simpleAdapter = new LikesSimpleAdapter(this, adapterList,
                    R.layout.like_item,
                    new String[]{"cityName", "humidity", "feelsLike", "temp", "icon"},
                    new int[]{R.id.city_name, R.id.humidity, R.id.feel_temp, R.id.now_temp, R.id.tqImage}
            );

            weather_list.setAdapter(simpleAdapter);
            city_like_tag.setText("共收藏" + adapterList.size() + "个城市");
        } else {
            city_like_tag.setText("还没有收藏城市呢！");
            // 当数据库中没有数据时，清空ListView的适配器，解决删除最后一项时不去掉item的问题
            weather_list.setAdapter(null);
        }
        WaitDialog.dismiss();
    }
}