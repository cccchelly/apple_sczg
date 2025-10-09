package com.sczg.apple.camrea;

/**
 * 相机初始化状态监听器
 * 用于监听相机初始化过程中的各个阶段状态
 */
public interface CameraInitListener {
    /**
     * 相机初始化开始
     */
    void onInitStart();
    
    /**
     * 相机设备枚举完成
     * @param deviceCount 发现的设备数量
     */
    void onDeviceEnumerated(int deviceCount);
    
    /**
     * 相机设备连接中
     */
    void onDeviceConnecting();
    
    /**
     * 相机设备连接成功
     */
    void onDeviceConnected();
    
    /**
     * 相机设备连接失败
     * @param error 错误信息
     */
    void onDeviceConnectFailed(String error);
    
    /**
     * 相机开始获取图像流
     */
    void onStartGrabbing();
    
    /**
     * 获取到第一帧图像
     */
    void onFirstFrameReceived();
    
    /**
     * 相机初始化完成，设备就绪
     */
    void onInitCompleted();
}