package com.sczg.apple.bean;

public class DeviceMessagePostBean {
    private double lat;
    private double lng;
    private String deviceId;

    public DeviceMessagePostBean(double lat, double lng, String deviceId) {
        this.lat = lat;
        this.lng = lng;
        this.deviceId = deviceId;
    }
}
