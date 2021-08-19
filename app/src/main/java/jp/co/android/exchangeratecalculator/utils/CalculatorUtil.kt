package jp.co.android.exchangeratecalculator.utils

object CalculatorUtil {

    /**
     * 選択された通貨基準、ターゲット通貨為替レート計算
     */
    fun calculateSelectedCurrencyToTargetExchangeRate(
        usdToSelectedCurrencyExchangeRate: Double,
        inputNumber: Double,
        usdToTargetExchangeRate: Double
    ): Double {
        return 1 / usdToSelectedCurrencyExchangeRate * inputNumber * usdToTargetExchangeRate
    }

}