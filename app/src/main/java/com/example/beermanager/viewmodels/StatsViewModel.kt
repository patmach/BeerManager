package com.example.beermanager.viewmodels

import android.app.Application
import android.content.res.Resources
import android.graphics.Color
import android.widget.LinearLayout
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.beermanager.R
import com.example.beermanager.data.DrinkingSession
import com.example.beermanager.data.TypeOfBeer
import com.example.beermanager.data.ViewModelResponse
import com.example.beermanager.data.getStatsGraphData
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class StatsViewModel(application: Application) : AndroidViewModel(application) {
    private val resources = getApplication<Application>().resources

    private val _barDataLiveData: MutableLiveData<ViewModelResponse<BarData, String>> by lazy {
        MutableLiveData<ViewModelResponse<BarData, String>>()
    }
    val barDataLiveData: LiveData<ViewModelResponse<BarData, String>> = _barDataLiveData

    private val _axisInfoLiveData: MutableLiveData<ViewModelResponse<ArrayList<String>, String>> by lazy {
        MutableLiveData<ViewModelResponse<ArrayList<String>, String>>()
    }
    val axisInfoLiveData: LiveData<ViewModelResponse<ArrayList<String>, String>> = _axisInfoLiveData


    fun getGraphData(){
        val data = getStatsGraphData(resources)
        _barDataLiveData.postValue(ViewModelResponse.Success(data.first))
        _axisInfoLiveData.postValue(ViewModelResponse.Success(data.second))

    }
}