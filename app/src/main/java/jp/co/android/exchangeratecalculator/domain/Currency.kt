package jp.co.android.exchangeratecalculator.domain

import io.reactivex.Single
import jp.co.android.exchangeratecalculator.repository.CurrencyRepositoryImpl

/**
 * 通貨リスト取得用Service
 */
class CurrencyService(
    private val repositoryImpl: CurrencyRepository = CurrencyRepositoryImpl()
) {

    fun load(): Single<CurrencyList> = repositoryImpl.load()

}

interface CurrencyRepository {

    fun load(): Single<CurrencyList>

}

data class CurrencyList(val currencyList: List<String>)