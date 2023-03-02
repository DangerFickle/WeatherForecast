package com.example.weatherforecast.util;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.example.weatherforecast.R;
import com.example.weatherforecast.enity.NowWeather;

import okhttp3.*;

import java.io.IOException;
import java.util.*;

/**
 * @Title: WeatherUtils
 * @ProjectName SpringBoot
 * @Description: TODO
 * @Author DengChao
 * @Date 2022/12/323:11
 */
public class WeatherUtils {
    // key1：385eaf41961648c1bba04ddf62ba5da3
    // key2：37c269979a7d42c5bf60106264ae66aa
    // key3：be32d77df8dc421a958070e1849bb5ad
    // key4：7972e668947544cf8f2a1fac225cda90
    private static final String KEY = "37c269979a7d42c5bf60106264ae66aa";

    // 获取当前天气
    private static final String CURRENT_URL = "https://devapi.qweather.com/v7/weather/now?key=" + KEY + "&location=";

    // 获取指定城市的locationId
    public static final String LOCATION_URL = "https://geoapi.qweather.com/v2/city/lookup?key=" + KEY + "&location=";

    // 获取未来24小时天气预报
    private static final String FUTURE_TWENTY_HOUR = "https://devapi.qweather.com/v7/weather/24h?key=" + KEY + "&location=";

    // 获取未来3天的天气预报
    private static final String FUTURE_THREE_DAY = "https://devapi.qweather.com/v7/weather/3d?key=" + KEY + "&location=";

    //获取未来7天的天气预报
    private static final String FUTURE_SEVEN_DAY = "https://devapi.qweather.com/v7/weather/7d?key=" + KEY + "&location=";

    // 获取当天的生活指数
    private static final String TODAY_LIFE_INDEX = "https://devapi.qweather.com/v7/indices/1d?type=1&key=" + KEY + "&location=";


    // 获取当前天气
    public static void getCurrentWeather(String cityLocationId, Handler handler, int what) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 创建OkHttpClient以发送请求
                    OkHttpClient okHttpClient = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(CURRENT_URL + cityLocationId)
                            .method("GET", null)
                            .build();
                    Call call = okHttpClient.newCall(request);
                    Response response = call.execute();
                    String body = response.body().string();
                    JSONObject jsonObject = JSONObject.parse(body);

                    Map<String, String> map = (Map<String, String>) jsonObject.get("now");

                    String code = jsonObject.getString("code");//获取响应码
                    NowWeather nowWeather = new NowWeather();
                    // 如果没有当前城市的信息，则返回带有404状态码的NowWeather对象
                    if ("404".equals(code)) {
                        nowWeather.setCode(code);
                        Message message = Message.obtain();
                        message.what = what;
                        message.obj = nowWeather;
                        if (handler != null) {
                            handler.sendMessage(message);
                        }
                        return;
                    }

                    nowWeather.setCode(code);//设置响应码
                    if ("402".equals(code)) {
                        Log.d("响应状态", code + "今日api调用超次数啦！");
                        return;
                    }
                    String obsTime = map.get("obsTime").substring(0, 10);
                    nowWeather.setObsTime(obsTime);//设置天气日期
                    nowWeather.setWindDir(map.get("windDir"));//设置风向
                    nowWeather.setHumidity(map.get("humidity"));//设置相对湿度，百分比数值
                    nowWeather.setWindSpeed(map.get("windSpeed"));//设置风速
                    nowWeather.setVis(map.get("vis"));//设置能见度

                    nowWeather.setFeelsLike(map.get("feelsLike"));//设置体感温度

                    nowWeather.setTemp(map.get("temp"));//设置当前温度
                    nowWeather.setWindScale(map.get("windScale"));//设置风力等级


                    // 通过七天预报api获取当日的天气情况信息
//                    Map<String, String> sevenDayMap = getCurrentWeatherBySevenUrl(cityLocationId);
//                    String textDay = sevenDayMap.get("textDay");
                    String text = map.get("text");
                    nowWeather.setText(text);//设置天气描述


                    // 重新向每日天气预报api发送新请求，获取最高温度和最低温度
                    Request requestForTemp = new Request.Builder()
                            .url(FUTURE_THREE_DAY + cityLocationId)
                            .method("GET", null)
                            .build();
                    Call callForTemp = okHttpClient.newCall(requestForTemp);
                    String bodyForTemp = callForTemp.execute().body().string();
                    JSONObject tempJsonObject = JSONObject.parse(bodyForTemp);
                    JSONArray daily = tempJsonObject.getJSONArray("daily");
                    Map<String, String> dailyMap = (Map<String, String>) daily.get(0);
                    String tempMin = dailyMap.get("tempMin");//获取最高温度
                    String tempMax = dailyMap.get("tempMax");//获取最低温度
                    nowWeather.setTempMin(tempMin);//设置最高温度
                    nowWeather.setTempMax(tempMax);//设置最低温度

