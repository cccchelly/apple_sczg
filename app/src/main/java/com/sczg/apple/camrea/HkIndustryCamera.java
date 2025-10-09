package com.sczg.apple.camrea;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.sczg.apple.App;
import com.sczg.apple.AppContants;
import com.sczg.apple.bean.PicturePathBean;
import com.sczg.apple.utils.FileUtils;
import com.sczg.apple.utils.ImageUtils;
import com.sczg.apple.utils.LogUtil;
import com.sczg.apple.utils.PermissionUtil;
import com.sczg.apple.utils.TimeUtils;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import MvCameraControlWrapper.CameraControlException;
import MvCameraControlWrapper.MvCameraControl;
import MvCameraControlWrapper.MvCameraControlDefines;
import androidx.core.app.ActivityCompat;

import static MvCameraControlWrapper.MvCameraControlDefines.MV_OK;

public class HkIndustryCamera implements ICamera {
    private String from = AppContants.CaptureSource.CAPTURE_SOURCE_TASK;
    private boolean isPreview = false;
    private boolean isCapture = false;
    private boolean isCaptureCompleted = false; // 防止重复抓图的标志位
    private boolean isFirstFrameReceived = false; // 是否已经获取到第一帧的标志位

    CaptureListener onCaptureListener;
    PreviewListener previewListener;
    CameraInitListener onCameraInitListener;
    //private int nDataSize = 30 * 1024 * 1024;        // 预设的图像数据大小
    private int nDataSize = 4096 * 2048 * 3;        // 预设的图像数据大小
    
    // 可调整的抓图延迟时间（毫秒）
    private long captureDelayMs = 5000; // 默认5秒

    private int selectDeviceNum = 0;
    private ArrayList<MvCameraControlDefines.MV_CC_DEVICE_INFO> deviceList = new ArrayList<>();
    private HkCameraManager cameraManager;
    GetOneFrameThread getOneFrameThread;
    OpenDeviceThread openDeviceThread;
    StartGrabThread startGrabThread;

    @Override
    public void initCamera() {
        // 重置第一帧标志
        isFirstFrameReceived = false;
        
        // 通知初始化开始
        if (onCameraInitListener != null) {
            onCameraInitListener.onInitStart();
        }
        
        PermissionUtil.SetUSBPermission();
        PermissionUtil.SetUSBMemory();
        
        // 获取读写权限
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(App.getAppContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // 存储权限未授予
            }
        }
        
        cameraManager = new HkCameraManager();
        String sdkVersion = cameraManager.GetSDKVersion();
        refreshMsg("SDK版本：" + sdkVersion);

        int nTransLayers = MvCameraControl.MV_CC_EnumerateTls();
        if (nTransLayers == MvCameraControlDefines.MV_GIGE_DEVICE) {
            refreshMsg("GigeDevice");
        } else if (nTransLayers == MvCameraControlDefines.MV_USB_DEVICE) {
            refreshMsg("UsbDevice");
        } else if (nTransLayers == (MvCameraControlDefines.MV_GIGE_DEVICE + MvCameraControlDefines.MV_USB_DEVICE)) {
            refreshMsg("GigeDevice and UsbDevice");
        }

        selectDeviceNum = 0;
        deviceList.clear();
        
        try {
            deviceList = cameraManager.enumDevice();
        } catch (CameraControlException e) {
            e.printStackTrace();
            deviceList = null;
            String errMsg = (e.errMsg != null) ? e.errMsg : "未知错误";
            String errCode = String.valueOf(e.errCode);
            refreshMsg(errMsg + errCode);
            return;
        }
        
