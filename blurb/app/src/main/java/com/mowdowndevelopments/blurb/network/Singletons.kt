package com.mowdowndevelopments.blurb.network

import android.content.Context
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.mowdowndevelopments.blurb.AppExecutors
import com.squareup.moshi.Moshi
import okhttp3.Cache
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.ExecutorService

object Singletons {
    private const val CACHE_SIZE = 536870912
    const val BASE_URL = "https://newsblur.com/"
    private lateinit var retrofit: Retrofit
    private lateinit var okHttpClient: OkHttpClient

    @JvmStatic
    val moshi: Moshi by lazy {
        Moshi.Builder().run {
            //add(KotlinJsonAdapterFactory())
            build()
        }
    }

    @JvmStatic
    fun getOkHttpClient(c: Context): OkHttpClient {
        if (!::okHttpClient.isInitialized) {
            val dispatcher = Dispatcher((AppExecutors.getInstance().networkIO() as ExecutorService))
            val cookieJar = PersistentCookieJar(SetCookieCache(),
                    SharedPrefsCookiePersistor(c.applicationContext))
            okHttpClient = OkHttpClient.Builder().run {
                cookieJar(cookieJar)
                cache(Cache(c.cacheDir, CACHE_SIZE.toLong()))
                dispatcher(dispatcher)
                build()
            }
        }
        return okHttpClient
    }

    @JvmStatic
    @JvmOverloads
    fun getNewsBlurAPI(c: Context, baseUrl: String = BASE_URL): NewsBlurAPI {
        if (!::retrofit.isInitialized) {
            retrofit = Retrofit.Builder().run {
                addConverterFactory(MoshiConverterFactory.create(moshi))
                baseUrl(baseUrl)
                client(getOkHttpClient(c))
                build()
            }
        }
        return retrofit.create(NewsBlurAPI::class.java)
    }
}