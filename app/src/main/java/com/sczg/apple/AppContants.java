package com.sczg.apple;

import java.text.SimpleDateFormat;

public interface AppContants {
    String API_BASE_URL = "https://jima.jimaiot.com/api/";
    // String API_BASE_URL = "http://e6245b98.natappfree.cc/api/";
    String MQTT_BASE_URL = "tcp://mqtt.jimaiot.com:1883";   //mqtt

    SimpleDateFormat taskTimeFormat = new SimpleDateFormat("HH:mm");

    int CONNECT_TIME_OUT = 15;
    int WRITE_TIME_OUT = 15;
    int READ_TIME_OUT = 15;
    String APP_TAG = "Apple";
    String HOME_TAB_INDEX = "home_tab_index";
    String TASK_DEFAULT_TIME = "12:00"; //定时任务默认时间
    String SHOW_PIC_URL_KEY = "SHOW_PIC_URL_KEY";

    interface CaptureSource {
        String CAPTURE_SOURCE_TASK = "task";
        String CAPTURE_SOURCE_HAND = "hand";
    }

    interface ARouterUrl {
        String SPLASH_ACTIVITY = "/foundation/splash";
        String MAIN_ACTIVITY = "/foundation/main";
        String PreviewActivity = "/foundation/PreviewActivity";
        String ShowCaptureActivity = "/foundation/ShowCaptureActivity";
    }

}
