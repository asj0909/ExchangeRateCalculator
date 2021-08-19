package jp.co.android.exchangeratecalculator.repository

import io.reactivex.Single
import jp.co.android.exchangeratecalculator.constants.ApiUrl
import jp.co.android.exchangeratecalculator.utils.createHttpClient
import jp.co.android.exchangeratecalculator.domain.CurrencyList
import jp.co.android.exchangeratecalculator.domain.CurrencyRepository
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 通貨リストレポジトリ
 */
class CurrencyRepositoryImpl(
    private val service: Service = createHttpClient(
        serviceClass = Service::class.java
    )
): CurrencyRepository {

    interface Service {

        @GET(ApiUrl.LIST)
        fun load(
            @Query("access_key") accessKey: String = ApiUrl.API_ACCESS_KEY
        ): Single<ListResponseData>

    }

    override fun load(): Single<CurrencyList> {
        return service.load().map {
            convert(it)
        }
    }

    private fun convert(responseData: ListResponseData): CurrencyList =
        responseData.currencies?.let {
            CurrencyList(it.keys.toList())
        } ?: CurrencyList(listOf())
}

/**
 * DTO
 */
data class ListResponseData(
    val success: Boolean,
    val terms: String?,
    val privacy: String?,
    val currencies: Map<String, String>?
)