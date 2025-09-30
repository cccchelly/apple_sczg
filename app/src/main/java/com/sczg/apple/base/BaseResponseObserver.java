package com.sczg.apple.base;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.sczg.apple.App;
import com.sczg.apple.bean.TokenBean;
import com.sczg.apple.http.AppDataManager;
import com.sczg.apple.http.network.Net;
import com.sczg.apple.utils.AppMsgUtil;
import com.sczg.apple.utils.ShareUtil;
import com.sczg.apple.utils.ToastUtils;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.MalformedJsonException;
import com.orhanobut.logger.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

public abstract class BaseResponseObserver<T extends ResponseBody> implements Observer<T> {

    private IBaseView mIBaseView = null;

    public BaseResponseObserver(IBaseView iBaseView) {
        mIBaseView = iBaseView;
    }

    public BaseResponseObserver() {

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
        try {
            String responseStr = response.string();
            try {
                JSONObject jsonObject = new JSONObject(responseStr);
                int code = jsonObject.getInt("code");
                if (code == 200) {
                    onSuccess(response);
                } else {
                    ToastUtils.showToast("错误：errorCode=" + code);
                    if (code == BaseResponse.RESULT_CODE_DEVICE_UNLOGIN) {
                        Login();
                    } else if (code == BaseResponse.RESULT_CODE_ERROR) {
                        Login();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected void Login() {
    }

    @Override
    public void onError(@NonNull Throwable e) {

        //异常处理，需要自己实现
        Logger.e("onError: " + e.toString());
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

    /**
     * 对api返回的错误状态的处理 需要时自己实现
     *
     * @param response
     */
    protected void onDataFailure(T response) {
        String msg = null;
        try {
            msg = response.string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Logger.w("request data but get failure:" + msg);
        if (!TextUtils.isEmpty(msg)) {
            //            mBaseMvpView.showException(response.getMsg());
            Toast.makeText(App.getAppContext(), msg, Toast.LENGTH_SHORT).show();
        } else {
            //            mBaseMvpView.showException("未知错误");
            Toast.makeText(App.getAppContext(), "未知错误", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 按照通用规则解析和处理数据请求时发生的错误。这个方法在执行支付等非标准的REST请求时很有用。
     */
    public static void handleError(Throwable throwable, IBaseView iBaseView) {
        if (throwable == null) {
            Toast.makeText(App.getAppContext(), "未知错误", Toast.LENGTH_SHORT).show();
            return;
        }
        //分为以下几类问题：网络连接，数据解析，客户端出错【空指针等】，服务器内部错误
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
