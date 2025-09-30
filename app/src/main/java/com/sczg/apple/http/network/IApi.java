package com.sczg.apple.http.network;

import com.sczg.apple.bean.DeviceMessagePostBean;
import com.sczg.apple.bean.PostPicMessageBean;
import com.sczg.apple.bean.TaskTimeBean;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface IApi {

    /// 任务设置
    @POST("device/taskTime")
    Observable<ResponseBody> setPhotoTask(@Body TaskTimeBean taskTimeBean);

    // 上传图片
    @Multipart
    @POST("device/imageReport")
    Observable<ResponseBody> uploadFile(
            @Part MultipartBody.Part file,
            @Query("deviceId") String deviceId,
            @Query("time") String time
    );

    /// 数据上传
    @POST("device/heartbeat")
    Observable<ResponseBody> postDeviceMessage(
            @Body DeviceMessagePostBean messagePostBean);
}
