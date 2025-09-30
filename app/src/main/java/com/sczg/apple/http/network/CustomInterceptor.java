package com.sczg.apple.http.network;

import com.sczg.apple.utils.ShareUtil;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class CustomInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        //添加请求参数
        HttpUrl url = original.url().newBuilder()
                //                .addQueryParameter("count", "5")
                //                .addQueryParameter("start", "0")
                .build();

        //添加请求头
        Request request = original.newBuilder()
      //                          .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
    //                            .addHeader("Connection", "keep-alive")
//                .addHeader("token", mIPreference.getToken())
                //                .addHeader("token","7ee9163da415eb7a5a3f6c7743914fbc")
//                .removeHeader("User-Agent")
//                .addHeader("User-Agent", CommonUtils.getUserAgent())
               // .method(original.method(), original.body())
               // .addHeader("Cookie", "token="+ ShareUtil.getToken())
                .url(url)
                .build();

            return chain.proceed(request);

    }
}
