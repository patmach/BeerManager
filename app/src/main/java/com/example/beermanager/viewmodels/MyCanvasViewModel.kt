package com.example.beermanager.viewmodels

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.beermanager.data.BitmapProcessor
import com.example.beermanager.data.ViewModelResponse
import com.example.beermanager.data.getStatsGraphData
import com.github.mikephil.charting.data.BarData

class MyCanvasViewModel(application: Application): AndroidViewModel(application) {
    private val resources = getApplication<Application>().resources

    private val _barDataLiveData: MutableLiveData<ViewModelResponse<BarData, String>> by lazy {
        MutableLiveData<ViewModelResponse<BarData, String>>()
    }
    val barDataLiveData: LiveData<ViewModelResponse<BarData, String>> = _barDataLiveData

    private val _axisInfoLiveData: MutableLiveData<ViewModelResponse<ArrayList<String>, String>> by lazy {
        MutableLiveData<ViewModelResponse<ArrayList<String>, String>>()
    }
    val axisInfoLiveData: LiveData<ViewModelResponse<ArrayList<String>, String>> = _axisInfoLiveData

    private val bitmapProcessor = BitmapProcessor()

    fun setBitmap(width: Int, height: Int, canvas: Canvas, bitmap: Bitmap){
        bitmapProcessor.saveCanvasToBitmap(width,height,canvas, bitmap)
    }
/*
    fun getGraphData(){
        val data = getStatsGraphData(resources)
        _barDataLiveData.postValue(ViewModelResponse.Success(data.first))
        _axisInfoLiveData.postValue(ViewModelResponse.Success(data.second))

    }*/
}