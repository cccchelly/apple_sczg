package com.sczg.apple.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.sczg.apple.App;
import com.sczg.apple.ui.activity.SplashActivity;


public class AppUtil {

    public static void restartApp() {
        AlarmManager mgr = (AlarmManager) App.getAppContext().getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(App.getAppContext(), SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("crash", true);
        PendingIntent restartIntent = PendingIntent.getActivity(App.getAppContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent); // 1秒钟后重启应用

        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
        System.gc();
    }


}
