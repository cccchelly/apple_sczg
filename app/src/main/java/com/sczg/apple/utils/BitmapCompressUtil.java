package com.sczg.apple.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

/**
 * 图片压缩工具类
 * 用于解决大尺寸图片在显示时超出GPU纹理限制的问题
 */
public class BitmapCompressUtil {
    
    // GPU支持的最大纹理尺寸，通常为4096x4096
    private static final int MAX_TEXTURE_SIZE = 4096;
    
    /**
     * 压缩Bitmap到GPU支持的最大纹理尺寸内
     * @param originalBitmap 原始Bitmap
     * @return 压缩后的Bitmap，如果原始尺寸已在限制内则返回原始Bitmap
     */
    public static Bitmap compressBitmapForDisplay(Bitmap originalBitmap) {
        if (originalBitmap == null) {
            LogUtil.e("BitmapCompressUtil: 输入的Bitmap为空");
            return null;
        }
        
        int originalWidth = originalBitmap.getWidth();
        int originalHeight = originalBitmap.getHeight();
        
        LogUtil.i("BitmapCompressUtil: 原始图片尺寸 " + originalWidth + "x" + originalHeight);
        
        // 检查是否需要压缩
        if (originalWidth <= MAX_TEXTURE_SIZE && originalHeight <= MAX_TEXTURE_SIZE) {
            LogUtil.i("BitmapCompressUtil: 图片尺寸在GPU限制内，无需压缩");
            return originalBitmap;
        }
        
        // 计算压缩比例
        float scaleRatio = calculateScaleRatio(originalWidth, originalHeight);
        
        int newWidth = Math.round(originalWidth * scaleRatio);
        int newHeight = Math.round(originalHeight * scaleRatio);
        
        LogUtil.i("BitmapCompressUtil: 压缩比例 " + scaleRatio + ", 新尺寸 " + newWidth + "x" + newHeight);
        
        try {
            // 使用Matrix进行高质量缩放
            Matrix matrix = new Matrix();
            matrix.postScale(scaleRatio, scaleRatio);
            
            Bitmap compressedBitmap = Bitmap.createBitmap(
                originalBitmap, 0, 0, originalWidth, originalHeight, matrix, true);
            
            LogUtil.i("BitmapCompressUtil: 图片压缩成功");
            return compressedBitmap;
            
        } catch (OutOfMemoryError e) {
            LogUtil.e("BitmapCompressUtil: 内存不足，尝试使用Canvas方式压缩");
            // 如果Matrix方式内存不足，尝试使用Canvas方式
            return compressBitmapWithCanvas(originalBitmap, newWidth, newHeight);
        } catch (Exception e) {
            LogUtil.e("BitmapCompressUtil: 压缩失败 " + e.getMessage());
            e.printStackTrace();
            return originalBitmap;
        }
    }
    
    /**
     * 计算压缩比例
     * @param width 原始宽度
     * @param height 原始高度
     * @return 压缩比例
     */
    private static float calculateScaleRatio(int width, int height) {
        float widthRatio = (float) MAX_TEXTURE_SIZE / width;
        float heightRatio = (float) MAX_TEXTURE_SIZE / height;
        
        // 选择较小的比例，确保两个维度都在限制内
        return Math.min(widthRatio, heightRatio);
    }
    
    /**
     * 使用Canvas方式压缩图片（内存占用更少）
     * @param originalBitmap 原始Bitmap
     * @param newWidth 新宽度
     * @param newHeight 新高度
     * @return 压缩后的Bitmap
     */
    private static Bitmap compressBitmapWithCanvas(Bitmap originalBitmap, int newWidth, int newHeight) {
        try {
            Bitmap compressedBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(compressedBitmap);
            
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setFilterBitmap(true);
            
            // 计算缩放矩阵
            Matrix matrix = new Matrix();
            float scaleX = (float) newWidth / originalBitmap.getWidth();
            float scaleY = (float) newHeight / originalBitmap.getHeight();
            matrix.setScale(scaleX, scaleY);
            
            canvas.drawBitmap(originalBitmap, matrix, paint);
            
            LogUtil.i("BitmapCompressUtil: Canvas方式压缩成功");
            return compressedBitmap;
            
        } catch (Exception e) {
            LogUtil.e("BitmapCompressUtil: Canvas方式压缩也失败 " + e.getMessage());
            e.printStackTrace();
            return originalBitmap;
        }
    }
    
    /**
     * 检查Bitmap是否超出GPU纹理限制
     * @param bitmap 要检查的Bitmap
     * @return true表示超出限制，false表示在限制内
     */
    public static boolean isExceedTextureLimit(Bitmap bitmap) {
        if (bitmap == null) {
            return false;
        }
        return bitmap.getWidth() > MAX_TEXTURE_SIZE || bitmap.getHeight() > MAX_TEXTURE_SIZE;
    }
    
    /**
     * 获取GPU支持的最大纹理尺寸
     * @return 最大纹理尺寸
     */
    public static int getMaxTextureSize() {
        return MAX_TEXTURE_SIZE;
    }
}