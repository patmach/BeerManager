package com.example.beermanager.data

import android.graphics.Color
import android.os.Bundle
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.beermanager.databinding.StatsFragmentBinding
import android.widget.EditText
import com.example.beermanager.data.DrinkingActivity.Companion.getNumberOfBeersByMonth
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.*
import com.github.mikephil.charting.utils.ViewPortHandler


class StatsFragment: DialogFragment() {
    private var _binding: StatsFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var editTextPrices = ArrayList<EditText>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = StatsFragmentBinding.inflate(inflater, container, false)

        var monthStats=getNumberOfBeersByMonth()
        var barEntries = ArrayList<BarEntry>()
        var count = 0
        for(numberOfBeers in monthStats.values) {
            barEntries.add(BarEntry(count.toFloat(),numberOfBeers.toFloat()))

            count++
        }
        var axisInfo= java.util.ArrayList<String>()
        for(keypair in monthStats.keys){
            axisInfo.add(keypair.first+'/'+keypair.second.substring(2))
        }

        val barDataSet=BarDataSet(barEntries,"Number of beers")
        barDataSet.color = Color.parseColor("#3B1E08")
        barDataSet.valueFormatter = DefaultValueFormatter(0)
        var graphData = BarData(barDataSet)

        binding.barGraph.data=graphData
        binding.barGraph.setFitBars(true);
        binding.barGraph.xAxis.valueFormatter = IndexAxisValueFormatter(axisInfo)

        binding.barGraph.legend.textSize=20F
        binding.barGraph.xAxis.position=XAxis.XAxisPosition.BOTTOM
        binding.barGraph.axisLeft.textSize=0F
        binding.barGraph.axisRight.textSize=0F
        binding.barGraph.xAxis.disableGridDashedLine()
        binding.barGraph.description.text=""
        binding.barGraph.barData.setValueTextSize(15F)

        binding.barGraph.axisRight.apply {
            setDrawGridLines(false)
            setDrawLabels(false)
            setDrawAxisLine(false)
        }
        binding.barGraph.axisLeft.apply {
            setDrawGridLines(false)
            setDrawLabels(false)
            setDrawAxisLine(false)
        }
        binding.barGraph.xAxis.apply {
            setDrawGridLines(false)
            setDrawAxisLine(false)
            setCenterAxisLabels(false)

        }
        binding.barGraph.setDrawGridBackground(false)
        binding.barGraph.setDrawBorders(false)
        binding.barGraph.invalidate()
        return binding.root
    }

}
