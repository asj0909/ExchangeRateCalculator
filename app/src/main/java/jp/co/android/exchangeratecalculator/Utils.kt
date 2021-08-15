package jp.co.android.exchangeratecalculator

object Utils {

    fun calculateSelectedCurrencyToTargetExchangeRate(
        usdToSelectedCurrencyExchangeRate: Double,
        inputNumber: Double,
        usdToTargetExchangeRate: Double
    ): Double {
        return 1 / usdToSelectedCurrencyExchangeRate * inputNumber * usdToTargetExchangeRate
    }

}