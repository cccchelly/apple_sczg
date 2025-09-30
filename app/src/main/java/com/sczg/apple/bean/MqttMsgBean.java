package com.sczg.apple.bean;

import java.util.List;

public class MqttMsgBean {
    /**
     * {"times": ["08:00", "12:00"]}
     */

    private List<String> times;


    public List<String> getTimes() {
        return times;
    }

    public void setTimes(List<String> times) {
        this.times = times;
    }
}
