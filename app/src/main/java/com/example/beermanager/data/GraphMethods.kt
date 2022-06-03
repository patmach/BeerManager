package com.example.beermanager.data

import android.content.res.Resources
import android.graphics.Color
import com.example.beermanager.MainActivity
import com.example.beermanager.R
import com.example.beermanager.data.DrinkingSession.Companion.drinkingSessionProcessor
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.time.Duration.Companion.hours

val HOUR = 1000*60*60

/**
 *
 * @param bac Blood alcohol content (if all was drunk in one moment)
 * @param resources Application resources
 * @return Data and axis labels for alcohol calculator graph
*/
fun getCalculatorGraphData(bac: Double, resources: Resources): Pair<LineData, ArrayList<String>>{
    val hours = computeDurationOfCurrentDrinkingInHours()
    //Time sequence between two values in graph.
    var densityOfData = 1.0 / 2 * bac * 10
    val res =computeAlcoholCalculatorGraphData(bac, densityOfData)
    val timeFromLastDrink = res.first
    val entries = res.second
    val axisInfo = loadAxisLabelsForAlcoholCalculatorGraph(timeFromLastDrink,densityOfData)
    //Loading of data to graph. And adjusting the graph appearance.
    val lineDataSet = LineDataSet(entries, resources.getString(R.string.per_mille))
    lineDataSet.color = Color.parseColor("#3B1E08")
    lineDataSet.valueFormatter = DefaultValueFormatter(2)
    val lineData = LineData(lineDataSet)
    return Pair<LineData, ArrayList<String>>(lineData,axisInfo)
}

/**
 * @return Number of hours between last and first alcoholic beer
 */
private fun computeDurationOfCurrentDrinkingInHours():Double{
    val diff =
        MainActivity.currentDrinkingSession.timeOfLastAlcoholicBeer.time - MainActivity.currentDrinkingSession.timeOfFirstAlcoholicBeer.time
    return diff / HOUR.toDouble()
}

private  fun computeAlcoholCalculatorGraphData(bac: Double, densityOfData:Double): Pair<Double, ArrayList<Entry>> {
    val hours = computeDurationOfCurrentDrinkingInHours()
    var entries = ArrayList<Entry>()
    var currentBac = bac - 0.015 * hours
    var timeFromLastDrink = 0.0

    var count = 0
    //Computes all values until blood alcohol content is 0.0
    while (currentBac > 0) {
        entries.add(Entry(count.toFloat(), currentBac.toFloat() * 10))
        timeFromLastDrink += densityOfData
        currentBac = bac - 0.015 * (hours + timeFromLastDrink)
        count++
    }
    //Adds another 4 values for zero BAC
    entries.add(Entry(count.toFloat(), 0.0.toFloat()))
    for (i in 1..3) {
        count++
        timeFromLastDrink += densityOfData
        entries.add(Entry(count.toFloat(), 0.0.toFloat()))
    }
    return Pair(timeFromLastDrink, entries)
}

private fun loadAxisLabelsForAlcoholCalculatorGraph(timeFromLastDrink:Double, densityOfData: Double): ArrayList<String> {
    //Loads times for xAxis labels
    var axisInfo = ArrayList<String>()
    var timeFromLastDrink2 = 0.0
    val densityOfLabels = 1//entries.count()/5 + 1
    var count = 0
    val dateFormat = SimpleDateFormat("HH:mm")
    dateFormat.timeZone = TimeZone.getDefault()
    while (timeFromLastDrink2 < timeFromLastDrink) {
        if ((count % densityOfLabels == 0)) {
            val date =
                Date((MainActivity.currentDrinkingSession.timeOfLastAlcoholicBeer.time + HOUR * timeFromLastDrink2).toLong())
            val duration = (1.0 * timeFromLastDrink2).hours
            axisInfo.add(dateFormat.format(date))
        } else {
            axisInfo.add("")
        }
        timeFromLastDrink2 += densityOfData
        count++
    }
    return axisInfo
}

/**
 * @param resources Application resources
 * @return Data and axis labels for month stats graph
 */
fun getStatsGraphData(resources: Resources):Pair<BarData, ArrayList<String>> {
    var monthStats = drinkingSessionProcessor.getNumberOfBeersByMonth()
    var barEntries = ArrayList<BarEntry>()
    var count = 0
    //data
    for (numberOfBeers in monthStats.values) {
        barEntries.add(BarEntry(count.toFloat(), numberOfBeers.toFloat()))
        count++
    }
    //X axis labels
    var axisInfo = ArrayList<String>()
    for (keypair in monthStats.keys) {
        axisInfo.add(keypair.first + '/' + keypair.second.substring(2))
    }
    val barDataSet = BarDataSet(barEntries, resources.getString(R.string.number_of_beers))
    barDataSet.color = Color.parseColor("#3B1E08")
    barDataSet.valueFormatter = DefaultValueFormatter(0)
    var graphData = BarData(barDataSet)
    return Pair(graphData, axisInfo)
}