package com.sczg.apple.utils;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import com.sczg.apple.App;

public class ActivityBrightnessManager {


    /**
     * 获得当前屏幕亮度的模式
     *
     * @return 1 为自动调节屏幕亮度,0 为手动调节屏幕亮度,-1 获取失败
     */
    public static int getScreenMode() {
        int mode = -1;
        try {
            mode = Settings.System.getInt(App.getAppContext().getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return mode;
    }

    /**
     * 获得当前屏幕亮度值
     *
     * @return 0--255
     */
    public static int getScreenBrightness() {
        int screenBrightness = -1;
        try {
            screenBrightness = Settings.System.getInt(App.getAppContext().getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return screenBrightness;
    }

    /**
     * 设置当前屏幕亮度的模式
     *
     * @param mode 1 为自动调节屏幕亮度,0 为手动调节屏幕亮度
     */
    public static void setScreenMode(int mode) {
        try {
            Settings.System.putInt(App.getAppContext().getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE, mode);
            Uri uri = Settings.System
                    .getUriFor("screen_brightness_mode");
            App.getAppContext().getContentResolver().notifyChange(uri, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存当前的屏幕亮度值，并使之生效
     *
     * @param paramInt 0-255
     */
    public static void setScreenBrightness(int paramInt) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(App.getAppContext())) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + App.getAppContext().getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                App.getAppContext().startActivity(intent);
            } else {
                //有了权限
                Settings.System.putInt(App.getAppContext().getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS, paramInt);
                Uri uri = Settings.System
                        .getUriFor("screen_brightness");
                App.getAppContext().getContentResolver().notifyChange(uri, null);
            }

        }else {


            Settings.System.putInt(App.getAppContext().getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS, paramInt);
            Uri uri = Settings.System
                    .getUriFor("screen_brightness");
            App.getAppContext().getContentResolver().notifyChange(uri, null);
        }
    }

}
