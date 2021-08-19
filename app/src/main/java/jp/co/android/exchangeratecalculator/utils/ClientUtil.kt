package jp.co.android.exchangeratecalculator.utils

import jp.co.android.exchangeratecalculator.constants.ApiUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * WebAPIクライアント生成
 */
fun <T> createHttpClient(
    serviceClass: Class<T>,
    timeoutSec: Long = 10L
): T {

    val okHttpBuilder = OkHttpClient.Builder().apply {
        readTimeout(timeoutSec, TimeUnit.SECONDS)
        connectTimeout(timeoutSec, TimeUnit.SECONDS)
    }

    val okHttpClient = okHttpBuilder.build()

    return Retrofit.Builder()
        .baseUrl(ApiUrl.API_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .client(okHttpClient)
        .build()
        .create(serviceClass)
}