package jp.co.android.exchangeratecalculator.domain

import io.reactivex.Single
import jp.co.android.exchangeratecalculator.repository.ExchangeRateLocalRepositoryImpl
import jp.co.android.exchangeratecalculator.repository.ExchangeRateRepositoryImpl


class ExchangeRateService(
    private val remoteRepositoryImpl: ExchangeRateRemoteRepository = ExchangeRateRepositoryImpl(),
    private val localRepository: ExchangeRateLocalRepository = ExchangeRateLocalRepositoryImpl()
) {

    fun getUsdToOthersExchangeRateListFromRemote(): Single<ExchangeRate> =
        remoteRepositoryImpl.loadUsdToOthersExchangeRateListFromRemote()

    fun getUsdToOthersExchangeRateListFromLocal(): ExchangeRate =
        localRepository.loadUsdToOthersExchangeRateListFromLocal()

    fun saveUsdToOthersExchangeRateListToLocal(exchangeRates: ExchangeRate) =
        localRepository.saveUsdToOthersExchangeRateListToLocal(exchangeRates)
}

interface ExchangeRateRemoteRepository {

    fun loadUsdToOthersExchangeRateListFromRemote(): Single<ExchangeRate>

}

interface ExchangeRateLocalRepository {

    fun loadUsdToOthersExchangeRateListFromLocal(): ExchangeRate
    fun saveUsdToOthersExchangeRateListToLocal(list: ExchangeRate)

}

data class ExchangeRate(val liveList: List<Pair<String, String>>)