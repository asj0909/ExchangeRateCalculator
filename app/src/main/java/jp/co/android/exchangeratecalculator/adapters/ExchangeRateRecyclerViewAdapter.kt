package jp.co.android.exchangeratecalculator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import jp.co.android.exchangeratecalculator.R
import kotlinx.android.synthetic.main.cell_view.view.*

/**
 * 為替レートリスト表示用Adapter
 */
class ExchangeRateRecyclerViewAdapter(
    private val context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var list: List<Pair<String, String>> = listOf()

    override fun getItemCount(): Int = list.count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CommonViewHolder(
            LayoutInflater.from(context).inflate(R.layout.cell_view, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as CommonViewHolder).onBind(list, position)
    }

    class CommonViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun onBind(list: List<Pair<String, String>>, position: Int) {
            val currencyName = list[position].first
            val exchangeRate = list[position].second

            itemView.currency_name.text = currencyName
            itemView.exchange_rate.text = exchangeRate
        }
    }
}