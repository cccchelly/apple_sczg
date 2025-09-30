package com.sczg.apple.http;

import com.sczg.apple.bean.DeviceMessagePostBean;
import com.sczg.apple.bean.PostPicMessageBean;
import com.sczg.apple.bean.TaskTimeBean;
import com.sczg.apple.http.cache.AppCache;
import com.sczg.apple.http.cache.IAcache;
import com.sczg.apple.http.network.IApi;
import com.sczg.apple.http.network.Net;

import java.util.List;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;

public final class AppDataManager implements IDataManager {


    private static AppDataManager mAppDataManager;
    private final IApi mIApi;
    private final AppCache mAppCache;

    private AppDataManager() {
        mIApi = Net.getInstance().create();
        mAppCache = AppCache.getInstence();
    }

    public static AppDataManager getInstance() {
        if (mAppDataManager == null) {
            synchronized (AppDataManager.class) {
                if (mAppDataManager == null) {
                    mAppDataManager = new AppDataManager();
                }
            }
        }
        return mAppDataManager;
    }

    @Override
    public IApi getApi() {
        return mIApi;
    }

    @Override
    public IAcache getAcache() {
        return mAppCache;
    }

    @Override
    public Observable<ResponseBody> setPhotoTask(TaskTimeBean taskTimeBean) {
        return mIApi.setPhotoTask(taskTimeBean);
    }

    @Override
    public Observable<ResponseBody> uploadFile(MultipartBody.Part file, String deviceId, String time) {
        return mIApi.uploadFile(file, deviceId, time);
    }

    @Override
    public Observable<ResponseBody> postDeviceMessage(DeviceMessagePostBean messagePostBean) {
        return mIApi.postDeviceMessage(messagePostBean);
    }


}
