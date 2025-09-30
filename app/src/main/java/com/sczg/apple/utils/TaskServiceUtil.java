package com.sczg.apple.utils;

import android.content.Intent;

import com.sczg.apple.App;
import com.sczg.apple.service.CaptureService;
import com.sczg.apple.service.LocationService;
import com.sczg.apple.service.MqttService;
import com.sczg.apple.service.PostPictureService;

public class TaskServiceUtil {

    public static void startTasks() {
        Intent intent = new Intent(App.getAppContext(), CaptureService.class);
        App.getAppContext().startService(intent);
        Intent postIntent = new Intent(App.getAppContext(), PostPictureService.class);
        App.getAppContext().startService(postIntent);
        Intent mqttIntent = new Intent(App.getAppContext(), MqttService.class);
        App.getAppContext().startService(mqttIntent);
        Intent locationIntent = new Intent(App.getAppContext(), LocationService.class);
        App.getAppContext().startService(locationIntent);
    }


    public static void stopTasks() {
        Intent intent = new Intent(App.getAppContext(), CaptureService.class);
        App.getAppContext().stopService(intent);
        Intent postIntent = new Intent(App.getAppContext(), PostPictureService.class);
        App.getAppContext().stopService(postIntent);
        Intent mqttIntent = new Intent(App.getAppContext(), MqttService.class);
        App.getAppContext().stopService(mqttIntent);
        Intent locationIntent = new Intent(App.getAppContext(), LocationService.class);
        App.getAppContext().stopService(locationIntent);
    }

    public static void startPhotoTasks() {
        Intent intent = new Intent(App.getAppContext(), CaptureService.class);
        App.getAppContext().startService(intent);
        Intent postIntent = new Intent(App.getAppContext(), PostPictureService.class);
        App.getAppContext().startService(postIntent);
    }

    public static void stopPhotoTasks() {
        Intent intent = new Intent(App.getAppContext(), CaptureService.class);
        App.getAppContext().stopService(intent);
        Intent postIntent = new Intent(App.getAppContext(), PostPictureService.class);
        App.getAppContext().stopService(postIntent);
    }

    public static void resetPhotoTasks() {
        new Thread(() -> {
            try {
                stopPhotoTasks();
                Thread.sleep(10 * 1000);
                startPhotoTasks();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }


    public static void resetTasks() {
        new Thread(() -> {
            try {
                stopTasks();
                Thread.sleep(10 * 1000);
                startTasks();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

}
