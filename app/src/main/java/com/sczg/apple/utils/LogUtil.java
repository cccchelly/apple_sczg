package com.sczg.apple.utils;

import android.util.Log;

public class LogUtil {
    private static boolean LOGV = true;
    private static boolean LOGD = true;
    private static boolean LOGI = true;
    private static boolean LOGW = true;
    private static boolean LOGE = true;

    public static void v(String mess) {
        if (LOGV) { Log.v(getTag(), mess); }
    }
    public static void d(String mess) {
        if (LOGD) { Log.d(getTag(), mess); }
    }
    public static void i(String mess) {
        if (LOGI) { Log.i(getTag(), mess); }
    }
    public static void w(String mess) {
        if (LOGW) { Log.w(getTag(), mess); }
    }
    public static void e(String mess) {
        if (LOGE) { Log.e(getTag(), mess); }
    }

    private static String getTag() {
        StackTraceElement[] trace = new Throwable().fillInStackTrace()
                .getStackTrace();
        String callingClass = "";
        for (int i = 2; i < trace.length; i++) {
            Class<?> clazz = trace[i].getClass();
            if (!clazz.equals(LogUtil.class)) {
                callingClass = trace[i].getClassName();
                callingClass = callingClass.substring(callingClass
                        .lastIndexOf('.') + 1);
                break;
            }
        }
        return callingClass;
    }

}
