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
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ExchangeRateRemoteRepositoryImplTest {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var repository: ExchangeRateRemoteRepositoryImpl

    @Before
    fun setUp() {
        // モックサーバーセットアップ
        mockWebServer = MockWebServer()
        mockWebServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse =
                MockResponse().setBody(
                    "{\n" +
                            "  \"success\":true,\n" +
                            "  \"terms\":\"https:\\/\\/currencylayer.com\\/terms\",\n" +
                            "  \"privacy\":\"https:\\/\\/currencylayer.com\\/privacy\",\n" +
                            "  \"timestamp\":1628941924,\n" +
                            "  \"source\":\"USD\",\n" +
                            "  \"quotes\":{\n" +
                            "    \"USDJPY\":109.591504,\n" +
                            "    \"USDKRW\":1162.380384,\n" +
                            "    \"USDUSD\":1\n" +
                            "  }\n" +
                            "}"
                )
        })

        mockWebServer.start()

        // テスト用レポジトリ生成
        repository = ExchangeRateRemoteRepositoryImpl(
            service = createRetrofitService(
                mockWebServer,
                ExchangeRateRemoteRepositoryImpl.Service::class.java
            )
        )
    }

    @After
    fun tearDown() = mockWebServer.shutdown()

    @Test
    fun path() {
        // モックサーバーへリクエスト
        repository.load()
            .timeout(1, TimeUnit.SECONDS)
            .blockingGet()
        // パス一致テスト
        val request = mockWebServer.takeRequest(1, TimeUnit.SECONDS)
        assertEquals("/live?access_key=${ApiUrl.API_ACCESS_KEY}", request.path)
    }

    @Test
    fun convert() {
        // モックサーバーへリクエスト
        val result = repository
            .load()
            .blockingGet()

        // モックレスポンス確認テスト
        assertEquals(result.liveList[0], Pair("USDJPY", "109.591504"))
        assertEquals(result.liveList[1], Pair("USDKRW", "1162.380384"))
        assertEquals(result.liveList[2], Pair("USDUSD", "1"))
    }
}