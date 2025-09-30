package com.sczg.apple.utils;

import android.Manifest;

import java.io.DataOutputStream;

public class PermissionUtil {

    //读写权限
    public static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    //请求状态码
    public static int REQUEST_PERMISSION_CODE = 10000;

    // 设置usb权限
    static public void SetUSBPermission() {
        String[] commands = new String[]{"chmod -R 777 /dev/bus/usb"};
        Process process = null;
        DataOutputStream dataOutputStream = null;
        try {

            process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            int length = commands.length;
            for (int i = 0; i < length; i++) {
                dataOutputStream.writeBytes(commands[i] + "\n");
            }
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            process.waitFor();
        } catch (Exception e) {

        } finally {
            try {
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
    }


    static public void SetUSBMemory() {
        String[] commands = new String[]{"echo 500 > /sys/module/usbcore/parameters/usbfs_memory_mb"};
        Process process = null;
        DataOutputStream dataOutputStream = null;
        try {

            process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            int length = commands.length;
            for (int i = 0; i < length; i++) {
                dataOutputStream.writeBytes(commands[i] + "\n");
            }
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            process.waitFor();
        } catch (Exception e) {

        } finally {
            try {
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
    }
}
