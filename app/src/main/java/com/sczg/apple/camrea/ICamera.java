package com.sczg.apple.camrea;

public interface ICamera {
    //初始化相机
    void initCamera();

    //连接相机
    void connectCamera();

    //执行拍照
    void captureExecute(String from);

    /// 预览
    void preview(PreviewListener previewListener);

    //结束相机
    void unInitCamera();

    //相机是否打开
    boolean isCameraOpen();

    //相机是否连接成功
    boolean isCameraConnect();

    //设置拍照监听
    void setOnCaptureListener(CaptureListener onCaptureListener);
    
    //设置相机初始化监听
    void setOnCameraInitListener(CameraInitListener onCameraInitListener);
    
    //设置抓图延迟时间
    void setCaptureDelayMs(long delayMs);
    
    //获取抓图延迟时间
    long getCaptureDelayMs();
}