                    Map<String, String> lifeIndexMap = getTodayLifeIndex(cityLocationId);// 获取当日生活指数信息
                    String lifeIndexText = lifeIndexMap.get("text");
                    nowWeather.setLifeIndexText(lifeIndexText);// 设置当日生活指数描述
                    Message message = Message.obtain();
                    message.what = what;
                    message.obj = nowWeather;
                    if (handler != null) {
                        handler.sendMessage(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // 通过locationId获取对应城市当前的天气Map数据，数据在收藏页面使用
    public static Map<String, Object> getCurrentWeather(String cityLocationId) {
        // 创建OkHttpClient以发送请求
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(CURRENT_URL + cityLocationId)
                .method("GET", null)
                .build();
        Call call = okHttpClient.newCall(request);
        String body = null;
        try {
            body = call.execute().body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONObject jsonObject = JSONObject.parse(body);
        String code = jsonObject.getString("code");
        if ("402".equals(code)) {
            Log.d("响应状态", code + "今日api调用次数超啦！");
            return null;
        }

        Object nowObject = jsonObject.get("now");

        Map<String, Object> nowMap = null;

        if (nowObject instanceof Map) {
            nowMap = (Map<String, Object>) nowObject;
            nowMap.put("code", code);
        }

        return nowMap;
    }

    //获取未来7天的天气预报
    public static void getFutureSevenDay(String cityLocationId, Handler handler, int what) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    OkHttpClient okHttpClient = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(FUTURE_SEVEN_DAY + cityLocationId)
                            .method("GET", null)
                            .build();
                    Call call = okHttpClient.newCall(request);
                    Response response = call.execute();
                    String body = response.body().string();
                    JSONObject daily = JSONObject.parse(body);
                    String code = daily.getString("code");
                    if ("402".equals(code)) {
                        Log.d("响应状态", code + "今日api调用超次数啦！");
                        return;
                    }
                    if ("404".equals(code)) {
                        NowWeather nowWeather = new NowWeather();
                        nowWeather.setCode(code);
                        Message message = Message.obtain();
                        message.what = what;
                        message.obj = nowWeather;
                        if (handler != null) {
                            handler.sendMessage(message);
                        }
                        return;
                    }

                    List<Map<String, Object>> dailyList = daily.getObject("daily", ArrayList.class);
                    Message message = Message.obtain();
                    message.obj = dailyList;
                    message.what = what;
                    if (handler != null) {
                        handler.sendMessage(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public static Map<String, String> getTodayLifeIndex(String cityLocationId) {
        Map<String, String> map = null;
        try {
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(TODAY_LIFE_INDEX + cityLocationId)
                    .method("GET", null)
                    .build();
            Call call = okHttpClient.newCall(request);
            Response response = call.execute();
            String body = response.body().string();
            JSONObject jsonObject = JSONObject.parse(body);
            String code = jsonObject.getString("code");
            if ("402".equals(code)) {
                Log.d("响应状态", code + "今日api调用超次数啦！");
                return null;
            }
            map = (Map<String, String>) jsonObject.getJSONArray("daily").get(0);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    // 获取未来24小时的天气预报
    public static void getFutureTwentyFourHour(String locationId, Handler handler, int what) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient okHttpClient = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(FUTURE_TWENTY_HOUR + locationId)
                        .method("GET", null).build();
                Call call = okHttpClient.newCall(request);
                String body = null;
                try {
                    Response response = call.execute();
                    body = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                JSONObject jsonObject = JSONObject.parse(body);
                String code = jsonObject.getString("code");
                if ("200".equals(code)) {
                    List<Map<String, Object>> twentyFourList = jsonObject.getObject("hourly", ArrayList.class);
//                    Log.d("twentyFourList", String.valueOf(twentyFourList));
                    // 格式化事件为小时
                    for (Map<String, Object> map : twentyFourList) {
                        String fxTime = (String) map.get("fxTime");
                        fxTime = fxTime.substring(11, 16);
                        map.put("fxTime", fxTime);
                        // 根据天气情况分配图标
                        String weatherCondition = (String) map.get("text");

                        switch (weatherCondition) {
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
                            default:
                                map.put("weatherImg", R.drawable.error_icon);
                                break;
                        }
                    }
                    Message message = Message.obtain();
                    message.obj = twentyFourList;
                    message.what = what;
                    handler.sendMessage(message);
                }
            }
        }).start();
    }

    // 通过七天预报api获取当日的天气情况信息
    private static Map<String, String> getCurrentWeatherBySevenUrl(String cityLocationId) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(FUTURE_SEVEN_DAY + cityLocationId)
                .method("GET", null).build();
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
        if ("402".equals(code)) {
            Log.d("响应状态", code + "今日api调用超次数啦！");
            return null;
        }

        ArrayList location = parse.getObject("daily", ArrayList.class);
        Map<String, String> map = (Map<String, String>) location.get(0);
        return map;

    }

}