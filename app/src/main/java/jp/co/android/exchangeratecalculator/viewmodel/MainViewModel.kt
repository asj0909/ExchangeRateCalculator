package jp.co.android.exchangeratecalculator.viewmodel

import android.content.Context
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import jp.co.android.exchangeratecalculator.utils.NetworkUtil
import jp.co.android.exchangeratecalculator.utils.CalculatorUtil
import jp.co.android.exchangeratecalculator.application.MainApplication
import jp.co.android.exchangeratecalculator.domain.CurrencyService
import jp.co.android.exchangeratecalculator.domain.ExchangeRateService

class MainViewModel(
    private val currencyService: CurrencyService = CurrencyService(),
    private val exchangeRateService: ExchangeRateService = ExchangeRateService(),
    private val context: Context = MainApplication.applicationContext()
) : ViewModel(), LifecycleObserver {

    companion object {
        const val REPLACED_CURRENCY_NAME_RANGE_START = 0
        const val REPLACED_CURRENCY_NAME_RANGE_END = 3
        const val SELECT_TEXT = "Please Select"
    }

    enum class ErrorType {
        FROM_LOCAL,
        FROM_REMOTE_CURRENCY,
        FROM_REMOTE_CHANGE_RATE
    }

    private val disposables: CompositeDisposable = CompositeDisposable()

    private val mutableCurrencyList: MutableLiveData<List<String>> = MutableLiveData()
    val currencyListData: LiveData<List<String>> = mutableCurrencyList

    private val mutableExchangeRateList: MutableLiveData<List<Pair<String, String>>> =
        MutableLiveData()
    val exchangeRateList: LiveData<List<Pair<String, String>>> = mutableExchangeRateList

    private val mutableError: MutableLiveData<ErrorType> = MutableLiveData()
    val error: LiveData<ErrorType> = mutableError

    var selectedCurrencyName: String = ""

    private var usdToSelectedCurrencyExchangeRate: Double = 0.0
    private var usdToOthersExchangeRateList: List<Pair<String, String>> = listOf()


    private fun isNetworkAvailable(): Boolean = NetworkUtil.isNetworkAvailable(context)

    /**
     * 通貨リスト取得
     */
    fun getCurrencyList() {
        if (!isNetworkAvailable()) {
            mutableError.value = ErrorType.FROM_LOCAL
            return
        }
        currencyService
            .load()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                mutableCurrencyList.value = listOf(SELECT_TEXT).plus(it.currencyList)
            }, {
                mutableError.value = ErrorType.FROM_REMOTE_CURRENCY
            })
            .addTo(disposables)
    }

    /**
     *  「USD基準選択された通貨の為替レートセット
     */
    fun setUsdToOthersExchangeRateList() {
        if (!isNetworkAvailable()) {
            mutableError.value = ErrorType.FROM_LOCAL
            return
        }
        // 為替レートリスト取得
        exchangeRateService.load()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ exchangeRate ->
                usdToOthersExchangeRateList = exchangeRate.liveList
                setUsdToSelectedCurrencyExchangeRate()
            }, {
                mutableError.value = ErrorType.FROM_REMOTE_CHANGE_RATE
            })
            .addTo(disposables)
    }

    /**
     * 為替レートリスト更新
     */
    fun updateExchangeRateList(inputString: String) {
        if (inputString.isEmpty()) return

        val list = usdToOthersExchangeRateList.map {
            val inputNumber = inputString.toDouble()
            val usdToTargetExchangeRate = it.second.toDouble()
            // 通貨名更新
            val displayedCurrencyName = it.first.replaceRange(
                REPLACED_CURRENCY_NAME_RANGE_START,
                REPLACED_CURRENCY_NAME_RANGE_END,
                selectedCurrencyName
            )
            it.copy(
                first = displayedCurrencyName,
                // 為替レート更新
                second = CalculatorUtil.calculateSelectedCurrencyToTargetExchangeRate(
                    usdToSelectedCurrencyExchangeRate,
                    inputNumber,
                    usdToTargetExchangeRate
                ).toString()
            )
        }
        mutableExchangeRateList.value = list
    }

    /**
     * USD基準選択された通貨の為替レートセット
     */
    private fun setUsdToSelectedCurrencyExchangeRate() =
        usdToOthersExchangeRateList.forEach {
            if ("USD$selectedCurrencyName" == it.first) {
                usdToSelectedCurrencyExchangeRate = it.second.toDouble()
            }
        }
}
