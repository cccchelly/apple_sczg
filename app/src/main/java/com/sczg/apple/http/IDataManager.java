package com.sczg.apple.http;

import com.sczg.apple.http.cache.IAcache;
import com.sczg.apple.http.network.IApi;

public interface IDataManager extends IApi, IAcache{

    IApi getApi();

    IAcache getAcache();
}
