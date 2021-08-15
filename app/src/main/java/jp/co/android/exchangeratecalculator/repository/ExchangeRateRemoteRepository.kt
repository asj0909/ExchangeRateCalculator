package jp.co.android.exchangeratecalculator.repository

import io.reactivex.Single
import jp.co.android.exchangeratecalculator.constants.ApiUrl
import jp.co.android.exchangeratecalculator.createHttpClient
import jp.co.android.exchangeratecalculator.domain.ExchangeRateRemoteRepository
import jp.co.android.exchangeratecalculator.domain.ExchangeRate
import retrofit2.http.GET
import retrofit2.http.Query

class ExchangeRateRepositoryImpl(
    private val service: Service = createHttpClient(serviceClass = Service::class.java)
): ExchangeRateRemoteRepository {

    interface Service {
        @GET(ApiUrl.LIVE)

        fun getUsdToOthersExchangeRateList(
            @Query("access_key") accessKey: String = ApiUrl.API_ACCESS_KEY
        ): Single<ExchangeRateResponseData>

    }

    override fun loadUsdToOthersExchangeRateListFromRemote(): Single<ExchangeRate> {
        return service.getUsdToOthersExchangeRateList().map { convert(it) }
    }

    private fun convert(responseData: ExchangeRateResponseData): ExchangeRate {
        return responseData.quotes?.let {
            ExchangeRate(it.toList())
        } ?: ExchangeRate(listOf())
    }
}

data class ExchangeRateResponseData(
    val success: Boolean,
    val terms: String?,
    val privacy: String?,
    val timestamp: Long?,
    val source: String?,
    val quotes: Map<String, String>?
)