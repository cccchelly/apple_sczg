package com.sczg.apple.utils.eventbus;

public class SerialBackMessage {
    public SerialBackMessage() {
    }

    public SerialBackMessage(String batvol, String sunvol, String sta, String error) {
        this.batvol = batvol;
        this.sunvol = sunvol;
        this.sta = sta;
        this.error = error;
    }

    private String batvol;
    private String sunvol;
    private String sta;
    private String error;

    public String getBatvol() {
        return batvol;
    }

    public void setBatvol(String batvol) {
        this.batvol = batvol;
    }

    public String getSunvol() {
        return sunvol;
    }

    public void setSunvol(String sunvol) {
        this.sunvol = sunvol;
    }

    public String getSta() {
        return sta;
    }

    public void setSta(String sta) {
        this.sta = sta;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "SerialBackMessage{" +
                "batvol='" + batvol + '\'' +
                ", sunvol='" + sunvol + '\'' +
                ", sta='" + sta + '\'' +
                ", error='" + error + '\'' +
                '}';
    }
}
