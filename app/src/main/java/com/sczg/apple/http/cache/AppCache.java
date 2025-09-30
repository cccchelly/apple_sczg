package com.sczg.apple.http.cache;

import com.sczg.apple.App;

public class AppCache implements IAcache{

    private static AppCache mAppCache;
    private final ACache mACache;

    public static AppCache getInstence() {
        if (mAppCache == null) {
            synchronized (AppCache.class) {
                if (mAppCache == null) {
                    mAppCache = new AppCache();
                }
            }
        }
        return mAppCache;
    }

    private AppCache() {
        mACache = ACache.get(App.getAppContext());
    }
}
