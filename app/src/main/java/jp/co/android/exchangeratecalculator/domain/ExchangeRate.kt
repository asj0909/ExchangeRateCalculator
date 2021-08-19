package jp.co.android.exchangeratecalculator.domain

import io.reactivex.Single
import jp.co.android.exchangeratecalculator.repository.ExchangeRateLocalRepositoryImpl
import jp.co.android.exchangeratecalculator.repository.ExchangeRateRemoteRepositoryImpl

/**
 * 為替レート取得/保存用Service
 */
class ExchangeRateService(
    private val remoteRepositoryImpl: ExchangeRateRemoteRepository = ExchangeRateRemoteRepositoryImpl(),
    private val localRepository: ExchangeRateLocalRepository = ExchangeRateLocalRepositoryImpl(),
    private val time: TimeService = TimeService()
) {

    fun load(): Single<ExchangeRate> {
        return if (time.shouldLoadFromRemote()) {
            remoteRepositoryImpl.load().map {
                time.saveCurrentTime()
                saveToLocal(it)
                it
            }
        } else {
            localRepository.load()
        }
    }

    private fun saveToLocal(exchangeRates: ExchangeRate) =
        localRepository.save(exchangeRates)
}

interface ExchangeRateRemoteRepository {

    fun load(): Single<ExchangeRate>

}

interface ExchangeRateLocalRepository {

    fun load(): Single<ExchangeRate>
    fun save(list: ExchangeRate)

}

data class ExchangeRate(val liveList: List<Pair<String, String>>)