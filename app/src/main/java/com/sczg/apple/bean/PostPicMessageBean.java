package com.sczg.apple.bean;

public class PostPicMessageBean {

    public PostPicMessageBean(String deviceId, String time) {
        this.deviceId = deviceId;
        this.time = time;
    }

    private String deviceId;
    private String time;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
