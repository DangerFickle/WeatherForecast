package com.example.weatherforecast.enity;

public class NowWeather {
    private String code;// 请求后的状态码
    private String obsTime;// 数据观测时间
    private String temp;// 温度，默认单位：摄氏度
    private String feelsLike;// 体感温度，默认单位：摄氏度
    private String icon;// 天气状况和图标的代码
    private String text;// 天气状况的文字描述，包括阴晴雨雪等天气状态的描述
    private String wind360;// 风向360角度
    private String windDir;// 风向
    private String windScale;// 风力等级
    private String windSpeed;// 风速，公里/小时
    private String humidity;// 相对湿度，百分比数值
    private String precip;// 当前小时累计降水量，默认单位：毫米
    private String pressure;// 大气压强，默认单位：百帕
    private String vis;// 能见度，默认单位：公里
    private String tempMin;// 当日最高温度
    private String tempMax;// 当日最低温度
    private String lifeIndexText;// 生活指数建议


    public String getLifeIndexText() {
        return lifeIndexText;
    }

    public void setLifeIndexText(String lifeIndexText) {
        this.lifeIndexText = lifeIndexText;
    }

    public String getTempMin() {
        return tempMin;
    }

    public void setTempMin(String tempMin) {
        this.tempMin = tempMin;
    }

    public String getTempMax() {
        return tempMax;
    }

    public void setTempMax(String tempMax) {
        this.tempMax = tempMax;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getObsTime() {
        return obsTime;
    }

    public void setObsTime(String obsTime) {
        this.obsTime = obsTime;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public String getFeelsLike() {
        return feelsLike;
    }

    public void setFeelsLike(String feelsLike) {
        this.feelsLike = feelsLike;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getWind360() {
        return wind360;
    }

    public void setWind360(String wind360) {
        this.wind360 = wind360;
    }

    public String getWindDir() {
        return windDir;
    }

    public void setWindDir(String windDir) {
        this.windDir = windDir;
    }

    public String getWindScale() {
        return windScale;
    }

    public void setWindScale(String windScale) {
        this.windScale = windScale;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(String windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getPrecip() {
        return precip;
    }

    public void setPrecip(String precip) {
        this.precip = precip;
    }

    public String getPressure() {
        return pressure;
    }

    public void setPressure(String pressure) {
        this.pressure = pressure;
    }

    public String getVis() {
        return vis;
    }

    public void setVis(String vis) {
        this.vis = vis;
    }

    @Override
    public String toString() {
        return "NowWeather{" +
                "code='" + code + '\'' +
                ", obsTime='" + obsTime + '\'' +
                ", temp='" + temp + '\'' +
                ", feelsLike='" + feelsLike + '\'' +
                ", icon='" + icon + '\'' +
                ", text='" + text + '\'' +
                ", wind360='" + wind360 + '\'' +
                ", windDir='" + windDir + '\'' +
                ", windScale='" + windScale + '\'' +
                ", windSpeed='" + windSpeed + '\'' +
                ", humidity='" + humidity + '\'' +
                ", precip='" + precip + '\'' +
                ", pressure='" + pressure + '\'' +
                ", vis='" + vis + '\'' +
                ", tempMin='" + tempMin + '\'' +
                ", tempMax='" + tempMax + '\'' +
                ", lifeIndexText='" + lifeIndexText + '\'' +
                '}';
    }
}
