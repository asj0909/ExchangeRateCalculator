package jp.co.android.exchangeratecalculator.viewmodel

import android.content.Context
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
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
    private val context: Context = ApplicationProvider.getApplicationContext()

    private val testExchangeRate: ExchangeRate = ExchangeRate(
        listOf(
            Pair("USDJPY", "109.59"),
            Pair("USDKRW", "1162.38"),
            Pair("USDUSD", "1.0")
        )
    )

    @Before
    fun setUp() {
        // 同期処理にするように設定
        RxAndroidPlugins.setMainThreadSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }

        // viewModel生成
        viewModel = MainViewModel(
            mockCurrencyService,
            mockExchangeRateService,
            mockTimeService,
            context
        )
    }

    @Test
    fun getCurrencyList_ok() {
        // 仮の通貨データ取得するように設定
        every {
            mockCurrencyService.load()
        } returns Single.just(CurrencyList(listOf("JPY", "KRW", "USD")))

        // Observer生成
        val listDataObserver: Observer<List<String>> = mockk(relaxed = true)
        viewModel.currencyListData.observeForever(listDataObserver)

        // 実行
        viewModel.getCurrencyList()

        // 成功有無テスト
        verify(exactly = 1) {
            listDataObserver.onChanged(any())
        }

        val expectedFirstItem = "Please Select"
        val actualList = viewModel.currencyListData.value
        requireNotNull(actualList)

        // １番目のデータテスト
        Assert.assertEquals(expectedFirstItem, actualList.first())
        // "Please Select", "JPY", "KRW", "USD"の４つデータテスト
        Assert.assertEquals(4, actualList.count())
    }

    @Test
    fun getCurrencyList_error() {
        // 仮の通貨データ取得するように設定
        every {
            mockCurrencyService.load()
        } returns Single.error(Throwable())

        // Observer生成
        val errorDataObserver: Observer<MainViewModel.ErrorType> = mockk(relaxed = true)
        viewModel.error.observeForever(errorDataObserver)

        // 実行
        viewModel.getCurrencyList()

        // 失敗時、データ更新有無確認
        verify(exactly = 1) {
            errorDataObserver.onChanged(any())
        }
    }

    @Test
    fun setUsdToSelectedCurrencyExchangeRate_remote_ok() {
        // リモートからデータ取得するように設定
        every {
            mockTimeService.shouldLoadFromRemote()
        } returns true

        every {
            mockExchangeRateService.loadFromRemote()
        } returns Single.just(testExchangeRate)

        // 実行
        viewModel.setUsdToOthersExchangeRateList()

        // 成功時、データ更新有無確認
        verify {
            mockTimeService.saveCurrentTime()
            mockExchangeRateService.saveToLocal(testExchangeRate)
        }
    }

    @Test
    fun setUsdToSelectedCurrencyExchangeRate_remote_true_error() {
        // リモートからデータ取得するように設定
        every {
            mockTimeService.shouldLoadFromRemote()
        } returns true

        // Observer生成
        val errorDataObserver: Observer<MainViewModel.ErrorType> = mockk(relaxed = true)
        viewModel.error.observeForever(errorDataObserver)

        every {
            mockExchangeRateService.loadFromRemote()
        } returns Single.error(Throwable())

        // 実行
        viewModel.setUsdToOthersExchangeRateList()

        // 失敗時、データ更新有無確認
        verify(exactly = 1) {
            errorDataObserver.onChanged(any())
        }
    }

    @Test
    fun setUsdToSelectedCurrencyExchangeRate_local() {
        // ローカルからデータ取得するように設定
        every {
            mockTimeService.shouldLoadFromRemote()
        } returns false

        // 実行
        viewModel.setUsdToOthersExchangeRateList()

        // 成功時通るメソッド確認
        verify {
            mockExchangeRateService.loadFromLocal()
        }
    }

    @Test
    fun updateExchangeRateList() {
        // Observer生成
        val listDataObserver: Observer<List<Pair<String, String>>> = mockk(relaxed = true)
        viewModel.exchangeRateList.observeForever(listDataObserver)

        // 実行
        viewModel.selectedCurrencyName = "JPY"
        viewModel.updateExchangeRateList("")
        viewModel.updateExchangeRateList("1")
        viewModel.updateExchangeRateList("12")
        viewModel.updateExchangeRateList("123")

        // 空文字の場合はデータ取得しないため、３回になるか確認
        verify(exactly = 3) {
            listDataObserver.onChanged(any())
        }

        val list = viewModel.exchangeRateList.value
        requireNotNull(list)

        // 通貨名と為替レート一致有無確認
        list.mapIndexed { index, pair ->
            if (index == 0) {
                Assert.assertEquals("JPYJPY", pair.first)
                val expected = Utils.calculateSelectedCurrencyToTargetExchangeRate(
                    pair.second.toDouble(), "123".toDouble(), 109.59
                )
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
}