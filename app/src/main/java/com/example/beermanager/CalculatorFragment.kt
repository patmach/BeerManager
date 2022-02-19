package com.example.beermanager

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.beermanager.MainActivity.Companion.currentDrinkingActivity
import com.example.beermanager.data.TypeOfBeer
import com.example.beermanager.databinding.CalculatorFragmentBinding
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import kotlin.collections.ArrayList


class CalculatorFragment : DialogFragment() {
    private var _binding: CalculatorFragmentBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = CalculatorFragmentBinding.inflate(inflater, container, false)
        val width= Resources.getSystem().getDisplayMetrics().widthPixels;
        val height=Resources.getSystem().getDisplayMetrics().heightPixels;
        binding.lineGraph.minimumHeight= ((height - binding.layout1.measuredHeight-binding.layout2.measuredHeight)*0.7).toInt()
        binding.lineGraph.minimumWidth= (width*0.9).toInt()
        binding.buttonCalculate.setOnClickListener(View.OnClickListener {
            if(check())
                makeGraph(calculateAlcohol())
        })
        if(binding.edittextWeight.text.isEmpty()){
            binding.edittextWeight.setText("60")
            binding.switchSex.isChecked=true
        }
        binding.buttonCalculate.performClick()
        return binding.root

    }

    fun calculateAlcohol(): Double {
        val widmarkSexConstant = if (binding.switchSex.isChecked) 0.55 else 0.68
        var bac = 0.0
        val weightInOunces = binding.edittextWeight.text.toString().toDouble() * 35.2739619
        for (typeOfBeer in TypeOfBeer.values()){
            val volume = currentDrinkingActivity.drankBeers[typeOfBeer]?.times(500) ?: 0
            val alcoholPercentage = typeOfBeer.getAlcoholPercentage()
            bac += (volume*alcoholPercentage*5.14)/(weightInOunces*widmarkSexConstant);
        }
        return bac
    }

    fun check():Boolean{
        if (binding.edittextWeight.text.isNotEmpty() &&
                (binding.edittextWeight.text.toString().toDoubleOrNull()!=null))
                    return true
        else{
            Toast.makeText(context, "The weight value has wrong format!\n\nYou can use only numbers and decimal point!", Toast.LENGTH_LONG).show();
            return false
        }
    }

    fun makeGraph(bac:Double){

        var entries = ArrayList<Entry>()
        val diff = currentDrinkingActivity.lastDrink.time - currentDrinkingActivity.startOfDrinking.time
        val hours = diff/ (1000*60*60).toDouble()
        var currentBac = bac - 0.015*hours
        var timeFromLastDrink=0.0
        var densityOfData = 1.0/2 * bac*10
        while(currentBac>0){
            entries.add(Entry(timeFromLastDrink.toFloat(), currentBac.toFloat()*10))
            timeFromLastDrink+=densityOfData
            currentBac = bac - 0.015*(hours+timeFromLastDrink)
        }
        entries.add(Entry(timeFromLastDrink.toFloat(), 0.0.toFloat()))
        for (i in 1..3) {
            timeFromLastDrink += densityOfData
            entries.add(Entry(timeFromLastDrink.toFloat(), 0.0.toFloat()))
        }
        var axisInfo= ArrayList<String>()
        var timeFromLastDrink2=0.0
        val densityOfLabels = entries.count()/20 + 1
        var count =0
        val dateFormat = SimpleDateFormat("hh:mm")
        while(timeFromLastDrink2<timeFromLastDrink){
            if (count % densityOfLabels ==0)
            {
                val date = Date((currentDrinkingActivity.lastDrink.time + (1000 * 60 * 60) * timeFromLastDrink2).toLong())
                axisInfo.add(dateFormat.format(date))
            }
            else{
                axisInfo.add("")
            }
            timeFromLastDrink2+=densityOfData
            count++
        }
        val lineDataSet = LineDataSet(entries,"‰ of alcohol")
        val lineData = LineData(lineDataSet)
        binding.lineGraph.data=lineData
        binding.lineGraph.xAxis.valueFormatter = IndexAxisValueFormatter(axisInfo)
        binding.lineGraph.invalidate()
        binding.lineGraph.visibility=View.VISIBLE
    }
}