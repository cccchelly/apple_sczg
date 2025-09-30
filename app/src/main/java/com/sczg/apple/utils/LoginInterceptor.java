package com.sczg.apple.utils;

import android.content.Context;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Interceptor;
import com.alibaba.android.arouter.facade.callback.InterceptorCallback;
import com.alibaba.android.arouter.facade.template.IInterceptor;
import com.orhanobut.logger.Logger;

@Interceptor(priority = 7)
public class LoginInterceptor implements IInterceptor {
    @Override
    public void process(Postcard postcard, InterceptorCallback callback) {
        callback.onContinue(postcard);

        /*int extra = postcard.getExtra();
        Logger.d("====login extra: %d", extra);
        if (extra == 401) {
            // 未登录
            callback.onInterrupt(new RuntimeException("user not login!"));
        } else {
            callback.onContinue(postcard);
        }*/
    }

    @Override
    public void init(Context context) {

    }
}
