package jp.co.android.exchangeratecalculator.repository

import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

/**
 * RxJavaに対応したテスト用RetrofitService生成
 */
fun <T> createRetrofitService(webServer: MockWebServer, service: Class<T>): T =
    Retrofit.Builder()
        .baseUrl(webServer.url("/").toString())
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .client(OkHttpClient())
        .build()
        .create(service)