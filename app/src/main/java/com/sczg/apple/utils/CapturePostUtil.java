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
        postCompress(file, picName);
    }

    private static String TAG = CapturePostUtil.class.getName();

    static void postCompress(File data, final String name) {
        final Bitmap image = BitmapFactory.decodeFile(data.getAbsolutePath());

        new Thread(() -> {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
            int options = 100;
            //while (baos.toByteArray().length / 1024 > 100) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset(); // 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
            //options -= 10;// 每次都减少10
            //}
            ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
            Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
            saveCompressImg(name, bitmap, getNewPicName(name), App.getAppContext());
        }).start();
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


    /// 保存
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
