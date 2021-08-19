package jp.co.android.exchangeratecalculator.repository

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Single
import jp.co.android.exchangeratecalculator.application.MainApplication
import jp.co.android.exchangeratecalculator.domain.ExchangeRateLocalRepository
import jp.co.android.exchangeratecalculator.domain.ExchangeRate
import java.lang.reflect.Type

/**
 * 為替レートリモートレポジトリ
 */
class ExchangeRateLocalRepositoryImpl(
    private val prefs: SharedPreferences? = PreferenceManager
        .getDefaultSharedPreferences(MainApplication.applicationContext()),
    private val gson: Gson = Gson()
) : ExchangeRateLocalRepository {

    companion object {
        const val PREFS_EXCHANGE_RATES_KEY = "exchange_rates"
    }

    override fun load(): Single<ExchangeRate> {
        val json: String? = prefs?.getString(PREFS_EXCHANGE_RATES_KEY, null)
        val type: Type = object : TypeToken<ArrayList<Pair<String, String>>>() {}.type
        val loadedArrayList =
            gson.fromJson(json, type) as ArrayList<Pair<String, String>>
        return Single.just(ExchangeRate(loadedArrayList.toList()))
    }

    override fun save(exchangeRates: ExchangeRate) {
        val editor = prefs?.edit()
        val json: String = gson.toJson(exchangeRates.liveList.toTypedArray())
        editor?.putString(PREFS_EXCHANGE_RATES_KEY, json)
        editor?.apply()
    }
}