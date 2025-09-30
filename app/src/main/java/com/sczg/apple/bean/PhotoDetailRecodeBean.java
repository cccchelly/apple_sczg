package com.sczg.apple.bean;

import java.util.List;

public class PhotoDetailRecodeBean {
    @Override
    public String toString() {
        return "PhotoDetailRecodeBean{" +
                "record=" + record +
                '}';
    }

    private List<RecordBean> record;

    public List<RecordBean> getRecord() {
        return record;
    }

    public void setRecord(List<RecordBean> record) {
        this.record = record;
    }

    public static class RecordBean {
        @Override
        public String toString() {
            return "RecordBean{" +
                    "count=" + count +
                    ", createDate=" + createDate +
                    ", createId=" + createId +
                    ", id=" + id +
                    ", pestType='" + pestType + '\'' +
                    ", photoId=" + photoId +
                    ", position='" + position + '\'' +
                    '}';
        }

        /**
         * count : 0
         * createDate : 1521619955000
         * createId : -1
         * id : 1
         * pestType : string
         * photoId : 0
         * position : string
         */

        private int count;
        private long createDate;
        private int createId;
        private int id;
        private String pestType;
        private int photoId;
        private String position;

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

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

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getPestType() {
            return pestType;
        }

        public void setPestType(String pestType) {
            this.pestType = pestType;
        }

        public int getPhotoId() {
            return photoId;
        }

        public void setPhotoId(int photoId) {
            this.photoId = photoId;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }
    }
}
