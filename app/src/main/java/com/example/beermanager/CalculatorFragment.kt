package com.example.beermanager

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.beermanager.MainActivity.Companion.currentDrinkingSession
import com.example.beermanager.data.TypeOfBeer
import com.example.beermanager.data.ViewModelResponse
import com.example.beermanager.databinding.CalculatorFragmentBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import com.github.mikephil.charting.formatter.DefaultValueFormatter

import com.example.beermanager.data.hideKeyboard
import com.example.beermanager.viewmodels.CalculatorViewModel
import com.example.beermanager.viewmodels.StatsViewModel
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStreamReader
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours


class CalculatorFragment : DialogFragment() {
    private val calculatorViewModel by viewModels<CalculatorViewModel>()

    private var _binding: CalculatorFragmentBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = CalculatorFragmentBinding.inflate(inflater, container, false)
        //Sets minimal height and width for graph according to display size.
        val displayWidth= Resources.getSystem().getDisplayMetrics().widthPixels;
        val displayHeight=Resources.getSystem().getDisplayMetrics().heightPixels;
        binding.lineGraph.minimumWidth= (displayWidth*0.9).toInt()
        binding.lineGraph.minimumHeight= ((displayHeight - binding.layout1.measuredHeight- binding.layout2.measuredHeight
                - binding.layout3.measuredHeight- binding.layout4.measuredHeight)
                *0.6).toInt()
        binding.buttonCalculate.setOnClickListener(View.OnClickListener {
            hideKeyboard()
            if(calculatorViewModel.check(binding.edittextWeight.text)) {
                val widmarkSexConstant = if (binding.switchSex.isChecked) 0.66 else 0.73
                calculatorViewModel.makeGraph(calculatorViewModel.calculateAlcohol(widmarkSexConstant, binding.edittextWeight.text.toString().toDouble()))
            }
            else
                Toast.makeText(context, getString(R.string.wrong_weight_value), Toast.LENGTH_LONG).show();
        })
        binding.edittextWeight.setOnClickListener(View.OnClickListener {
            //Graph is not displayed over upper elements when keyboard is shown.
            binding.lineGraph.visibility= View.INVISIBLE
        })
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        calculatorViewModel.weightInfoLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is ViewModelResponse.Success -> {
                    binding.edittextWeight.setText(it.content)
                }
                is ViewModelResponse.Error -> {
                    binding.edittextWeight.setText("60")
                }
                is ViewModelResponse.Idle -> {}
                is ViewModelResponse.Loading -> {}
            }
        }
        calculatorViewModel.sexInfoLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is ViewModelResponse.Success -> {
                    binding.switchSex.isChecked=it.content
                }
                is ViewModelResponse.Error -> {}
                is ViewModelResponse.Idle -> {}
                is ViewModelResponse.Loading -> {}
            }
        }
        calculatorViewModel.soberMessageLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is ViewModelResponse.Success -> {
                    binding.textviewInfo.text = it.content
                }
                is ViewModelResponse.Error -> {}
                is ViewModelResponse.Idle -> {}
                is ViewModelResponse.Loading -> {}
            }
        }
        calculatorViewModel.lineDataLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is ViewModelResponse.Success -> {
                    binding.lineGraph.data=it.content
                    binding.lineGraph.lineData.setValueTextSize(15F)
                    binding.lineGraph.invalidate()
                }
                is ViewModelResponse.Error -> {}
                is ViewModelResponse.Idle -> {}
                is ViewModelResponse.Loading -> {}
            }
        }
        calculatorViewModel.axisInfoLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is ViewModelResponse.Success -> {
                    binding.lineGraph.xAxis.valueFormatter = IndexAxisValueFormatter(it.content)
                    binding.lineGraph.invalidate()
                }
                is ViewModelResponse.Error -> {}
                is ViewModelResponse.Idle -> {}
                is ViewModelResponse.Loading -> {}
            }
        }

        calculatorViewModel.loadParameters()
        setGraph()

    }

    override fun onStop() {
        super.onStop()
        calculatorViewModel.saveParameters(binding.edittextWeight.text.toString(), binding.switchSex.isChecked)
    }


    fun setGraph(){
        var lineGraph= binding.lineGraph
        lineGraph.legend.textSize=10F
        lineGraph.xAxis.disableGridDashedLine()
        lineGraph.description.text=""
        lineGraph.xAxis.position= XAxis.XAxisPosition.BOTTOM
        lineGraph.axisLeft.textSize=0F
        //lineGraph.setVisibleXRangeMaximum(8F)
        lineGraph.xAxis.apply {
            setDrawGridLines(false)
            setDrawAxisLine(true)
            setDrawLabels(true)
            setCenterAxisLabels(false)
        }
        lineGraph.axisLeft.apply {
            setDrawGridLines(false)
            setDrawLabels(false)
            setDrawAxisLine(false)
        }
        lineGraph.axisRight.apply {
            setDrawGridLines(false)
            setDrawAxisLine(false)
            setCenterAxisLabels(false)
        }
        lineGraph.setBackgroundColor(Color.WHITE)
        lineGraph.visibility=View.VISIBLE
        lineGraph.invalidate()
    }
}