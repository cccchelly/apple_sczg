package com.sczg.apple;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;

import androidx.multidex.MultiDexApplication;

import com.facebook.imagepipeline.request.ImageRequest;
import com.sczg.apple.utils.MyLifecycleHandler;
import com.alibaba.android.arouter.launcher.ARouter;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

import org.litepal.LitePal;

public class App extends MultiDexApplication {

    private static Context mAppContext;
    private static Activity mainActivity;

    public static Context getAppContext() {
        return mAppContext;
    }

    public static void setMainActivity(Activity activity) {
        mainActivity = activity;
    }

    public static Activity getMainActivity() {
        return mainActivity;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAppContext = getApplicationContext();

        initLogger();
        initARouter();
        initFresco();
        LitePal.initialize(mAppContext);

        registerActivityLifecycleCallbacks(new MyLifecycleHandler());
    }

    private void initLogger() {
        Logger.addLogAdapter(new AndroidLogAdapter(PrettyFormatStrategy
                .newBuilder()
                .tag(AppContants.APP_TAG)
                .build()
        ) {
            @Override
            public boolean isLoggable(int priority, String tag) {
                return BuildConfig.DEBUG;
            }
        });
    }

    private void initARouter() {
        if (BuildConfig.DEBUG) {
            ARouter.openLog();
            ARouter.openDebug();
        }

        ARouter.init(this);
    }

    private void initFresco() {
        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(getApplicationContext())
                .setDownsampleEnabled(true)
                .setResizeAndRotateEnabledForNetwork(true)
                .setBitmapsConfig(Bitmap.Config.RGB_565)
                .build();
        Fresco.initialize(this, config);
    }
}
