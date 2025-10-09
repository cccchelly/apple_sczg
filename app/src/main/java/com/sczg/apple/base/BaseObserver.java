package com.sczg.apple.base;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.sczg.apple.App;
import com.sczg.apple.bean.TokenBean;
import com.sczg.apple.http.AppDataManager;
import com.sczg.apple.utils.AppMsgUtil;
import com.sczg.apple.utils.ShareUtil;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.MalformedJsonException;
import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public abstract class BaseObserver<T extends BaseResponse> implements Observer<T> {

    private IBaseView mIBaseView = null;

    public BaseObserver(IBaseView iBaseView) {
        mIBaseView = iBaseView;
    }

    public BaseObserver() {

    }

    @Override
    public void onSubscribe(@NonNull Disposable d) {
        if (null != mIBaseView) {
            mIBaseView.addDisposable(d);
        }
    }

    @Override
    public void onNext(@NonNull T response) {
        if (null != mIBaseView) {
            mIBaseView.showDataView();
        }
        Logger.d("onNext: %s", response);
        switch (response.getCode()) {
            case BaseResponse.RESULT_CODE_SUCCESS:
                onSuccess(response);
                break;
            case BaseResponse.RESULT_CODE_DEVICE_UNLOGIN:
                onDataFailure(response);
                Login();
                break;
            case BaseResponse.RESULT_CODE_ERROR:
                onDataFailure(response);
                break;
            case BaseResponse.RESULT_CODE_TOKEN_EXPIRED:
                break;
            default:
                onDataFailure(response);
        }
    }

    @Override
    public void onError(@NonNull Throwable e) {
        String errorMsg = (e != null) ? e.toString() : "Unknown error";
        Logger.e("onError: " + errorMsg);
        if (null != mIBaseView) {
            mIBaseView.dissmissLoadingView();
        }
        handleError(e, mIBaseView);
    }

    @Override
    public void onComplete() {
        if (null != mIBaseView) {
            mIBaseView.dissmissLoadingView();
        }
    }

    public abstract void onSuccess(T response);

    protected void onDataFailure(T response) {
        String msg = (response != null) ? response.getMsg() : null;
        String logMsg = (msg != null) ? msg : "null";
        Logger.w("request failure:" + logMsg);
        if (!TextUtils.isEmpty(msg)) {
            Toast.makeText(App.getAppContext(), msg, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(App.getAppContext(), "未知错误", Toast.LENGTH_SHORT).show();
        }
    }

    protected void Login() {
    }

    public static void handleError(Throwable throwable, IBaseView iBaseView) {
        if (throwable == null) {
            Toast.makeText(App.getAppContext(), "未知错误", Toast.LENGTH_SHORT).show();
            return;
        }

        if (throwable instanceof SocketTimeoutException
                || throwable instanceof ConnectException
                || throwable instanceof UnknownHostException
                || throwable instanceof IOException) {
            Toast.makeText(App.getAppContext(), "网络异常", Toast.LENGTH_SHORT).show();
            if (null != iBaseView) {
                iBaseView.showErrorView();
            }
        } else if ((throwable instanceof JsonSyntaxException) || (throwable instanceof
                NumberFormatException) || (throwable instanceof MalformedJsonException)) {
            Toast.makeText(App.getAppContext(), "数据解析异常", Toast.LENGTH_SHORT).show();
        } else if ((throwable instanceof HttpException)) {
            Toast.makeText(App.getAppContext(), "服务器错误" + ((HttpException) throwable).code(), Toast.LENGTH_SHORT).show();
        } else if (throwable instanceof NullPointerException) {
            Toast.makeText(App.getAppContext(), "客户端异常" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(App.getAppContext(), "未知错误", Toast.LENGTH_SHORT).show();
        }
    }
}
