package jp.co.android.exchangeratecalculator.domain

import io.reactivex.Single
import jp.co.android.exchangeratecalculator.repository.ExchangeRateLocalRepositoryImpl
import jp.co.android.exchangeratecalculator.repository.ExchangeRateRemoteRepositoryImpl

class ExchangeRateService(
    private val remoteRepositoryImpl: ExchangeRateRemoteRepository = ExchangeRateRemoteRepositoryImpl(),
    private val localRepository: ExchangeRateLocalRepository = ExchangeRateLocalRepositoryImpl()
) {

    fun loadFromRemote(): Single<ExchangeRate> =
        remoteRepositoryImpl.load()

    fun loadFromLocal(): ExchangeRate =
        localRepository.load()

    fun saveToLocal(exchangeRates: ExchangeRate) =
        localRepository.save(exchangeRates)
}

interface ExchangeRateRemoteRepository {

    fun load(): Single<ExchangeRate>

}

interface ExchangeRateLocalRepository {

    fun load(): ExchangeRate
    fun save(list: ExchangeRate)

}

data class ExchangeRate(val liveList: List<Pair<String, String>>)