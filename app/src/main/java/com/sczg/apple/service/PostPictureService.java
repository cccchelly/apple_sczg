package com.sczg.apple.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.sczg.apple.utils.CapturePostUtil;
import com.sczg.apple.utils.ShareUtil;
import com.sczg.apple.utils.TimeUtils;

import java.text.SimpleDateFormat;

public class PostPictureService extends Service {
    private String TAG = PostPictureService.class.getName();
    private boolean flagStop = false;

    Handler mHandler = new Handler();

    Runnable r = () -> {
        Log.i("--post--", "开始执行上传");
        new Thread(() -> CapturePostUtil.findLocalPic()).start();
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        flagStop = false;  //服务启动
        startPostTask();
    }

    private void startPostTask() {
        new Thread(() -> {
            try {
                String taskTime = ShareUtil.getTaskTime();
                while (true) {
                    String nowTime = TimeUtils.millis2String(System.currentTimeMillis(), new SimpleDateFormat("HH:mm"));
                    if (flagStop) {    // 检测到服务销毁，跳出循环
                        Log.i(TAG, "上传任务停止");
                        break;
                    }
                    if (TextUtils.equals(taskTime, nowTime)) {
                        mHandler.postDelayed(r, 5 * 60 * 1000);//延时执行上传服务
                        break;
                    } else {
                        Thread.sleep(1000);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "--------->onStartCommand: ");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "--------->onDestroy: ");
        //服务销毁，取消handler循环，置反标志位，时间检测循环退出
        mHandler.removeCallbacks(r);
        flagStop = true;
        super.onDestroy();
    }
}
