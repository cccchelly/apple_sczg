package com.sczg.apple.utils;

import android.os.Environment;

import com.sczg.apple.bean.PicturePathBean;

import org.litepal.crud.DataSupport;

import java.util.List;

public class PictureCleanUtil {

    public static void doCleanIfNecessary() {
        cleanLog();
        if (!isNeedClean())
            return;

        List<PicturePathBean> picPaths = DataSupport.findAll(PicturePathBean.class);
        try {
            for (int i = 0; i < 2; i++) {
                DataSupport.deleteAll(PicturePathBean.class, "path = ?", picPaths.get(i).getPath());
                FileUtils.deleteFile(FileUtils.getFileFromSdcard(picPaths.get(i).getPath()).getAbsolutePath());
            }
        } catch (Exception e) {

        }

    }

    private static void cleanLog() {
        DeleteUtil.delete(Environment.getExternalStorageDirectory().getAbsolutePath(), false, ".log");
    }

    private static boolean isNeedClean() {
        boolean isNeed = false;
        long total = MemoryUtil.getRomTotalSize();
        long ava = MemoryUtil.getRomAvailableSize();
        LogUtil.i(String.valueOf(total / ava));
        if (total / ava >= 10) {
            isNeed = true;
        }
        return isNeed;
    }
}
