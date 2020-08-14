package com.mowdowndevelopments.blurb.network;

import android.content.Context;

import com.mowdowndevelopments.blurb.AppExecutors;
import com.squareup.moshi.Moshi;

import java.net.CookieHandler;
import java.util.concurrent.ExecutorService;

import okhttp3.Cache;
import okhttp3.Dispatcher;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class Singletons {

    private static final int CACHE_SIZE = 536870912;
    private static final String BASE_URL = "https://newsblur.com/";
    private static Retrofit retrofit = null;
    private static OkHttpClient okHttpClient = null;
    private static Moshi moshi = null;

    private Singletons(){}

    public static OkHttpClient getOkHttpClient() {
        if (okHttpClient == null){ //TODO Implement new CookieHandler
            Dispatcher dispatcher = new Dispatcher((ExecutorService) AppExecutors.getInstance().networkIO());
            okHttpClient = new  OkHttpClient.Builder()
                    .cookieJar(new JavaNetCookieJar(CookieHandler.getDefault()))
                    .dispatcher(dispatcher)
                    .build();
        }
        return okHttpClient;
    }

    public static OkHttpClient getOkHttpClient(Context c){
        return getOkHttpClient().newBuilder()
                .cache(new Cache(c.getCacheDir(), CACHE_SIZE))
                .build();
    }

    public static NewsBlurAPI getNewsBlurAPI(){
        return getNewsBlurAPI(BASE_URL);
    }

    public static NewsBlurAPI getNewsBlurAPI(Context c){
        return getNewsBlurAPI(BASE_URL, c);
    }

    public static NewsBlurAPI getNewsBlurAPI(String baseUrl) {
        if (retrofit == null){
            retrofit = new Retrofit.Builder()
                    .addConverterFactory(MoshiConverterFactory.create(getMoshi()))
                    .baseUrl(baseUrl)
                    .client(getOkHttpClient())
                    .build();
        }
        return retrofit.create(NewsBlurAPI.class);
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
            //TODO Add adapters for model classes?
            moshi = new Moshi.Builder()
                    .build();
        }
        return moshi;
    }
}
