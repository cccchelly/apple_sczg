package com.sczg.apple.utils;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FilenameFilter;

public class DeleteRunnable  implements Runnable{
    private String mRegEx;
    private String dirPath;
    private boolean isPrefix;
    private static final String TAG = DeleteRunnable.class.getSimpleName();
    /**
     * Constructor
     *
     * @param dirPath  要删除文件所在的目录路径
     * @param isPrefix true为前缀 false为后缀
     * @param mRegEx   规则
     */
    public DeleteRunnable(String dirPath, boolean isPrefix, String mRegEx) {
        this.mRegEx = mRegEx;
        this.dirPath = dirPath;
        this.isPrefix = isPrefix;
    }
    @Override
    public void run() {
        enumAllFileList();
    }
    /**
     * 枚举并删除所有符合条件(前缀)的文件
     */
    private void enumAllFileList() {
        if (!TextUtils.isEmpty(dirPath)) {
            File adDir = new File(dirPath);
            if (adDir.exists() && adDir.isDirectory()) {
                if (!TextUtils.isEmpty(mRegEx)) {
                    DeleteFileFilter filter = new DeleteFileFilter(isPrefix, mRegEx);
                    // 2.匹配是否是需要删除的文件
                    File[] fileList = adDir.listFiles(filter);
                    if (fileList != null && fileList.length > 0) {
                        for (File file : fileList) {
                            if (file.isFile() && file.exists()) {
                                boolean delete = file.delete();
                                Log.i(TAG, "删除文件" + (delete ? "成功~" : "失败！"));
                            }
                        }
                    }
                }
            }
        }
    }

    class DeleteFileFilter implements FilenameFilter {
        private boolean isPrefix;
        private String mRegEx;// 前缀或后缀规则
        public DeleteFileFilter(boolean isPrefix, @NonNull String regEx) {
            this.isPrefix = isPrefix;
            this.mRegEx = regEx;
        }
        @Override
        public boolean accept(File file, String s) {
            return isPrefix ? s.startsWith(mRegEx) : s.endsWith(mRegEx);
        }
    }

}
