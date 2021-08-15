package jp.co.android.exchangeratecalculator.application

import android.app.Application
import android.content.Context
import android.content.ContextWrapper

class MainApplication : Application() {

    init { instance = this }

    companion object {
        private var instance: MainApplication? = null

        fun applicationContext() : Context {
            return instance?.applicationContext ?: ContextWrapper(null)
        }
    }
}