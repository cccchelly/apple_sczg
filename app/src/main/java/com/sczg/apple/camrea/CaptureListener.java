package com.sczg.apple.camrea;

import android.graphics.Bitmap;

import java.io.File;

public interface CaptureListener {
    /**
     * 抓图完成回调
     * @param bitmap 抓取的图片
     * @param file 保存的文件
     * @param name 文件名
     */
    void onCaptureFinish(Bitmap bitmap, File file, String name);
    
    /**
     * 抓图开始回调
     * @param totalDelayMs 总延迟时间（毫秒）
     */
    void onCaptureStart(long totalDelayMs);
    
    /**
     * 抓图进度回调
     * @param remainingMs 剩余时间（毫秒）
     * @param totalDelayMs 总延迟时间（毫秒）
     */
    void onCaptureProgress(long remainingMs, long totalDelayMs);
    
    /**
     * 抓图处理中回调
     * 倒计时完毕后，正在保存图片和处理数据
     */
    void onCaptureProcessing();
}
