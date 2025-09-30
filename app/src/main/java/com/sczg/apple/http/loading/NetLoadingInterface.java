package com.sczg.apple.http.loading;

public interface NetLoadingInterface {

    void showLoadingView();

    void showLoadingView(String msg);

    void dissLoadingView();

    void releaseView();
}
