package com.sczg.apple.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.sczg.apple.App;
import com.sczg.apple.base.BaseResponseObserver;
import com.sczg.apple.bean.DeviceMessagePostBean;
import com.sczg.apple.http.AppDataManager;
import com.sczg.apple.utils.AppMsgUtil;
import com.sczg.apple.utils.ShareUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.Nullable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

/**
 * Created by Administrator on 2018-04-18.
 */

public class LocationService extends Service {
    private String TAG = LocationService.class.getName();
    //声明mlocationClient对象
    public AMapLocationClient mlocationClient;
    //声明mLocationOption对象
    public AMapLocationClientOption mLocationOption = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        location();
    }

    private void location() {
        Log.e(TAG, "===定位开始 ");
        AMapLocationClient.updatePrivacyShow(App.getAppContext(), true, true);
        AMapLocationClient.updatePrivacyAgree(App.getAppContext(), true);
        try {
            mlocationClient = new AMapLocationClient(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位监听
        mlocationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation amapLocation) {
                if (amapLocation != null) {
                    if (amapLocation.getErrorCode() == 0) {
                        //定位成功回调信息，设置相关消息
                        amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                        amapLocation.getLatitude();//获取纬度
                        amapLocation.getLongitude();//获取经度
                        amapLocation.getAccuracy();//获取精度信息
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date date = new Date(amapLocation.getTime());
                        df.format(date);//定位时间
                        ShareUtil.saveLocationLat((float) amapLocation.getLatitude());
                        ShareUtil.saveLocationLng((float) amapLocation.getLongitude());
                        postLocation(amapLocation.getLatitude(), amapLocation.getLongitude());
                        Log.i(TAG, "Longitude=" + amapLocation.getLongitude() + ",Latitude=" + amapLocation.getLatitude());
                    } else {
                        //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                        Log.i("AmapError", "location Error, ErrCode:"
                                + amapLocation.getErrorCode() + ", errInfo:"
                                + amapLocation.getErrorInfo());
                    }
                }
            }
        });
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置定位间隔,单位毫秒,
        mLocationOption.setInterval(2 * 60 * 60 * 1000);
        //设置定位参数
        mlocationClient.setLocationOption(mLocationOption);
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为1000ms），并且在合适时间调用stopLocation()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用onDestroy()方法
        // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
        //启动定位
        mlocationClient.startLocation();
    }

    private void postLocation(double latitude, double longitude) {
        DeviceMessagePostBean deviceMessagePostBean = new DeviceMessagePostBean(
                latitude, longitude, AppMsgUtil.getIMEI(App.getAppContext())
        );
        AppDataManager.getInstance()
                .postDeviceMessage(deviceMessagePostBean)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseResponseObserver<ResponseBody>() {
                    @Override
                    public void onSuccess(ResponseBody response) {
                        Log.i("==post_location==", response.toString());
                    }
                });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "--------->onStartCommand: ");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "--------->onDestroy: ");
        super.onDestroy();
    }
}
