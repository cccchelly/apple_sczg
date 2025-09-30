package com.sczg.apple.camrea;

import android.graphics.Bitmap;

import java.io.File;

public interface CaptureListener {
    void onCaptureFinish(Bitmap bitmap, File file, String name);
}
