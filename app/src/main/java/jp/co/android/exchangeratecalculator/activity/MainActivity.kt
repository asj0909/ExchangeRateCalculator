package jp.co.android.exchangeratecalculator.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import jp.co.android.exchangeratecalculator.R
import jp.co.android.exchangeratecalculator.viewmodel.MainViewModel
import jp.co.android.exchangeratecalculator.adapters.ExchangeRateRecyclerViewAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.input_number_et
import kotlinx.android.synthetic.main.activity_main.recycler_view

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener, TextWatcher {

    private val viewModel = lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    private val recyclerViewAdapter by lazy {
        ExchangeRateRecyclerViewAdapter(this@MainActivity)
    }

    private lateinit var spinnerAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Adapterセット
        recycler_view.apply {
            adapter = recyclerViewAdapter
            layoutManager = LinearLayoutManager(
                this@MainActivity, LinearLayoutManager.VERTICAL, false)
        }

        viewModel.value.getCurrencyList()

        spinner.onItemSelectedListener = this
        input_number_et.addTextChangedListener(this)
        input_number_et.isEnabled = false

        viewModel.value.currencyListData.observe(this, Observer {
            // 通貨リスト取得/表示
            spinnerAdapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item, it)

            spinner.adapter = spinnerAdapter
        })

        viewModel.value.exchangeRateList.observe(this, Observer {
            updateList(it)
        })

        viewModel.value.error.observe(this, Observer {
            when (it) {
                MainViewModel.ErrorType.FROM_LOCAL -> {
                    showAlert(resources.getString(R.string.local_network_error_text)) {
                        viewModel.value.getCurrencyList()
                    }
                }
                MainViewModel.ErrorType.FROM_REMOTE_CURRENCY -> {
                    showAlert(resources.getString(R.string.server_error_text)) {
                        viewModel.value.getCurrencyList()
                    }
                }
                MainViewModel.ErrorType.FROM_REMOTE_CHANGE_RATE -> {
                    showAlert(resources.getString(R.string.server_error_text)) {
                        viewModel.value.setUsdToOthersExchangeRateList()
                    }
                }
            }
        })
    }

    override fun onNothingSelected(parent: AdapterView<*>?) = Unit

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (position == 0) {
            input_number_et.isEnabled = false
            updateList(listOf())
            return
        }

        // 通貨が選択された時、為替レート取得
        input_number_et.isEnabled = true
        val selectedCurrency = spinner.selectedItem.toString()
        viewModel.value.selectedCurrencyName = selectedCurrency
        viewModel.value.setUsdToOthersExchangeRateList()
        viewModel.value.updateExchangeRateList(input_number_et.text.toString())
    }

    override fun afterTextChanged(s: Editable?) {
        // 数字を全部消した時、アイテム削除
        if (s.isNullOrEmpty() || spinner.selectedItemPosition == 0) {
            updateList(listOf())
            return
        }
        viewModel.value.updateExchangeRateList(s.toString())
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

    private fun updateList(newList: List<Pair<String, String>>) {
        recyclerViewAdapter.list = newList
        recyclerViewAdapter.notifyDataSetChanged()
    }

    private fun showAlert(message: String, callback:(() -> Unit)? = null) =
        AlertDialog.Builder(this)
            .setTitle("ERROR")
            .setMessage(message)
            .setPositiveButton(resources.getString(R.string.confirm_text)) { _, _ ->
                callback?.invoke()
            }
            .show()
}