        if (deviceList != null) {
            int size = deviceList.size();
            // 通知设备枚举完成
            if (onCameraInitListener != null) {
                onCameraInitListener.onDeviceEnumerated(size);
            }
            if (size > 0) {
                List<String> deviceName = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    MvCameraControlDefines.MV_CC_DEVICE_INFO entity = deviceList.get(i);
                    if (entity.transportLayerType == MvCameraControlDefines.MV_GIGE_DEVICE) {
                        String manufacturerName = (entity.gigEInfo.manufacturerName != null) ? entity.gigEInfo.manufacturerName : "";
                        String serialNumber = (entity.gigEInfo.serialNumber != null) ? entity.gigEInfo.serialNumber : "";
                        String deviceVersion = (entity.gigEInfo.deviceVersion != null) ? entity.gigEInfo.deviceVersion : "";
                        String userDefinedName = (entity.gigEInfo.userDefinedName != null) ? entity.gigEInfo.userDefinedName : "";
                        String str = "[" + i + "]"
                                + manufacturerName + "--"
                                + serialNumber + "--"
                                + deviceVersion + "--"
                                + userDefinedName;
                        deviceName.add(str);
                    } else {
                        String manufacturerName = (entity.usb3VInfo.manufacturerName != null) ? entity.usb3VInfo.manufacturerName : "";
                        String serialNumber = (entity.usb3VInfo.serialNumber != null) ? entity.usb3VInfo.serialNumber : "";
                        String deviceVersion = (entity.usb3VInfo.deviceVersion != null) ? entity.usb3VInfo.deviceVersion : "";
                        String userDefinedName = (entity.usb3VInfo.userDefinedName != null) ? entity.usb3VInfo.userDefinedName : "";
                        String str = "[" + i + "]"
                                + manufacturerName + "--"
                                + serialNumber + "--"
                                + deviceVersion + "--"
                                + userDefinedName;
                        deviceName.add(str);
                    }
                }
            } else {
                refreshMsg("未枚举到设备");
                // 通知设备枚举完成，但没有发现设备
                if (onCameraInitListener != null) {
                    onCameraInitListener.onDeviceEnumerated(0);
                }
            }
        } else {
            refreshMsg("未枚举到设备");
            // 通知设备枚举完成，但没有发现设备
            if (onCameraInitListener != null) {
                onCameraInitListener.onDeviceEnumerated(0);
            }
        }
    }

    @Override
    public void connectCamera() {
        // 通知开始连接设备
        if (onCameraInitListener != null) {
            onCameraInitListener.onDeviceConnecting();
        }
        
        // 打开相机
        if (openDeviceThread == null) {
            openDeviceThread = new OpenDeviceThread();
            openDeviceThread.start();
        } else {
            if (!openDeviceThread.isAlive()) {
                openDeviceThread.start();
            }
        }
    }

    @Override
    public void captureExecute(String from) {
        this.from = from;
        isCapture = true;
        isCaptureCompleted = false; // 重置抓图完成标志位
        refreshMsg("===capture===");
        // 取流
        if (startGrabThread == null) {
            startGrabThread = new StartGrabThread();
            startGrabThread.start();
        }
        // 获取图像帧
        if (getOneFrameThread == null) {
            getOneFrameThread = new GetOneFrameThread();
            getOneFrameThread.runGetFrameFlag = true;
            getOneFrameThread.start();
        }
    }

    @Override
    public void preview(PreviewListener previewListener) {
        isPreview = true;
        refreshMsg("===preview===");
        this.previewListener = previewListener;
        // 取流
        if (startGrabThread == null) {
            startGrabThread = new StartGrabThread();
            startGrabThread.start();
        } else {
            if (!startGrabThread.isAlive()) {
                startGrabThread.start();
            }
        }
        // 获取图像帧
        if (getOneFrameThread == null) {
            getOneFrameThread = new GetOneFrameThread();
            getOneFrameThread.runGetFrameFlag = true;
            getOneFrameThread.start();
        } else {
            if (!getOneFrameThread.isAlive()) {
                getOneFrameThread.runGetFrameFlag = true;
                getOneFrameThread.start();
            }
        }
    }

    @Override
    public void unInitCamera() {
        // 停止循环取图像帧
        if (getOneFrameThread != null) {
            getOneFrameThread.runGetFrameFlag = false;
            getOneFrameThread = null;
        }

        startGrabThread = null;

        isCapture = false;
        isPreview = false;
        isCaptureCompleted = false; // 重置抓图完成标志位
        from = AppContants.CaptureSource.CAPTURE_SOURCE_TASK;
        if (cameraManager != null) {
            cameraManager.stopDevice();
            cameraManager.closeDevice();
            cameraManager.destroyHandle();
        }
    }

    @Override
    public boolean isCameraOpen() {
        return true;
    }

    @Override
    public boolean isCameraConnect() {
        return true;
    }

    @Override
    public void setOnCaptureListener(CaptureListener onCaptureListener) {
        this.onCaptureListener = onCaptureListener;
    }

    @Override
    public void setOnCameraInitListener(CameraInitListener onCameraInitListener) {
        this.onCameraInitListener = onCameraInitListener;
    }
    
    /**
     * 设置抓图延迟时间
     * @param delayMs 延迟时间（毫秒）
     */
    public void setCaptureDelayMs(long delayMs) {
        this.captureDelayMs = delayMs;
    }
    
    /**
     * 获取当前抓图延迟时间
     * @return 延迟时间（毫秒）
     */
    public long getCaptureDelayMs() {
        return this.captureDelayMs;
    }

    class GetOneFrameThread extends Thread {
        boolean runGetFrameFlag = true;
        byte[] bytes = null;
        MvCameraControlDefines.MV_FRAME_OUT_INFO info = new MvCameraControlDefines.MV_FRAME_OUT_INFO();
        // 抓图模式延迟相关变量
        private long captureStartTime = 0;

        @Override
        public void run() {
            super.run();
            refreshMsg("===GetOneFrameThread===");
            
            if (cameraManager == null) {
                refreshMsg("相机管理器未初始化");
                unInitCamera();
                return;
            }
            
            Integer width = new Integer(0);
            Integer height = new Integer(0);

            cameraManager.setIntValue("Width", 5472);
            cameraManager.setIntValue("Height", 3648);

            /*cameraManager.setIntValue("Width", 4096);
            cameraManager.setIntValue("Height", 2370);*/
            int nRetW = cameraManager.getIntValue("Width", width);
            int nRetH = cameraManager.getIntValue("Height", height);

            if (nRetW == MV_OK && nRetH == MV_OK) {
                if (bytes == null) {
                    bytes = new byte[width * height * 3];
                } else {
                    if (bytes.length < width * height * 3) {
                        bytes = new byte[width * height * 3];
                    }
                }
            } else {
                refreshMsg("获取图像宽高失败");
                unInitCamera();
                return;
            }

            while (runGetFrameFlag) {
                int nRet = cameraManager.getOneFrameTimeout(bytes, info, 1000);
                if (nRet == 0) {
                    if (bytes == null) {
                        Log.e("====", "get copy data error!");
                        return;
                    }

                    Bitmap bitmap = ImageUtils.rgb2Bitmap(bytes, width, height);
                    
                    // 如果是第一次获取到帧，通知第一帧回调和初始化完成
                    if (!isFirstFrameReceived) {
                        isFirstFrameReceived = true;
                        // 通知获取到第一帧
                        if (onCameraInitListener != null) {
                            onCameraInitListener.onFirstFrameReceived();
                        }
                        // 通知相机初始化完成，设备就绪
                        if (onCameraInitListener != null) {
                            onCameraInitListener.onInitCompleted();
                        }
                    }
                    
                    // 如果是抓图模式且还未开始计时，在获取到第一帧时开始计时
                    if (isCapture && captureStartTime == 0) {
                        captureStartTime = System.currentTimeMillis();
                        refreshMsg("获取到第一帧，开始延迟计时，等待" + (captureDelayMs / 1000) + "秒后开始抓图...");
                        // 只有手动拍照才通知抓图开始（显示进度弹窗）
                        if (onCaptureListener != null && TextUtils.equals(from, AppContants.CaptureSource.CAPTURE_SOURCE_HAND)) {
                            onCaptureListener.onCaptureStart(captureDelayMs);
                        }
                    }
                    
                    // 预览模式
                    if (previewListener != null && isPreview) {
                        previewListener.updateBitmap(bitmap);
                    }
                    // 抓图模式
                    if (isCapture && !isCaptureCompleted && captureStartTime > 0) {
                        // 检查是否已经等待了设定的延迟时间
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - captureStartTime >= captureDelayMs) {
                            // 设置标志位，防止重复抓图
                            isCaptureCompleted = true;
                            refreshMsg("倒计时完毕，正在处理截图...");
                            
                            // 通知开始处理截图（保存图片和数据）
                            if (onCaptureListener != null && TextUtils.equals(from, AppContants.CaptureSource.CAPTURE_SOURCE_HAND)) {
                                onCaptureListener.onCaptureProcessing();
                            }
                            
                            String picName = TimeUtils.millis2String(System.currentTimeMillis(),
                                    new SimpleDateFormat("yyyyMMddHHmmss")) + ".jpg";
                            //文件保存到内存
                            ImageUtils.saveImage(bitmap, picName);
                            //文件地址保存到数据库
                            PicturePathBean data = new PicturePathBean();
                            data.setPath(picName);
                            data.save();
                            File file = FileUtils.getFileFromSdcard(picName);

                            if (null != onCaptureListener) {
                                if (TextUtils.equals(from, AppContants.CaptureSource.CAPTURE_SOURCE_HAND)) {
                                    if (bitmap != null) {
                                        refreshMsg("抓图成功，准备显示图片，图片尺寸: " + bitmap.getWidth() + "x" + bitmap.getHeight());
                                        onCaptureListener.onCaptureFinish(bitmap, file, picName);
                                    } else {
                                        refreshMsg("抓图失败：bitmap为空");
                                    }
                                }
                            }
                            // 延迟关闭相机，确保回调能够正确执行
                            new Thread(() -> {
                                try {
                                    Thread.sleep(1000); // 等待1秒确保回调执行完成
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                unInitCamera();
                            }).start();
                        } else {
                            // 还没到设定时间，继续等待
                            long remainingTime = captureDelayMs - (currentTime - captureStartTime);
                            refreshMsg("等待中...剩余时间: " + (remainingTime / 1000) + "秒");
                            // 只有手动拍照才通知进度更新（显示进度弹窗）
                            if (onCaptureListener != null && TextUtils.equals(from, AppContants.CaptureSource.CAPTURE_SOURCE_HAND)) {
                                onCaptureListener.onCaptureProgress(remainingTime, captureDelayMs);
                            }
                        }
                    }
                }
            }
        }
    }

    class OpenDeviceThread extends Thread {
        @Override
        public void run() {
            refreshMsg("===OpenDeviceThread");
            if (deviceList.size() == 0) {
                refreshMsg("请先枚举设备");
                unInitCamera();
                return;
            }
            try {
                cameraManager.createHandle(deviceList.get(selectDeviceNum));
            } catch (CameraControlException e) {
                e.printStackTrace();
                refreshMsg(e.errMsg + e.errCode);
                return;
            }

            int nRet = cameraManager.openDevice();

            if (nRet != MV_OK) {
                refreshMsg("OpenDevice fail nRet = " + Integer.toHexString(nRet));
                // 通知设备连接失败
                if (onCameraInitListener != null) {
                    onCameraInitListener.onDeviceConnectFailed("设备连接失败，错误码: " + Integer.toHexString(nRet));
                }
                nRet = cameraManager.closeDevice();
                if (nRet != MV_OK) {
                    refreshMsg("closeDevice fail nRet = " + Integer.toHexString(nRet));
                } else {
                    refreshMsg("=== closeDevice success ===");
                }
                return;
            }
            
            // 通知设备连接成功
            if (onCameraInitListener != null) {
                onCameraInitListener.onDeviceConnected();
            }
        }
    }

    class StartGrabThread extends Thread {
        @Override
        public void run() {
            super.run();

            refreshMsg("===StartGrab====");
            // 通知开始获取图像流
            if (onCameraInitListener != null) {
                onCameraInitListener.onStartGrabbing();
            }
            setCameraConfig();
            if (!deviceList.isEmpty() && deviceList.get(selectDeviceNum).transportLayerType == MvCameraControlDefines.MV_GIGE_DEVICE) {


                int nPacketSize = cameraManager.getOptimalPacketSize();
                if (nPacketSize > 0) {
                    int nRet = cameraManager.setIntValue("GevSCPSPacketSize", nPacketSize);
                    if (nRet != MV_OK) {
                        refreshMsg("Warning: Set Packet Size fail nRet" + Integer.toHexString(nRet));
                    } else {
                        refreshMsg("set GevSCPSPacketSize 1500");
                    }
                } else {
                    refreshMsg("Warning: Get Packet Size fail nRet" + Integer.toHexString(nPacketSize));
                }

                Integer GevSCPD = new Integer(0);
                int nRet = cameraManager.getIntValue("GevSCPD", GevSCPD);
                if (nRet != MV_OK) {
                    refreshMsg("get GevSCPD fail nRet = " + Integer.toHexString(nRet));
                } else {
                    refreshMsg("GevSCPD = " + GevSCPD.intValue());
                }

                nRet = cameraManager.setIntValue("GevSCPD", 2000);
                if (nRet == MV_OK) {
                    refreshMsg("Set GevSCPD success");
                } else {
                    refreshMsg("Set GevSCPD fail");
                }

                nRet = cameraManager.getIntValue("GevSCPD", GevSCPD);
                if (nRet != MV_OK) {
                    refreshMsg("get GevSCPD fail nRet = " + Integer.toHexString(nRet));
                } else {
                    refreshMsg("GevSCPD = " + GevSCPD.intValue());
                }
            }


            final Integer width = new Integer(0);
            int nRet = cameraManager.getIntValue("Width", width);
            if (nRet != MV_OK) {
                refreshMsg("get Width fail nRet = " + Integer.toHexString(nRet));
                return;
            }

            final Integer height = new Integer(0);
            nRet = cameraManager.getIntValue("Height", height);
            if (nRet != MV_OK) {
                refreshMsg("get Height fail nRet = " + Integer.toHexString(nRet));
                return;
            }

            final Integer pixelFormat = new Integer(0);
            nRet = cameraManager.getEnumValue("PixelFormat", pixelFormat);
            refreshMsg("get PixelFormat nRet = " + Integer.toHexString(nRet));
            if (nRet != MV_OK) {
                return;
            }

            nRet = cameraManager.startDevice();
            if (nRet != MV_OK) {
                refreshMsg("startDevice" + Integer.toHexString(nRet));
            }
        }
    }

    public void setCameraConfig() {

        /*/// 文档和SDK对应不上，这个值是从官方mvs软件上调试获取的，对应RGB8
        int nRet = cameraManager.setEnumValue("PixelFormat", 0x02180014);
        if (nRet != MV_OK) {
            refreshMsg("set PixelFormat fail nRet = " + Integer.toHexString(nRet));
        }
        // 0:Off 1:Once 2:Continuous
        nRet = cameraManager.setEnumValue("ExposureAuto", 2);
        if (nRet != MV_OK) {
            refreshMsg("set PixelFormat fail nRet = " + Integer.toHexString(nRet));
        }
        // 图像压缩模式 0:Off 1:JPEG
        cameraManager.setEnumValue("ImageCompressionMode", 1);

        // 图像压缩质量
        cameraManager.setIntValue("ImageCompressionQuality", 100);

        // 采集模式，单帧、多帧、连续
        // 0:SingleFrame  1:MultiFrame  2:Continuous
        cameraManager.setEnumValue("AcquisitionMode", 2);

        // 自动增益
        // 0:Off  1:Once  2:Continuous
        cameraManager.setEnumValue("GainAuto", 2);

        // 自动白平衡
        // 0:Off  1:Continuous  2:Once
        cameraManager.setEnumValue("BalanceWhiteAuto", 1);

        Integer exposureAuto = new Integer(0);
        nRet = cameraManager.getEnumValue("ExposureAuto", exposureAuto);
        refreshMsg("ExposureAuto" + Integer.toHexString(nRet));*/
    }

    private void refreshMsg(String msg) {
        String message = (msg != null) ? msg : "";
        LogUtil.i(message);
    }

}
