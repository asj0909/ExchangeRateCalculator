package jp.co.android.exchangeratecalculator.domain

import io.reactivex.Single
import jp.co.android.exchangeratecalculator.repository.CurrencyRepositoryImpl

class CurrencyService(
    private val repositoryImpl: CurrencyRepository = CurrencyRepositoryImpl()
) {

    fun getCurrencyList(): Single<CurrencyList> = repositoryImpl.getCurrencyList()

}

interface CurrencyRepository {

    fun getCurrencyList(): Single<CurrencyList>

}

data class CurrencyList(val currencyList: List<String>)