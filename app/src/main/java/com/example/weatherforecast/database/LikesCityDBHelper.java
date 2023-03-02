package com.example.weatherforecast.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;

import com.example.weatherforecast.enity.CityInfo;
import com.example.weatherforecast.util.WeatherUtils;
import com.zaaach.citypicker.model.City;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LikesCityDBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "city_info.db";
    private static final String TABLE_CITY_LIKES = "city_likes";
    private static final int DB_VERSION = 1;
    private static LikesCityDBHelper mHelper = null;
    private SQLiteDatabase mRDB = null;
    private SQLiteDatabase mWDB = null;

    private LikesCityDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // 利用单例模式获取数据库帮助器的唯一实例
    public static LikesCityDBHelper getInstance(Context context) {
        if (mHelper == null) {
            mHelper = new LikesCityDBHelper(context);
        }
        return mHelper;
    }

    // 打开数据库的读连接
    public SQLiteDatabase openReadLink() {
        if (mRDB == null || !mRDB.isOpen()) {
            mRDB = mHelper.getReadableDatabase();
        }
        return mRDB;
    }

    // 打开数据库的写连接
    public SQLiteDatabase openWriteLink() {
        if (mWDB == null || !mWDB.isOpen()) {
            mWDB = mHelper.getWritableDatabase();
        }
        return mWDB;
    }

    // 关闭数据库连接
    public void closeLink() {
        if (mRDB != null && mRDB.isOpen()) {
            mRDB.close();
            mRDB = null;
        }

        if (mWDB != null && mWDB.isOpen()) {
            mWDB.close();
            mWDB = null;
        }
    }

    // 创建数据库，执行建表语句
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_CITY_LIKES + " (" +
                " city_name VARCHAR NOT NULL UNIQUE," +
                " location_id VARCHAR NOT NULL UNIQUE);";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        String sql = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN phone VARCHAR;";
//        db.execSQL(sql);
//        sql = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN password VARCHAR;";
//        db.execSQL(sql);
    }

    public long insert(CityInfo cityInfo) {
        ContentValues values = new ContentValues();
        values.put("city_name", cityInfo.getCity_name());
        values.put("location_id", cityInfo.getLocation_id());
        // 执行插入记录动作，该语句返回插入记录的行号
        // 如果第三个参数values 为Null或者元素个数为0， 由于insert()方法要求必须添加一条除了主键之外其它字段为Null值的记录，
        // 为了满足SQL语法的需要， insert语句必须给定一个字段名 ，如：insert into person(name) values(NULL)，
        // 倘若不给定字段名 ， insert语句就成了这样： insert into person() values()，显然这不满足标准SQL的语法。
        // 如果第三个参数values 不为Null并且元素的个数大于0 ，可以把第二个参数设置为null 。
        //return mWDB.insert(TABLE_NAME, null, values);
        return mWDB.insert(TABLE_CITY_LIKES, null, values);
    }

    public long deleteByCityName(String cityName) {
        return mWDB.delete(TABLE_CITY_LIKES, "city_name=?", new String[]{cityName});
    }


    //    public List<User> queryAll() {
//        List<User> list = new ArrayList<>();
//        // 执行记录查询动作，该语句返回结果集的游标
//        Cursor cursor = mRDB.query(TABLE_NAME, null, null, null, null, null, null);
//        // 循环取出游标指向的每条记录
//        while (cursor.moveToNext()) {
//            User user = new User();
//            user.id = cursor.getInt(0);
//            user.name = cursor.getString(1);
//            user.age = cursor.getInt(2);
//            user.height = cursor.getLong(3);
//            user.weight = cursor.getFloat(4);
//            //SQLite没有布尔型，用0表示false，用1表示true
//            user.married = (cursor.getInt(5) == 0) ? false : true;
//            list.add(user);
//        }
//        return list;
//    }
//
    public CityInfo queryLikeCityByName(String cityName) {
        // 执行记录查询动作，该语句返回结果集的游标
        Cursor cursor = mRDB.query(TABLE_CITY_LIKES, null, "city_name=?", new String[]{cityName}, null, null, null);
        if (cursor.getCount() == 0) {
            return null;
        }
        CityInfo cityInfo = new CityInfo();
        cursor.moveToNext();
        cityInfo.setCity_name(cursor.getString(0));
        cityInfo.setLocation_id(cursor.getString(1));
        return cityInfo;
    }

    public List<CityInfo> queryLikeCityAll() {
        List<CityInfo> likedCityList = new ArrayList<>();
        Cursor cursor = mRDB.query(TABLE_CITY_LIKES, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            CityInfo cityInfo = new CityInfo();
            String cityName = cursor.getString(0);
            String locationId = cursor.getString(1);
            cityInfo.setCity_name(cityName);
            cityInfo.setLocation_id(locationId);
            likedCityList.add(cityInfo);
        }
        return likedCityList;
    }
}
