package jp.co.android.exchangeratecalculator.repository

import jp.co.android.exchangeratecalculator.constants.ApiUrl
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class CurrencyRepositoryImplTest {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var repository: CurrencyRepositoryImpl

    @Before
    fun setUp() {
        // モックサーバーセットアップ
        mockWebServer = MockWebServer()
        mockWebServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse =
                MockResponse().setBody(
                    "{\n" +
                            "\"success\": true,\n" +
                            "\"terms\": \"https://currencylayer.com/terms\",\n" +
                            "\"privacy\": \"https://currencylayer.com/privacy\",\n" +
                            "\"currencies\": {\n" +
                            "   \"AED\": \"United Arab Emirates Dirham\",\n" +
                            "   \"AFN\": \"Afghan Afghani\",\n" +
                            "   \"ALL\": \"Albanian Lek\",\n" +
                            "   \"AMD\": \"Armenian Dram\",\n" +
                            "   \"ANG\": \"Netherlands Antillean Guilder\"\n" +
                            "   }\n" +
                            "}"
                )
        })

        mockWebServer.start()

        // テスト用レポジトリ生成
        repository = CurrencyRepositoryImpl(
            service = createRetrofitService(
                mockWebServer,
                CurrencyRepositoryImpl.Service::class.java
            )
        )
    }

    @After
    fun tearDown() = mockWebServer.shutdown()

    @Test
    fun path() {
        // モックサーバーへリクエスト
        repository.load().timeout(1, TimeUnit.SECONDS).blockingGet()
        // パス一致テスト
        val request = mockWebServer.takeRequest(1, TimeUnit.SECONDS)
        assertEquals("/list?access_key=${ApiUrl.API_ACCESS_KEY}", request.path)
    }

    @Test
    fun convert() {
        // モックサーバーへリクエスト
        val result = repository
            .load()
            .blockingGet()

        // モックレスポンス確認テスト
        assertEquals(result.currencyList[0], "AED")
        assertEquals(result.currencyList[1], "AFN")
        assertEquals(result.currencyList[2], "ALL")
        assertEquals(result.currencyList[3], "AMD")
        assertEquals(result.currencyList[4], "ANG")
    }
}