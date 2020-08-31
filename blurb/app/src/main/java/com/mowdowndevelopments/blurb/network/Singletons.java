package com.mowdowndevelopments.blurb.network;

import android.content.Context;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.mowdowndevelopments.blurb.AppExecutors;
import com.squareup.moshi.Moshi;

import java.util.concurrent.ExecutorService;

import okhttp3.Cache;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class Singletons {

    private static final int CACHE_SIZE = 536870912;
    public static final String BASE_URL = "https://newsblur.com/";
    private static Retrofit retrofit = null;
    private static OkHttpClient okHttpClient = null;
    private static Moshi moshi = null;

    private Singletons(){}

    public static OkHttpClient getOkHttpClient(Context c){
        if (okHttpClient == null){
            Dispatcher dispatcher = new Dispatcher((ExecutorService) AppExecutors.getInstance().networkIO());
            PersistentCookieJar cookieJar = new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(c));
            okHttpClient = new  OkHttpClient.Builder()
                    .cookieJar(cookieJar)
                    .cache(new Cache(c.getCacheDir(), CACHE_SIZE))
                    .dispatcher(dispatcher)
                    .build();
        }
        return okHttpClient;
    }

    public static NewsBlurAPI getNewsBlurAPI(Context c){
        return getNewsBlurAPI(BASE_URL, c);
    }

    public static NewsBlurAPI getNewsBlurAPI(String baseUrl, Context c) {
        if (retrofit == null){
            retrofit = new Retrofit.Builder()
                    .addConverterFactory(MoshiConverterFactory.create(getMoshi()))
                    .baseUrl(baseUrl)
                    .client(getOkHttpClient(c))
                    .build();
        }
        return retrofit.create(NewsBlurAPI.class);
    }

    public static Moshi getMoshi() {
        if (moshi == null){
            moshi = new Moshi.Builder()
                    .build();
        }
        return moshi;
    }
}
