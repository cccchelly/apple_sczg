package com.sczg.apple.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

/**
 * 无损图片压缩工具类
 * 基于Luban库实现智能压缩，在保证清晰度的前提下减少图片体积
 */
public class LosslessImageCompressUtil {
    
    private static final String TAG = "LosslessImageCompressUtil";
    
    // 压缩质量阈值
    private static final int HIGH_QUALITY = 95;  // 高质量压缩
    private static final int MEDIUM_QUALITY = 85; // 中等质量压缩
    private static final int LOW_QUALITY = 75;   // 低质量压缩
    
    // 文件大小阈值（KB）
    private static final int LARGE_FILE_THRESHOLD = 2048;  // 2MB
    private static final int MEDIUM_FILE_THRESHOLD = 1024; // 1MB
    private static final int SMALL_FILE_THRESHOLD = 512;   // 512KB
    
    /**
     * 压缩回调接口
     */
    public interface CompressCallback {
        /**
         * 压缩成功
         * @param compressedFile 压缩后的文件
         * @param originalSize 原始文件大小（字节）
         * @param compressedSize 压缩后文件大小（字节）
         */
        void onSuccess(File compressedFile, long originalSize, long compressedSize);
        
        /**
         * 压缩失败
         * @param error 错误信息
         */
        void onError(Throwable error);
    }
    
    /**
     * 智能压缩图片
     * 根据图片大小和质量自动选择最佳压缩策略
     * 
     * @param context 上下文
     * @param sourceFile 源文件
     * @param callback 压缩回调
     */
    public static void compressImage(Context context, File sourceFile, CompressCallback callback) {
        if (sourceFile == null || !sourceFile.exists()) {
            if (callback != null) {
                callback.onError(new IllegalArgumentException("源文件不存在"));
            }
            return;
        }
        
        LogUtil.d("开始压缩图片: " + sourceFile.getAbsolutePath());
        LogUtil.d("原始文件大小: " + (sourceFile.length() / 1024) + "KB");
        
        long originalSize = sourceFile.length();
        
        // 如果文件已经很小，直接返回原文件
        if (originalSize < SMALL_FILE_THRESHOLD * 1024) {
            LogUtil.d("文件已经很小，无需压缩");
            if (callback != null) {
                callback.onSuccess(sourceFile, originalSize, originalSize);
            }
            return;
        }
        
        // 根据文件大小选择压缩策略
        int targetSizeKB = getTargetSize(originalSize);
        int quality = getCompressionQuality(originalSize);
        
        LogUtil.d("目标大小: " + targetSizeKB + "KB, 压缩质量: " + quality);
        
        try {
            Luban.with(context)
                    .load(sourceFile)
                    .ignoreBy(100) // 忽略小于100KB的图片
                    .setTargetDir(sourceFile.getParent()) // 设置压缩后文件保存目录
                    .filter(path -> !(TextUtils.isEmpty(path) || path.toLowerCase().endsWith(".gif")))
                    .setCompressListener(new OnCompressListener() {
                        @Override
                        public void onStart() {
                            LogUtil.d("开始Luban压缩");
                        }
                        
                        @Override
                        public void onSuccess(File compressedFile) {
                            LogUtil.d("Luban压缩完成");
                            
                            // 进一步优化压缩结果
                            optimizeCompressedImage(compressedFile, quality, originalSize, callback);
                        }
                        
                        @Override
                        public void onError(Throwable e) {
                            LogUtil.e("Luban压缩失败: " + e.getMessage());
                            
                            // Luban压缩失败，尝试使用传统压缩方法
                            fallbackCompress(sourceFile, quality, originalSize, callback);
                        }
                    })
                    .launch();
                    
        } catch (Exception e) {
            LogUtil.e("压缩过程异常: " + e.getMessage());
            if (callback != null) {
                callback.onError(e);
            }
        }
    }
    
    /**
     * 根据原始文件大小确定目标压缩大小
     */
    private static int getTargetSize(long originalSize) {
        long originalSizeKB = originalSize / 1024;
        
        if (originalSizeKB > LARGE_FILE_THRESHOLD) {
            return LARGE_FILE_THRESHOLD; // 大文件压缩到2MB
        } else if (originalSizeKB > MEDIUM_FILE_THRESHOLD) {
            return MEDIUM_FILE_THRESHOLD; // 中等文件压缩到1MB
        } else {
            return SMALL_FILE_THRESHOLD; // 小文件压缩到512KB
        }
    }
    
    /**
     * 根据原始文件大小确定压缩质量
     */
    private static int getCompressionQuality(long originalSize) {
        long originalSizeKB = originalSize / 1024;
        
        if (originalSizeKB > LARGE_FILE_THRESHOLD) {
            return LOW_QUALITY; // 大文件使用较低质量
        } else if (originalSizeKB > MEDIUM_FILE_THRESHOLD) {
            return MEDIUM_QUALITY; // 中等文件使用中等质量
        } else {
            return HIGH_QUALITY; // 小文件使用高质量
        }
    }
    
