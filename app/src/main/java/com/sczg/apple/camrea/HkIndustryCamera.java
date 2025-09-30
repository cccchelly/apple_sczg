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

    CaptureListener onCaptureListener;
    PreviewListener previewListener;
    //private int nDataSize = 30 * 1024 * 1024;        // 预设的图像数据大小
    private int nDataSize = 4096 * 2048 * 3;        // 预设的图像数据大小

    private int selectDeviceNum = 0;
    private ArrayList<MvCameraControlDefines.MV_CC_DEVICE_INFO> deviceList = new ArrayList<>();
    private HkCameraManager cameraManager;
    GetOneFrameThread getOneFrameThread;
    OpenDeviceThread openDeviceThread;
    StartGrabThread startGrabThread;

    @Override
    public void initCamera() {
        PermissionUtil.SetUSBPermission();
        PermissionUtil.SetUSBMemory();
        // 获取读写权限
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(App.getAppContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            }
        }
        cameraManager = new HkCameraManager();
        refreshMsg("SDK版本：" + cameraManager.GetSDKVersion());

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
            refreshMsg(e.errMsg + e.errCode);
            return;
        }
        if (deviceList != null) {
            int size = deviceList.size();
            if (size > 0) {
                List<String> deviceName = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    MvCameraControlDefines.MV_CC_DEVICE_INFO entity = deviceList.get(i);
                    if (entity.transportLayerType == MvCameraControlDefines.MV_GIGE_DEVICE) {
                        String str = "[" + i + "]"
                                + entity.gigEInfo.manufacturerName + "--"
                                + entity.gigEInfo.serialNumber + "--"
                                + entity.gigEInfo.deviceVersion + "--"
                                + entity.gigEInfo.userDefinedName;
                        deviceName.add(str);
                    } else {
                        String str = "[" + i + "]"
                                + entity.usb3VInfo.manufacturerName + "--"
                                + entity.usb3VInfo.serialNumber + "--"
                                + entity.usb3VInfo.deviceVersion + "--"
                                + entity.usb3VInfo.userDefinedName;
                        deviceName.add(str);
                    }
                }
            } else {
                refreshMsg("未枚举到设备");
            }
        } else {
            refreshMsg("未枚举到设备");
        }
    }

    @Override
    public void connectCamera() {
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

    class GetOneFrameThread extends Thread {
        boolean runGetFrameFlag = true;
        byte[] bytes = null;
        MvCameraControlDefines.MV_FRAME_OUT_INFO info = new MvCameraControlDefines.MV_FRAME_OUT_INFO();

        @Override
        public void run() {
            super.run();
            refreshMsg("===GetOneFrameThread===");
            Integer width = new Integer(0);
            Integer height = new Integer(0);

            /*cameraManager.setIntValue("Width", 5472);
            cameraManager.setIntValue("Height", 3648);*/

            cameraManager.setIntValue("Width", 4096);
            cameraManager.setIntValue("Height", 2370);
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
                    // 预览模式
                    if (previewListener != null && isPreview) {
                        previewListener.updateBitmap(bitmap);
                    }
                    // 抓图模式
                    if (isCapture) {
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
                                onCaptureListener.onCaptureFinish(bitmap, file, picName);
                            }
                        }
                        unInitCamera();
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
                nRet = cameraManager.closeDevice();
                if (nRet != MV_OK) {
                    refreshMsg("closeDevice fail nRet = " + Integer.toHexString(nRet));
                } else {
                    refreshMsg("=== closeDevice success ===");
                }
                return;
            }
        }
    }

    class StartGrabThread extends Thread {
        @Override
        public void run() {
            super.run();

            refreshMsg("===StartGrab====");
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
        LogUtil.i(msg);
    }

}
