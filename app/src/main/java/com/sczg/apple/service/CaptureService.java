package com.sczg.apple.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.sczg.apple.App;
import com.sczg.apple.AppContants;
import com.sczg.apple.camrea.CameraNormalManager;
import com.sczg.apple.utils.ShareUtil;
import com.sczg.apple.utils.TimeUtils;
import com.sczg.apple.utils.ToastUtils;

import java.text.SimpleDateFormat;

public class CaptureService extends Service {
    private static final String TAG = CaptureService.class.getName();
    Handler toastHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ToastUtils.showToast(msg.obj.toString());
        }
    };

    private boolean flagStop = false;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startPointTask();
    }

    private void startPointTask() {
        ToastUtils.showToast("定时拍照任务开始执行");
        flagStop = false;  //服务启动
        new Thread(() -> {
            try {
                while (true) {
                    String taskTime = ShareUtil.getTaskTime();
                    String nowTime = TimeUtils.millis2String(System.currentTimeMillis(), AppContants.taskTimeFormat);
                    if (flagStop) {    //检测到服务销毁，跳出循环
                        Log.i(TAG, "拍照任务停止");
                        break;
                    }
                    if (TextUtils.equals(taskTime, nowTime)) {
                        Log.i("--capture--", "时间匹配，开始执行拍照任务");
                        CameraNormalManager.taskCapturePhoto(AppContants.CaptureSource.CAPTURE_SOURCE_TASK);
                        Thread.sleep(60 * 1000);
                    } else {
                        Log.i("--capture--", "定时任务时间不匹配;"+nowTime +" != "+ taskTime);
                        Thread.sleep(20 * 1000);
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
        flagStop = true;
        super.onDestroy();
    }


    private void toastOnMain(String content) {
        Message msg = Message.obtain();
        msg.what = 0;
        msg.obj = content;
        toastHandle.sendMessage(msg);
    }

}