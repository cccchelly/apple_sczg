package com.sczg.apple.utils;

import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;

import com.sczg.apple.App;
import com.sczg.apple.ui.activity.MainActivity;

import java.io.File;

public class MemoryUtil {
    /**
     * 获得SD卡总大小
     *
     * @return
     */
    public static String getSDTotalSize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return Formatter.formatFileSize(App.getAppContext(), blockSize * totalBlocks);
    }

    /**
     * 获得sd卡剩余容量，即可用大小
     *
     * @return
     */
    public static  String getSDAvailableSize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return Formatter.formatFileSize(App.getAppContext(), blockSize * availableBlocks);
    }

    /**
     * 获得机身内容总大小
     *
     * @return
     */
    public static  long getRomTotalSize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        //return Formatter.formatFileSize(App.getAppContext(), blockSize * totalBlocks);
        return blockSize * totalBlocks;
    }

    /**
     * 获得机身可用内存
     *
     * @return
     */
    public static  long getRomAvailableSize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        //return Formatter.formatFileSize(App.getAppContext(), blockSize * availableBlocks);
        return blockSize * availableBlocks;
    }
}
