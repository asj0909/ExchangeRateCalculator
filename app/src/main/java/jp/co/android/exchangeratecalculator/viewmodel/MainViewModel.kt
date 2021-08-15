package jp.co.android.exchangeratecalculator.viewmodel

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import jp.co.android.exchangeratecalculator.Utils
import jp.co.android.exchangeratecalculator.domain.CurrencyService
import jp.co.android.exchangeratecalculator.domain.ExchangeRateService
import jp.co.android.exchangeratecalculator.domain.TimeService

class MainViewModel(
    private val currencyService: CurrencyService = CurrencyService(),
    private val exchangeRateService: ExchangeRateService = ExchangeRateService(),
    private val time: TimeService = TimeService()
) : ViewModel(), LifecycleObserver {

    companion object {
        const val REPLACED_CURRENCY_NAME_RANGE_START = 0
        const val REPLACED_CURRENCY_NAME_RANGE_END = 3
    }

    private val disposables: CompositeDisposable = CompositeDisposable()

    private val mutableCurrencyList: MutableLiveData<List<String>> = MutableLiveData()
    val currencyListData: LiveData<List<String>> = mutableCurrencyList

    private val mutableExchangeRateList: MutableLiveData<List<Pair<String, String>>> =
        MutableLiveData()
    val exchangeRateList: LiveData<List<Pair<String, String>>> = mutableExchangeRateList

    var selectedCurrencyName: String = ""

    private var usdToSelectedCurrencyExchangeRate: Double = 0.0
    private var usdToOthersExchangeRateList: List<Pair<String, String>> = listOf()

    fun shouldLoadFromRemote() : Boolean = time.shouldLoadFromRemote()

    fun setUsdToSelectedCurrencyExchangeRate() {
        if (shouldLoadFromRemote()) {
            exchangeRateService.getUsdToOthersExchangeRateListFromRemote()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess { exchangeRate ->
                    time.saveCurrentTime()
                    exchangeRateService.saveUsdToOthersExchangeRateListToLocal(exchangeRate)
                    usdToOthersExchangeRateList = exchangeRate.liveList
                    setSelectedCurrencyToOthersExchangeRate()
                }
                .subscribe()
                .addTo(disposables)
        } else {
            val exchangeRates = exchangeRateService.getUsdToOthersExchangeRateListFromLocal()
            usdToOthersExchangeRateList = exchangeRates.liveList
            setSelectedCurrencyToOthersExchangeRate()
        }
    }

    fun updateExchangeRateList(inputString: String) {
        if (inputString.isEmpty()) return

        val list = usdToOthersExchangeRateList.map {
            val inputNumber = inputString.toDouble()
            val usdToTargetExchangeRate = it.second.toDouble()
            val displayedCurrencyName = it.first.replaceRange(
                REPLACED_CURRENCY_NAME_RANGE_START,
                REPLACED_CURRENCY_NAME_RANGE_END,
                selectedCurrencyName
            )
            it.copy(
                first = displayedCurrencyName,
                second = Utils.calculateSelectedCurrencyToTargetExchangeRate(
                    usdToSelectedCurrencyExchangeRate,
                    inputNumber,
                    usdToTargetExchangeRate
                ).toString()
            )
        }
        mutableExchangeRateList.value = list
    }

    fun getCurrencyList() {
        currencyService
            .getCurrencyList()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess {
                mutableCurrencyList.value = listOf("Please Select").plus(it.currencyList)
            }
            .subscribe()
            .addTo(disposables)
    }

    private fun setSelectedCurrencyToOthersExchangeRate() {
        usdToOthersExchangeRateList.forEach {
            if ("USD$selectedCurrencyName" == it.first) {
                usdToSelectedCurrencyExchangeRate = it.second.toDouble()
            }
        }
    }
}
