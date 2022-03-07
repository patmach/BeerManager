package com.example.beermanager

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.beermanager.MainActivity.Companion.currentDrinkingActivity
import com.example.beermanager.data.TypeOfBeer
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

import com.example.beermanager.data.DrinkingActivity
import com.example.beermanager.data.hideKeyboard
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStreamReader


class CalculatorFragment : DialogFragment() {
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
            if(check())
                makeGraph(calculateAlcohol())
        })
        binding.edittextWeight.setOnClickListener(View.OnClickListener {
            //Graph is not displayed over upper elements when keyboard is shown.
            binding.lineGraph.visibility= View.INVISIBLE
        })
        if(binding.edittextWeight.text.isEmpty()){
            //Default params
            binding.edittextWeight.setText("60")
            binding.switchSex.isChecked=true
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        loadParameters()

    }

    override fun onDestroy() {
        super.onDestroy()
        saveParameters()
    }

    /**
     * Calculates blood alcohol content if all alcohol would be drank in one moment.
     */
    fun calculateAlcohol(): Double {
        val widmarkSexConstant = if (binding.switchSex.isChecked) 0.66 else 0.73
        var bac = 0.0
        val weightInOunces = binding.edittextWeight.text.toString().toDouble() * 35.2739619
        for (typeOfBeer in TypeOfBeer.values()){
            val volume = currentDrinkingActivity.drankBeers[typeOfBeer]?.times(500) ?: 0
            val alcoholPercentage = typeOfBeer.getAlcoholPercentage()
            bac += (volume*alcoholPercentage*5.14)/(weightInOunces*widmarkSexConstant);
        }
        val noAlcoholTime= Date(((bac/0.015)*(1000*60*60) + currentDrinkingActivity.timeOfFirstAlcoholicBeer.time).toLong())
        val noAlcoholTimeString=SimpleDateFormat("dd/MM HH:mm").format(noAlcoholTime);
        binding.textviewInfo.text="Your blood alcohol content should be 0.0 ‰ at "+ noAlcoholTimeString + ".\n\n" +
                "The calculation (using the Widmark formula) is not accurate and cannot be relied upon."
        return bac
    }

    /**
     * Checks correctness of weight value.
     */
    fun check():Boolean{
        if (binding.edittextWeight.text.isNotEmpty() &&
                (binding.edittextWeight.text.toString().toDoubleOrNull()!=null))
                    return true
        else{
            Toast.makeText(context, "The weight value has wrong format!\n\nYou can use only numbers and decimal point!", Toast.LENGTH_LONG).show();
            return false
        }
    }

    /**
     * omputes data for graph and then creates graph.
     * @param bac - blood alcohol content not considering the duration of drinking
     */
    fun makeGraph(bac:Double){
        var entries = ArrayList<Entry>()
        //How many hours between last and first alcoholic beer
        val diff = currentDrinkingActivity.timeOfLastAlcoholicBeer.time - currentDrinkingActivity.timeOfFirstAlcoholicBeer.time
        val hours = diff/ (1000*60*60).toDouble()
        var currentBac = bac - 0.015*hours
        var timeFromLastDrink=0.0
        //Time sequence between two values in graph.
        var densityOfData = 1.0/2 * bac*10
        var count =0
        //Computes all values until blood alcohol content is 0.0
        while(currentBac>0){
            entries.add(Entry(count.toFloat(), currentBac.toFloat()*10))
            timeFromLastDrink+=densityOfData
            currentBac = bac - 0.015*(hours+timeFromLastDrink)
            count++
        }
        //Adds another 4 values for zero BAC
        entries.add(Entry(count.toFloat(), 0.0.toFloat()))
        var indexOfFirst0= entries.count()-1
        for (i in 1..3) {
            count++
            timeFromLastDrink += densityOfData
            entries.add(Entry(count.toFloat(), 0.0.toFloat()))
        }

        //Loads times for xAxis labels
        var axisInfo= ArrayList<String>()
        var timeFromLastDrink2 = 0.0
        val densityOfLabels = 1//entries.count()/5 + 1
        count =0
        val dateFormat = SimpleDateFormat("HH:mm")
        dateFormat.timeZone=TimeZone.getDefault()
        while(timeFromLastDrink2<timeFromLastDrink){
            if ((count % densityOfLabels ==0)|| (axisInfo.count()==indexOfFirst0))
            {
                val date = Date((currentDrinkingActivity.timeOfLastAlcoholicBeer.time + (1000 * 60 * 60) * timeFromLastDrink2).toLong())
                axisInfo.add(dateFormat.format(date))
            }
            else{
                axisInfo.add("")
            }
            timeFromLastDrink2+=densityOfData
            count++
        }

        //Loading of data to graph. And adjusting the graph appearance.
        val lineDataSet = LineDataSet(entries,"‰ of alcohol")
        lineDataSet.color = Color.parseColor("#3B1E08")
        lineDataSet.valueFormatter = DefaultValueFormatter(2)
        val lineData = LineData(lineDataSet)
        var lineGraph= binding.lineGraph
        lineGraph.data=lineData
        lineGraph.xAxis.valueFormatter = IndexAxisValueFormatter(axisInfo)
        lineGraph.legend.textSize=10F
        lineGraph.xAxis.disableGridDashedLine()
        lineGraph.description.text=""
        lineGraph.lineData.setValueTextSize(15F)
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

    /**
     * Saves parameters (weight and sex) to file
     */
    fun saveParameters() {
        try {
            MainActivity.fileContext?.openFileOutput(
                "calculator_parameters.json",
                Context.MODE_PRIVATE
            ).use { fos ->
                if (fos != null) {
                    fos.write(("{\n \"weight\" : \"${binding.edittextWeight.text.toString()}\" ,").toByteArray())
                    fos.write(("\n \"female\" : \"${binding.switchSex.isChecked.toString()}\"\n}").toByteArray())
                }
            }
        }
        catch(e: IOException){
            var debug=1
        }
        catch(e: JSONException){
            var debug=1
        }
    }

    /**
     * Loads parameters (weight and sex) from file
     */
    fun loadParameters() {
        var fileContentJSON: JSONObject = JSONObject()
        var readFromFile=false
        try {
            try {
                MainActivity.fileContext?.openFileInput("calculator_parameters.json").use { fis ->
                    val inputStreamReader = InputStreamReader(fis)
                    var fileText= inputStreamReader.readText()
                    fileContentJSON = JSONObject(fileText)
                    readFromFile=true
                }
            }
            catch(e:IOException){
                var debug=1
            }

            if (readFromFile) {
                binding.edittextWeight.setText(fileContentJSON.getString("weight"))
                binding.switchSex.isChecked=fileContentJSON.getBoolean("female")
            }
        }
        catch(e:JSONException){
            var debug=e
        }
    }

}