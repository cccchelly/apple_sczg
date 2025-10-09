package com.sczg.apple.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.sczg.apple.App;
import com.sczg.apple.AppContants;

public class ShareUtil {
    private static final String SHARE_NAME = "share_name";
    private static final String TOKEN = "user_token";
    // 定时任务执行时间
    private static final String TIME_TASK_KEY = "time_task_key";
    private static final String LOCATION_LAT_KEY = "location_lat_key";
    private static final String LOCATION_LNG_KEY = "location_lng_key";
    // 抓图延迟时间（毫秒）
    private static final String CAPTURE_DELAY_KEY = "capture_delay_key";
    private static final long DEFAULT_CAPTURE_DELAY = 5000; // 默认5秒

    public static SharedPreferences getShare() {
        SharedPreferences sharedPreferences = App.getAppContext().getSharedPreferences(SHARE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences;
    }

    // token
    public static void saveToken(String token) {
        SharedPreferences.Editor editor = getShare().edit();
        editor.putString(TOKEN, token);
        editor.apply();
    }

    public static String getToken() {
        String token = getShare().getString(TOKEN, "");
        return token;
    }

    // 定时任务时间
    public static void saveTaskTime(long time) {
        String timeStr = TimeUtils.millis2String(time, AppContants.taskTimeFormat);
        saveTaskTimeStr(timeStr);
    }

    public static void saveTaskTimeStr(String time) {
        SharedPreferences.Editor editor = getShare().edit();
        editor.putString(TIME_TASK_KEY, time);
        editor.apply();
    }

    public static String getTaskTime() {
        String time = getShare().getString(TIME_TASK_KEY, AppContants.TASK_DEFAULT_TIME);
        return time;
    }

    public static void saveLocationLat(float lat) {
        SharedPreferences.Editor editor = getShare().edit();
        editor.putFloat(LOCATION_LAT_KEY, lat);
        editor.apply();
    }

    public static float getLocationLat() {
        float lat = getShare().getFloat(LOCATION_LAT_KEY, 0);
        return lat;
    }

    public static void saveLocationLng(float lng) {
        SharedPreferences.Editor editor = getShare().edit();
        editor.putFloat(LOCATION_LNG_KEY, lng);
        editor.apply();
    }

    public static float getLocationLng() {
        float lng = getShare().getFloat(LOCATION_LNG_KEY, 0);
        return lng;
    }

    // 抓图延迟时间
    /**
     * 保存抓图延迟时间（毫秒）
     * @param delayMs 延迟时间（毫秒）
     */
    public static void saveCaptureDelay(long delayMs) {
        SharedPreferences.Editor editor = getShare().edit();
        editor.putLong(CAPTURE_DELAY_KEY, delayMs);
        editor.apply();
    }

    /**
     * 获取抓图延迟时间（毫秒）
     * @return 延迟时间（毫秒），默认5000毫秒
     */
    public static long getCaptureDelay() {
        return getShare().getLong(CAPTURE_DELAY_KEY, DEFAULT_CAPTURE_DELAY);
    }

    /**
     * 获取抓图延迟时间（秒）
     * @return 延迟时间（秒），默认5秒
     */
    public static int getCaptureDelaySeconds() {
        return (int) (getCaptureDelay() / 1000);
    }

    /**
     * 保存抓图延迟时间（秒）
     * @param delaySeconds 延迟时间（秒）
     */
    public static void saveCaptureDelaySeconds(int delaySeconds) {
        saveCaptureDelay(delaySeconds * 1000L);
    }

}
