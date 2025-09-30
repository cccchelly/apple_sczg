package com.sczg.apple.bean;

import com.sczg.apple.utils.TimeUtils;

import java.util.List;

public class TaskTimeBean {
    public TaskTimeBean(List<String> times, String deviceId) {
        this.times = times;
        this.deviceId = deviceId;
    }

    private List<String> times;
    private String deviceId;


    public List<String> getTimes() {
        return times;
    }

    public void setTimes(List<String> times) {
        this.times = times;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
