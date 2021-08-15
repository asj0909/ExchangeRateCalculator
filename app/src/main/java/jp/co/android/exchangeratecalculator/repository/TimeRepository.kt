package jp.co.android.exchangeratecalculator.repository

import android.content.SharedPreferences
import android.preference.PreferenceManager
import jp.co.android.exchangeratecalculator.application.MainApplication

class TimeRepository(
    private val prefs: SharedPreferences? = PreferenceManager
        .getDefaultSharedPreferences(MainApplication.applicationContext())
) {

    companion object {
        const val PREFS_TIME_KEY = "time"
    }

    fun loadRemoteDataSavedTime(): Long = prefs?.getLong(PREFS_TIME_KEY, 0) ?: 0

    fun saveCurrentTimeToLocal() {
            val editor = prefs?.edit()
            val currentTime = System.currentTimeMillis()
            editor?.putLong(PREFS_TIME_KEY, currentTime)
            editor?.apply()
    }
}