package com.sczg.apple.camrea;
import com.sczg.apple.utils.PictureCleanUtil;

public class CameraNormalManager implements ICamera {

    private static CameraNormalManager cameraNormalManager;
    private static ICamera iCamera;

    private CameraNormalManager() {
    }


    public static CameraNormalManager getInstance() {
        if (cameraNormalManager == null) {
            synchronized (CameraNormalManager.class) {
                if (cameraNormalManager == null) {
                    cameraNormalManager = new CameraNormalManager();
                    cameraNormalManager.setCamera(new HkIndustryCamera());
                }
            }
        }
        return cameraNormalManager;
    }

    public void setCamera(ICamera iCamera) {
        if (iCamera != null) {
            this.iCamera = iCamera;
        }
    }


    @Override
    public void initCamera() {
        iCamera.initCamera();
    }

    @Override
    public void connectCamera() {
        iCamera.connectCamera();
    }

    @Override
    public void captureExecute(String from) {
        //执行任务前检查内存，如果有必要则清理内存
        PictureCleanUtil.doCleanIfNecessary();
        iCamera.captureExecute(from);
    }

    @Override
    public void preview(PreviewListener previewListener) {
        iCamera.preview(previewListener);
    }


    @Override
    public void unInitCamera() {
        iCamera.unInitCamera();
    }

    @Override
    public boolean isCameraOpen() {
        return iCamera.isCameraOpen();
    }

    @Override
    public boolean isCameraConnect() {
        return iCamera.isCameraConnect();
    }

    @Override
    public void setOnCaptureListener(CaptureListener onCaptureListener) {
        iCamera.setOnCaptureListener(onCaptureListener);
    }

    public static void taskCapturePhoto(String from) {
        new Thread(() -> {
            CameraNormalManager cameraNormalManager = CameraNormalManager.getInstance();
            cameraNormalManager.initCamera();
            cameraNormalManager.connectCamera();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            cameraNormalManager.captureExecute(from);

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

}
