package jp.co.android.exchangeratecalculator.domain

import jp.co.android.exchangeratecalculator.repository.TimeRepository

class TimeService(
    private val timeRepository: TimeRepository = TimeRepository()
) {
    companion object {
        const val THIRTY_MINUTES_TO_MILLIS = 1000 * 60 * 30
    }

    fun shouldLoadFromRemote(): Boolean {
        val savedTime = timeRepository.loadRemoteDataSavedTime()

        return savedTime + THIRTY_MINUTES_TO_MILLIS < System.currentTimeMillis()
    }

    fun saveCurrentTime() = timeRepository.saveCurrentTimeToLocal()

}