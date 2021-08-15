package jp.co.android.exchangeratecalculator.viewmodel

import androidx.lifecycle.Observer
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Single
import jp.co.android.exchangeratecalculator.Utils
import jp.co.android.exchangeratecalculator.domain.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MainViewModelTest {

    private lateinit var viewModel: MainViewModel
    private val mockCurrencyService: CurrencyService = mockk(relaxed = true)
    private val mockExchangeRateService: ExchangeRateService = mockk(relaxed = true)
    private val mockTimeService: TimeService = mockk(relaxed = true)


    @Before
    fun setUp() {

        every {
            mockCurrencyService.getCurrencyList()
        } returns Single.just(CurrencyList(listOf("JPY", "KRW", "USD")))

        every {
            mockExchangeRateService.getUsdToOthersExchangeRateListFromRemote()
        } returns Single.just(
            ExchangeRate(
                listOf(
                    Pair("USDJPY", "109.59"),
                    Pair("USDKRW", "1162.38"),
                    Pair("USDUSD", "1.0")
                )
            )
        )

        viewModel = MainViewModel(
            mockCurrencyService,
            mockExchangeRateService,
            mockTimeService
        )
    }

    @Test
    fun setUsdToSelectedCurrencyExchangeRate_when_shouldLoadFromRemote_true() {
        every {
            mockTimeService.shouldLoadFromRemote()
        } returns true

        viewModel.setUsdToSelectedCurrencyExchangeRate()

        verify {
            mockTimeService.saveCurrentTime()
            mockExchangeRateService.getUsdToOthersExchangeRateListFromRemote()

        }
    }

    @Test
    fun setUsdToSelectedCurrencyExchangeRate_when_shouldLoadFromRemote_false() {
        every {
            mockTimeService.shouldLoadFromRemote()
        } returns false

        viewModel.setUsdToSelectedCurrencyExchangeRate()

        verify {
            mockExchangeRateService.getUsdToOthersExchangeRateListFromLocal()

        }
    }

    @Test
    fun updateExchangeRateList() {
        val listDataObserver: Observer<List<Pair<String, String>>> = mockk(relaxed = true)
        viewModel.exchangeRateList.observeForever(listDataObserver)

        viewModel.selectedCurrencyName = "JPY"
        viewModel.updateExchangeRateList("")
        viewModel.updateExchangeRateList("1")
        viewModel.updateExchangeRateList("12")
        viewModel.updateExchangeRateList("123")

        verify(exactly = 3) {
            listDataObserver.onChanged(any())
        }

        val list = viewModel.exchangeRateList.value ?: listOf()
        list.mapIndexed { index, pair ->
            if (index == 0) {
                Assert.assertEquals("JPYJPY", pair.first)
                val expected = Utils.calculateSelectedCurrencyToTargetExchangeRate(
                    pair.second.toDouble(), "123".toDouble(), 109.59)
                Assert.assertEquals(expected, pair.second)
            }
            if (index == 1) {
                Assert.assertEquals("JPYKRW", pair.first)
                val expected = Utils.calculateSelectedCurrencyToTargetExchangeRate(
                    pair.second.toDouble(), "123".toDouble(), 1162.38)
                Assert.assertEquals(expected, pair.second)
            }
            if (index == 2) {
                Assert.assertEquals("JPYUSD", pair.first)
                val expected = Utils.calculateSelectedCurrencyToTargetExchangeRate(
                    pair.second.toDouble(), "123".toDouble(), 1.0)
                Assert.assertEquals(expected, pair.second)
            }
        }
    }

    @Test
    fun getCurrencyList() {
        val listDataObserver: Observer<List<String>> = mockk(relaxed = true)
        viewModel.currencyListData.observeForever(listDataObserver)

        viewModel.getCurrencyList()

        verify(exactly = 1) {
            listDataObserver.onChanged(any())
        }

        val expected = "Please Select"
        val actual = viewModel.currencyListData.value?.first() ?: ""
        Assert.assertEquals(expected, actual)

    }
}