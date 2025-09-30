package com.sczg.apple.bean;

import org.litepal.crud.DataSupport;

public class PicturePathBean extends DataSupport{
    String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
