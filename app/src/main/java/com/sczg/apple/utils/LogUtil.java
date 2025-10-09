package com.sczg.apple.utils;

import android.util.Log;

public class LogUtil {
    private static boolean LOGV = true;
    private static boolean LOGD = true;
    private static boolean LOGI = true;
    private static boolean LOGW = true;
    private static boolean LOGE = true;

    public static void v(String mess) {
        if (LOGV) { 
            String message = (mess != null) ? mess : "";
            Log.v(getTag(), message); 
        }
    }
    public static void d(String mess) {
        if (LOGD) { 
            String message = (mess != null) ? mess : "";
            Log.d(getTag(), message); 
        }
    }
    public static void i(String mess) {
        if (LOGI) { 
            String message = (mess != null) ? mess : "";
            Log.i(getTag(), message); 
        }
    }
    public static void w(String mess) {
        if (LOGW) { 
            String message = (mess != null) ? mess : "";
            Log.w(getTag(), message); 
        }
    }
    public static void e(String mess) {
        if (LOGE) { 
            String message = (mess != null) ? mess : "";
            Log.e(getTag(), message); 
        }
    }

    private static String getTag() {
        try {
            StackTraceElement[] trace = new Throwable().fillInStackTrace()
                    .getStackTrace();
            if (trace == null || trace.length <= 2) {
                return "LogUtil";
            }
            
            String callingClass = "LogUtil";
            for (int i = 2; i < trace.length; i++) {
                StackTraceElement element = trace[i];
                if (element != null) {
                    String className = element.getClassName();
                    if (className != null && !className.equals(LogUtil.class.getName())) {
                        callingClass = className;
                        if (callingClass.contains(".")) {
                            callingClass = callingClass.substring(callingClass
                                    .lastIndexOf('.') + 1);
                        }
                        break;
                    }
                }
            }
            return (callingClass != null && !callingClass.isEmpty()) ? callingClass : "LogUtil";
        } catch (Exception e) {
            return "LogUtil";
        }
    }

}
