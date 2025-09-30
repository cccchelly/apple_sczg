package com.sczg.apple.utils;

import android.util.Log;

import com.sczg.apple.App;
import com.sczg.apple.base.BaseResponseObserver;
import com.sczg.apple.bean.TaskTimeBean;
import com.sczg.apple.http.AppDataManager;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class TaskTimeUtil {
    public static void saveTaskTime(long time) {
        ShareUtil.saveTaskTime(time);
        resetTask();
    }

    public static void saveTaskTime(String timeStr) {
        ShareUtil.saveTaskTimeStr(timeStr);
        resetTask();
    }

    private static void resetTask() {
        postTaskTime();
        TaskServiceUtil.resetPhotoTasks();
    }

    private static void postTaskTime() {
        String taskTime = ShareUtil.getTaskTime();
        List timeList = new ArrayList();
        timeList.add(taskTime);

        TaskTimeBean taskTimeBean = new TaskTimeBean(timeList, AppMsgUtil.getIMEI(App.getAppContext()));
        AppDataManager.getInstance()
                .setPhotoTask(taskTimeBean)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseResponseObserver<ResponseBody>() {
                    @Override
                    public void onSuccess(ResponseBody response) {
                        Log.i("==post_time==", response.toString());
                    }
                });
    }
}