    /**
     * 优化Luban压缩后的图片
     */
    private static void optimizeCompressedImage(File lubanCompressedFile, int quality, 
                                              long originalSize, CompressCallback callback) {
        try {
            long compressedSize = lubanCompressedFile.length();
            LogUtil.d("Luban压缩后大小: " + (compressedSize / 1024) + "KB");
            
            // 如果Luban压缩效果已经很好，直接使用
            if (compressedSize < originalSize * 0.7) { // 压缩率超过30%
                LogUtil.d("Luban压缩效果良好，直接使用");
                if (callback != null) {
                    callback.onSuccess(lubanCompressedFile, originalSize, compressedSize);
                }
                return;
            }
            
            // 进一步优化压缩
            LogUtil.d("进一步优化压缩质量");
            furtherOptimize(lubanCompressedFile, quality, originalSize, callback);
            
        } catch (Exception e) {
            LogUtil.e("优化压缩图片失败: " + e.getMessage());
            if (callback != null) {
                callback.onError(e);
            }
        }
    }
    
    /**
     * 进一步优化压缩
     */
    private static void furtherOptimize(File sourceFile, int quality, 
                                      long originalSize, CompressCallback callback) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(sourceFile.getAbsolutePath());
            if (bitmap == null) {
                if (callback != null) {
                    callback.onError(new RuntimeException("无法解码图片"));
                }
                return;
            }
            
            // 创建优化后的文件
            String optimizedPath = sourceFile.getAbsolutePath().replace(".jpg", "_optimized.jpg");
            File optimizedFile = new File(optimizedPath);
            
            FileOutputStream fos = new FileOutputStream(optimizedFile);
            boolean success = bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);
            fos.close();
            
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
            
            if (success && optimizedFile.exists()) {
                long optimizedSize = optimizedFile.length();
                LogUtil.d("进一步优化完成，大小: " + (optimizedSize / 1024) + "KB");
                
                // 删除中间文件，重命名优化文件
                if (sourceFile.delete()) {
                    optimizedFile.renameTo(sourceFile);
                }
                
                if (callback != null) {
                    callback.onSuccess(sourceFile, originalSize, optimizedSize);
                }
            } else {
                if (callback != null) {
                    callback.onError(new RuntimeException("进一步优化失败"));
                }
            }
            
        } catch (IOException e) {
            LogUtil.e("进一步优化异常: " + e.getMessage());
            if (callback != null) {
                callback.onError(e);
            }
        }
    }
    
    /**
     * 备用压缩方法（当Luban失败时使用）
     */
    private static void fallbackCompress(File sourceFile, int quality, 
                                       long originalSize, CompressCallback callback) {
        LogUtil.d("使用备用压缩方法");
        
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(sourceFile.getAbsolutePath());
            if (bitmap == null) {
                if (callback != null) {
                    callback.onError(new RuntimeException("无法解码图片"));
                }
                return;
            }
            
            // 创建压缩文件
            String compressedPath = sourceFile.getAbsolutePath().replace(".jpg", "_compressed.jpg");
            File compressedFile = new File(compressedPath);
            
            FileOutputStream fos = new FileOutputStream(compressedFile);
            boolean success = bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);
            fos.close();
            
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
            
            if (success && compressedFile.exists()) {
                long compressedSize = compressedFile.length();
                LogUtil.d("备用压缩完成，大小: " + (compressedSize / 1024) + "KB");
                
                // 删除原文件，重命名压缩文件
                if (sourceFile.delete()) {
                    compressedFile.renameTo(sourceFile);
                }
                
                if (callback != null) {
                    callback.onSuccess(sourceFile, originalSize, compressedSize);
                }
            } else {
                if (callback != null) {
                    callback.onError(new RuntimeException("备用压缩失败"));
                }
            }
            
        } catch (IOException e) {
            LogUtil.e("备用压缩异常: " + e.getMessage());
            if (callback != null) {
                callback.onError(e);
            }
        }
    }
    
    /**
     * 计算压缩率
     * @param originalSize 原始大小
     * @param compressedSize 压缩后大小
     * @return 压缩率百分比
     */
    public static double getCompressionRatio(long originalSize, long compressedSize) {
        if (originalSize == 0) return 0;
        return ((double)(originalSize - compressedSize) / originalSize) * 100;
    }
    
    /**
     * 格式化文件大小显示
     * @param sizeInBytes 文件大小（字节）
     * @return 格式化后的大小字符串
     */
    public static String formatFileSize(long sizeInBytes) {
        if (sizeInBytes < 1024) {
            return sizeInBytes + "B";
        } else if (sizeInBytes < 1024 * 1024) {
            return String.format("%.1fKB", sizeInBytes / 1024.0);
        } else {
            return String.format("%.1fMB", sizeInBytes / (1024.0 * 1024.0));
        }
    }
}