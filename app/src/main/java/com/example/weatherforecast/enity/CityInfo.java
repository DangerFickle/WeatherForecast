package com.example.weatherforecast.enity;

public class CityInfo {
    private String city_name;
    private String location_id;

    public CityInfo() {
    }

    public CityInfo(String city_name, String location_id) {
        this.city_name = city_name;
        this.location_id = location_id;
    }

    public String getCity_name() {
        return city_name;
    }

    public void setCity_name(String city_name) {
        this.city_name = city_name;
    }

    public String getLocation_id() {
        return location_id;
    }

    public void setLocation_id(String location_id) {
        this.location_id = location_id;
    }

    @Override
    public String toString() {
        return "CityInfo{" +
                "city_name='" + city_name + '\'' +
                ", location_id='" + location_id + '\'' +
                '}';
    }
}
