package jp.co.android.exchangeratecalculator.repository

import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import jp.co.android.exchangeratecalculator.application.MainApplication
import jp.co.android.exchangeratecalculator.domain.ExchangeRateLocalRepository
import jp.co.android.exchangeratecalculator.domain.ExchangeRate
import java.lang.reflect.Type

class ExchangeRateLocalRepositoryImpl(
    private val prefs: SharedPreferences? = PreferenceManager
        .getDefaultSharedPreferences(MainApplication.applicationContext()),
    private val gson: Gson = Gson()
) : ExchangeRateLocalRepository {

    companion object {
        const val PREFS_EXCHANGE_RATES_KEY = "exchange_rates"
    }

    override fun loadUsdToOthersExchangeRateListFromLocal(): ExchangeRate {
        val json: String? = prefs?.getString(PREFS_EXCHANGE_RATES_KEY, null)
        val type: Type = object : TypeToken<ArrayList<Pair<String, String>>>() {}.type
        val loadedArrayList =
            gson.fromJson(json, type) as ArrayList<Pair<String, String>>
        return ExchangeRate(loadedArrayList.toList())
    }

    override fun saveUsdToOthersExchangeRateListToLocal(exchangeRates: ExchangeRate) {
        val editor = prefs?.edit()
        val json: String = gson.toJson(exchangeRates.liveList.toTypedArray())
        editor?.putString(PREFS_EXCHANGE_RATES_KEY, json)
        editor?.apply()
    }
}