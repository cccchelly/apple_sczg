package com.sczg.apple.bean;

import java.util.List;

public class PicListBean {
    @Override
    public String toString() {
        return "PicListBean{" +
                "record=" + record +
                ", list=" + list +
                '}';
    }

    private List<?> record;
    private List<ListBean> list;

    public List<?> getRecord() {
        return record;
    }

    public void setRecord(List<?> record) {
        this.record = record;
    }

    public List<ListBean> getList() {
        return list;
    }

    public void setList(List<ListBean> list) {
        this.list = list;
    }

    public static class ListBean {
        @Override
        public String toString() {
            return "ListBean{" +
                    "createDate=" + createDate +
                    ", createId=" + createId +
                    ", deviceId=" + deviceId +
                    ", id=" + id +
                    ", mode=" + mode +
                    ", name='" + name + '\'' +
                    ", photoDate=" + photoDate +
                    ", url='" + url + '\'' +
                    ", weather=" + weather +
                    '}';
        }

        /**
         * createDate : 1521022097000
         * createId : -1
         * deviceId : 1111111111
         * id : 5
         * mode : 0
         * name : 2018-03-14 18:08:14null
         * photoDate : 1520956800000
         * url : WechatIMG691.png
         * weather : 0
         */

        private long createDate;
        private int createId;
        private int deviceId;
        private int id;
        private int mode;
        private String name;
        private long photoDate;
        private String url;
        private int weather;

        public long getCreateDate() {
            return createDate;
        }

        public void setCreateDate(long createDate) {
            this.createDate = createDate;
        }

        public int getCreateId() {
            return createId;
        }

        public void setCreateId(int createId) {
            this.createId = createId;
        }

        public int getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(int deviceId) {
            this.deviceId = deviceId;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getMode() {
            return mode;
        }

        public void setMode(int mode) {
            this.mode = mode;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getPhotoDate() {
            return photoDate;
        }

        public void setPhotoDate(long photoDate) {
            this.photoDate = photoDate;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getWeather() {
            return weather;
        }

        public void setWeather(int weather) {
            this.weather = weather;
        }
    }
}
