package com.sczg.apple.http.network;

import android.util.Log;

import com.sczg.apple.AppContants;
import com.sczg.apple.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class Net {
    private static Net mNet;
    private static Retrofit mRetrofit;

    private Net() {
        OkHttpClient okHttpClient = provideOkHttpClient(new CustomInterceptor());
        mRetrofit = provideRetrofit(okHttpClient);
    }

    public static Net getInstance() {
        if (mNet == null) {
            synchronized (Net.class) {
                if (mNet == null) {
                    mNet = new Net();
                }
            }
        }
        return mNet;
    }

    public IApi create() {
        return mRetrofit.create(IApi.class);
    }

    private Retrofit provideRetrofit(OkHttpClient okHttpClient) {

        String finalUrl ;
        finalUrl = AppContants.API_BASE_URL;
        return new Retrofit.Builder()
                .baseUrl(finalUrl)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private OkHttpClient provideOkHttpClient(CustomInterceptor customInterceptor) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(AppContants.CONNECT_TIME_OUT, TimeUnit.SECONDS)
                .writeTimeout(AppContants.WRITE_TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(AppContants.READ_TIME_OUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor(customInterceptor)
                .addInterceptor((new HttpLoggingInterceptor()
                        .setLevel(BuildConfig.DEBUG || Log.isLoggable("OkHttp", Log.VERBOSE) ?
                                HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE)));
        return builder.build();
    }
}
