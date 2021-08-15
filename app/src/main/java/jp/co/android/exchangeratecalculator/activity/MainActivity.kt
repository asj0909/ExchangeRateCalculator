package jp.co.android.exchangeratecalculator.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
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

        recycler_view.apply {
            adapter = recyclerViewAdapter
            layoutManager = LinearLayoutManager(
                this@MainActivity, LinearLayoutManager.VERTICAL, false)
        }

        spinner.onItemSelectedListener = this
        input_number_et.addTextChangedListener(this)

        viewModel.value.getCurrencyList()

        viewModel.value.currencyListData.observe(this, Observer {
            spinnerAdapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item, it)

            spinner.adapter = spinnerAdapter
        })

        viewModel.value.exchangeRateList.observe(this, Observer {
            recyclerViewAdapter.updateItems(it)
        })

    }

    override fun onNothingSelected(parent: AdapterView<*>?) = Unit

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (position == 0) {
            recyclerViewAdapter.updateItems(listOf())
            return
        }

        val selectedCurrency = spinner.selectedItem.toString()
        viewModel.value.selectedCurrencyName = selectedCurrency
        viewModel.value.setUsdToSelectedCurrencyExchangeRate()
        viewModel.value.updateExchangeRateList(input_number_et.text.toString())
    }

    override fun afterTextChanged(s: Editable?) {
        if (s.isNullOrEmpty() || spinner.selectedItemPosition == 0) {
            recyclerViewAdapter.updateItems(listOf())
            return
        }
        viewModel.value.updateExchangeRateList(s.toString())
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
}
