package com.ath.bondoman.ui.chart

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Pie
import com.anychart.enums.Align
import com.anychart.enums.LegendLayout
import com.ath.bondoman.databinding.FragmentChartBinding
import com.ath.bondoman.viewmodel.ChartViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChartFragment : Fragment() {

    private var _binding: FragmentChartBinding? = null

    private val binding get() = _binding!!
    private val chartViewModel:ChartViewModel by viewModels()
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val chartViewModel =
                ViewModelProvider(this).get(ChartViewModel::class.java)

        _binding = FragmentChartBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textChart
        chartViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        val data: MutableList<DataEntry> = mutableListOf(
            ValueDataEntry("Income", 0.0),
            ValueDataEntry("Expenditure", 0.0)
        )
        val pie = AnyChart.pie()

        pie.labels().position("outside")

        pie.legend().title().enabled(true)
        pie.legend().title()
            .text("Financial Report (in Rp)")
            .padding(0.0, 0.0, 10.0, 0.0)

        pie.legend()
            .position("center-bottom")
            .itemsLayout(LegendLayout.HORIZONTAL)
            .align(Align.CENTER)

        val anyChartView: AnyChartView=binding.anyChartView
        chartViewModel.allIncome.observe(viewLifecycleOwner) { income ->
            Log.d("Income", income.toString())
            // Update Income entry
            data[0] = ValueDataEntry("Income", income)
            // Refresh the chart
            updateChartFragment(data,anyChartView,textView,pie)
        }
        chartViewModel.allExpenditure.observe(viewLifecycleOwner) { expenditure ->
            Log.d("Expenditure", expenditure.toString())
            // Update Expenditure entry
            data[1] = ValueDataEntry("Expenditure",expenditure)
            updateChartFragment(data,anyChartView,textView,pie)
        }
        anyChartView.setChart(pie)
        return root
    }

    fun updateChartFragment(data:MutableList<DataEntry>, anyChartView:AnyChartView,textView:TextView, pie: Pie) {
        if (data.isEmpty() || isChartDataZero(data)) {
            // Pie chart is empty, update the TextView
            anyChartView.visibility = View.GONE
            textView.visibility = View.VISIBLE
        } else {
            // Pie chart has data, update the AnyChartView
            textView.visibility = View.GONE
            anyChartView.visibility = View.VISIBLE
            pie.data(data)
        }
    }

    // TODO: check zero in valuedataentry
    fun isChartDataZero(data:MutableList<DataEntry>): Boolean {
        if (data.size>1) {
            val incomeEntry = data[0]
            val expenditureEntry = data[1]
            var isIncomeZero=false
            var isExpenditureZero = false

            isIncomeZero = incomeEntry.getValue("value").equals(0.0)
            isExpenditureZero=expenditureEntry.getValue("value").equals(0.0)


            Log.d("IsIncomeZero",incomeEntry.getValue("value").toString())
            Log.d("IsExpenditureZero",incomeEntry.getValue("value").toString())
            return isIncomeZero && isExpenditureZero
        }else{
            return true;
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}