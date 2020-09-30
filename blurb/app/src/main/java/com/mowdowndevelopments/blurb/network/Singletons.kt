package com.mowdowndevelopments.blurb.network;

import android.content.Context;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.mowdowndevelopments.blurb.AppExecutors;
import com.squareup.moshi.Moshi;

import org.jetbrains.annotations.NotNull;

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
    private static BillingClient billingClient = null;

    private Singletons(){}

    @NotNull
    public static OkHttpClient getOkHttpClient(Context c){
        if (okHttpClient == null){
            Dispatcher dispatcher = new Dispatcher((ExecutorService) AppExecutors.getInstance().networkIO());
            PersistentCookieJar cookieJar = new PersistentCookieJar(new SetCookieCache(),
                    new SharedPrefsCookiePersistor(c.getApplicationContext()));
            okHttpClient = new  OkHttpClient.Builder()
                    .cookieJar(cookieJar)
                    .cache(new Cache(c.getCacheDir(), CACHE_SIZE))
                    .dispatcher(dispatcher)
                    .build();
        }
        return okHttpClient;
    }

    @NotNull
    public static NewsBlurAPI getNewsBlurAPI(Context c){
        return getNewsBlurAPI(c, BASE_URL);
    }

    @NotNull
    public static NewsBlurAPI getNewsBlurAPI(Context c, String baseUrl) {
        if (retrofit == null){
            retrofit = new Retrofit.Builder()
                    .addConverterFactory(MoshiConverterFactory.create(getMoshi()))
                    .baseUrl(baseUrl)
                    .client(getOkHttpClient(c))
                    .build();
        }
        return retrofit.create(NewsBlurAPI.class);
    }

    @NotNull
    public static Moshi getMoshi() {
        if (moshi == null){
            moshi = new Moshi.Builder()
                    .build();
        }
        return moshi;
    }

    @NotNull
    public static BillingClient getBillingClient(Context c) {
        if (billingClient == null){
            PurchasesUpdatedListener listener = (billingResult, list) -> {
                //TODO Fill out listener
            };
            billingClient = BillingClient.newBuilder(c.getApplicationContext())
                    .setListener(listener)
                    .enablePendingPurchases()
                    .build();
        }
        return billingClient;
    }
}
