package com.sczg.apple.utils;

import android.text.TextUtils;
import android.view.Gravity;
import android.widget.Toast;

import com.sczg.apple.App;

public class ToastUtils {

    private static String mPreMessage;

    private static long mTime;

    private final static long DELAY_TIME = 2000;


    public static void showToast(String message) {

        if (TextUtils.equals(message, mPreMessage)) {
            if (System.currentTimeMillis() - mTime > DELAY_TIME) {
                Toast.makeText(App.getAppContext(), message, Toast.LENGTH_SHORT).show();
                mTime = System.currentTimeMillis();
            }
        } else {
            Toast.makeText(App.getAppContext(), message, Toast.LENGTH_SHORT).show();
            mTime = System.currentTimeMillis();
        }

        mPreMessage = message;
    }

    public static void showCenterToast(String message) {
        if (TextUtils.equals(message, mPreMessage)) {
            if (System.currentTimeMillis() - mTime > DELAY_TIME) {
                Toast toast = Toast.makeText(App.getAppContext(), message, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                mTime = System.currentTimeMillis();

            }
        } else {
            Toast toast = Toast.makeText(App.getAppContext(), message, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            mTime = System.currentTimeMillis();
        }

        mPreMessage = message;
    }
}
