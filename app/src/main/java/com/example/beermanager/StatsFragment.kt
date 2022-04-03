package com.example.beermanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.beermanager.databinding.StatsFragmentBinding
import com.example.beermanager.data.ViewModelResponse
import com.example.beermanager.viewmodels.StatsViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.*

/**
 * Controls views of Stats Fragment layout and performs required actions.
 */
class StatsFragment: DialogFragment() {
    private val statsViewModel by viewModels<StatsViewModel>()
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
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        statsViewModel.barDataLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is ViewModelResponse.Success -> {
                    binding.barGraph.data=it.content
                    binding.barGraph.barData.setValueTextSize(15F)
                    binding.barGraph.invalidate()
                }
            }
        }
        statsViewModel.axisInfoLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is ViewModelResponse.Success -> {
                    binding.barGraph.xAxis.valueFormatter = IndexAxisValueFormatter(it.content)
                    binding.barGraph.invalidate()
                }
            }
        }
        statsViewModel.getGraphData()
        setGraph()
    }

    fun setGraph(){
        binding.barGraph.setFitBars(true);
        //Adjusting the graph view
        binding.barGraph.legend.textSize=20F
        binding.barGraph.xAxis.position=XAxis.XAxisPosition.BOTTOM
        binding.barGraph.axisLeft.textSize=0F
        binding.barGraph.axisRight.textSize=0F
        binding.barGraph.xAxis.disableGridDashedLine()
        binding.barGraph.description.text=""


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
    }

}
