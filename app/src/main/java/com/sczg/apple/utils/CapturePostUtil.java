package com.sczg.apple.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.sczg.apple.App;
import com.sczg.apple.base.BaseResponseObserver;
import com.sczg.apple.bean.PicturePathBean;
import com.sczg.apple.bean.PostPicMessageBean;
import com.sczg.apple.http.AppDataManager;

import org.litepal.crud.DataSupport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class CapturePostUtil {

    public static void findLocalPic() {
        List<PicturePathBean> picPaths = DataSupport.findAll(PicturePathBean.class);
        Log.i(TAG, "图片数量 = " + picPaths.size());
        if (picPaths.size() > 0) {
            String path = picPaths.get(0).getPath();
            try {
                File file = FileUtils.getFileFromSdcard(path);
                normalPost(file, path);
            } catch (NullPointerException e) {
                // 未找到图片，从数据库清除图片地址
                DataSupport.deleteAll(PicturePathBean.class, "path = ?", path);
                findLocalPic();
            }
        }
    }

    public static void normalPost(final File file, final String picName) {
        // 直接上传原图，不进行压缩
        LogUtil.d("直接上传原图，跳过压缩: " + picName);
        uploadCompressedFile(file, picName);
        
        // 注释掉压缩逻辑，保留以备后用
        // postCompressWithLossless(file, picName);
    }

    private static String TAG = CapturePostUtil.class.getName();

    /**
     * 使用无损压缩进行图片处理和上传
     * 注释掉以备后用 - 当前直接上传原图不压缩
     * @param data 原始图片文件
     * @param name 图片名称
     */
    /*
    static void postCompressWithLossless(File data, final String name) {
        LogUtil.d("开始无损压缩图片: " + name);
        
        // 使用新的无损压缩工具类
        LosslessImageCompressUtil.compressImage(App.getAppContext(), data, 
            new LosslessImageCompressUtil.CompressCallback() {
                @Override
                public void onSuccess(File compressedFile, long originalSize, long compressedSize) {
                    // 计算压缩率
                    double compressionRatio = LosslessImageCompressUtil.getCompressionRatio(originalSize, compressedSize);
                    
                    LogUtil.d("图片压缩成功: " + name);
                    LogUtil.d("原始大小: " + LosslessImageCompressUtil.formatFileSize(originalSize));
                    LogUtil.d("压缩后大小: " + LosslessImageCompressUtil.formatFileSize(compressedSize));
                    LogUtil.d("压缩率: " + String.format("%.1f%%", compressionRatio));
                    
                    // 显示压缩结果给用户
                    String compressionInfo = String.format("图片压缩完成\n原始: %s\n压缩后: %s\n压缩率: %.1f%%", 
                        LosslessImageCompressUtil.formatFileSize(originalSize),
                        LosslessImageCompressUtil.formatFileSize(compressedSize),
                        compressionRatio);
                    ToastUtils.showToast(compressionInfo);
                    
                    // 直接上传压缩后的文件
                    uploadCompressedFile(compressedFile, name);
                }
                
                @Override
                public void onError(Throwable error) {
                    LogUtil.e("图片压缩失败: " + error.getMessage());
                    ToastUtils.showToast("图片压缩失败: " + error.getMessage());
                    
                    // 压缩失败时使用原文件上传
                    LogUtil.d("压缩失败，使用原文件上传");
                    uploadCompressedFile(data, name);
                }
            });
    }
    */

    /**
     * 上传文件（可以是压缩后的文件或原始文件）
     * @param file 要上传的文件
     * @param originalName 原始文件名
     */
    static void uploadCompressedFile(File file, String originalName) {
        try {
            LogUtil.d("开始上传图片: " + originalName);
            
            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

            PostPicMessageBean postPicMessageBean = new PostPicMessageBean(
                AppMsgUtil.getIMEI(App.getAppContext()), 
                originalName.split("\\.")[0]
            );
            
            AppDataManager.getInstance()
                    .uploadFile(filePart, postPicMessageBean.getDeviceId(), postPicMessageBean.getTime())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new BaseResponseObserver<ResponseBody>() {
                        @Override
                        public void onSuccess(ResponseBody response) {
                            LogUtil.d("图片上传成功: " + originalName);
                            ToastUtils.showToast("图片:" + originalName + "上传成功");
                            
                            try {
                                // 数据库删除文件名，删除文件
                                DataSupport.deleteAll(PicturePathBean.class, "path = ?", originalName);
                                
                                // 删除原始文件
                                File originalFile = FileUtils.getFileFromSdcard(originalName);
                                if (originalFile != null && originalFile.exists()) {
                                    FileUtils.deleteFile(originalFile.getAbsolutePath());
                                }
                                
                                // 删除上传的临时文件（如果不是原文件）
                                if (!file.getAbsolutePath().equals(originalFile.getAbsolutePath())) {
                                    FileUtils.deleteFile(file.getAbsolutePath());
                                }
                                
                                // 递归上传下一张图片
                                findLocalPic();
                                
                            } catch (Exception e) {
                                LogUtil.e("清理文件失败: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                        
                        @Override
                        public void onError(Throwable e) {
                            LogUtil.e("图片上传失败: " + e.getMessage());
                            ToastUtils.showToast("图片上传失败: " + e.getMessage());
                        }
                    });
                    
        } catch (Exception e) {
            LogUtil.e("准备上传文件时发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * @deprecated 已废弃的压缩方法，现在直接上传原图
     */
    @Deprecated
    static void postCompress(File data, final String name) {
        // 直接上传原图，不进行压缩
        LogUtil.d("postCompress: 直接上传原图，跳过压缩: " + name);
        uploadCompressedFile(data, name);
        
        // 注释掉压缩逻辑，保留以备后用
        // postCompressWithLossless(data, name);
    }

    private static String getNewPicName(String oldName) {
        String[] s = oldName.split("\\.");
        String name = s[0];
        StringBuffer sb = new StringBuffer();
        long mills = TimeUtils.string2Millis(name, new SimpleDateFormat("yyyyMMddHHmmss"));
        String newTime = TimeUtils.millis2String(mills + 1000, new SimpleDateFormat("yyyyMMddHHmmss"));
        sb.append(newTime);
        sb.append(".jpg");
        return sb.toString();
    }


    /**
     * @deprecated 已废弃的保存压缩图片方法，使用uploadCompressedFile替代
     * 保存压缩图片并上传
     */
    @Deprecated
    public static boolean saveCompressImg(final String oldName, Bitmap bitmap, final String newName, Context context) {
        try {
            String dir = Environment.getExternalStorageDirectory().getAbsolutePath();
            File file = new File(dir);
            if (!file.exists()) {
                file.mkdirs();
            }
            Log.i("saveImgUri", " = " + dir);
            final File mFile = new File(dir + "/" + newName);
            if (!mFile.exists()) {
                FileOutputStream outputStream = new FileOutputStream(mFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                Uri uri = Uri.fromFile(mFile);
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            }

            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), mFile);
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", mFile.getName(), requestFile);

            PostPicMessageBean postPicMessageBean = new PostPicMessageBean(AppMsgUtil.getIMEI(App.getAppContext()), oldName.split("\\.")[0]);
            AppDataManager.getInstance()
                    .uploadFile(filePart, postPicMessageBean.getDeviceId(),postPicMessageBean.getTime())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new BaseResponseObserver<ResponseBody>() {
                        @Override
                        public void onSuccess(ResponseBody response) {
                            ToastUtils.showToast("图片:" + oldName + "上传成功");
                            try {
                                Log.i("==fileName==", oldName);
                                //数据库删除文件名   删除文件
                                DataSupport.deleteAll(PicturePathBean.class, "path = ?", oldName);
                                FileUtils.deleteFile(FileUtils.getFileFromSdcard(oldName).getAbsolutePath());
                                if (mFile != null) {
                                    FileUtils.deleteFile(mFile);
                                }
                                findLocalPic();   //递归上传，每次上传第一张图片，完成后删除图片继续上传直到全部上传完毕。
                                //Log.i("==fileAbsName==",FileUtils.getFileFromSdcard(picName).getAbsolutePath());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
            return true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
}
