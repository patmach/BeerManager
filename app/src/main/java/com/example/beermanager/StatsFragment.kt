package com.example.beermanager

import android.graphics.Color
import android.os.Bundle
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

/**
 * Controls views of Stats Fragment layout and performs required actions.
 */
class StatsFragment: DialogFragment() {
    private var _binding: StatsFragmentBinding? = null
    private val binding get() = _binding!!


    /**
     * Loads data and creates graph representation.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = StatsFragmentBinding.inflate(inflater, container, false)
        var monthStats=getNumberOfBeersByMonth()
        var barEntries = ArrayList<BarEntry>()
        var count = 0
        //data
        for(numberOfBeers in monthStats.values) {
            barEntries.add(BarEntry(count.toFloat(),numberOfBeers.toFloat()))
            count++
        }
        //X axis labels
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

        //Adjusting the graph view
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
