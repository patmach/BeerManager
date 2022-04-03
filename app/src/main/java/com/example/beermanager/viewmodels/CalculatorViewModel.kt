package com.example.beermanager.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.beermanager.MainActivity
import com.example.beermanager.MainActivity.Companion.currentDrinkingSession
import com.example.beermanager.R
import com.example.beermanager.data.TypeOfBeer
import com.example.beermanager.data.ViewModelResponse
import com.example.beermanager.data.getCalculatorGraphData
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.time.Duration.Companion.hours

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {
    private val resources = getApplication<Application>().resources

    private val _lineDataLiveData: MutableLiveData<ViewModelResponse<LineData, String>> by lazy {
        MutableLiveData<ViewModelResponse<LineData, String>>()
    }
    val lineDataLiveData: LiveData<ViewModelResponse<LineData, String>> = _lineDataLiveData

    private val _axisInfoLiveData: MutableLiveData<ViewModelResponse<ArrayList<String>, String>> by lazy {
        MutableLiveData<ViewModelResponse<ArrayList<String>, String>>()
    }
    val axisInfoLiveData: LiveData<ViewModelResponse<ArrayList<String>, String>> = _axisInfoLiveData

    private val _weightLiveData: MutableLiveData<ViewModelResponse<String, String>> by lazy {
        MutableLiveData<ViewModelResponse<String, String>>()
    }
    val weightInfoLiveData: LiveData<ViewModelResponse<String, String>> = _weightLiveData

    private val _sexLiveData: MutableLiveData<ViewModelResponse<Boolean, String>> by lazy {
        MutableLiveData<ViewModelResponse<Boolean, String>>()
    }
    val sexInfoLiveData: LiveData<ViewModelResponse<Boolean, String>> = _sexLiveData

    private val _soberMessageLiveData: MutableLiveData<ViewModelResponse<String, String>> by lazy {
        MutableLiveData<ViewModelResponse<String, String>>()
    }
    val soberMessageLiveData: LiveData<ViewModelResponse<String, String>> = _soberMessageLiveData

    /**
     * Checks correctness of weight value.
     */
    fun check(text: Editable):Boolean{
        return text.isNotEmpty() &&
                (text.toString().toDoubleOrNull()!=null)
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
            catch(e: IOException){
                loadError()
            }

            if (readFromFile) {
                _weightLiveData.postValue(ViewModelResponse.Success(fileContentJSON.getString("weight")))
                _sexLiveData.postValue(ViewModelResponse.Success(fileContentJSON.getBoolean("female")))
            }
        }
        catch(e: JSONException){
            loadError()
        }
    }

    private fun loadError(){
        _weightLiveData.postValue(ViewModelResponse.Error(resources.getString(R.string.load_error)))
        _sexLiveData.postValue(ViewModelResponse.Error(resources.getString(R.string.load_error)))
    }

    /**
     * Saves parameters (weight and sex) to file
     */
    fun saveParameters(weight:String, sex:Boolean) {
        val r = Runnable {
            try {
                MainActivity.fileContext?.openFileOutput(
                    "calculator_parameters.json",
                    Context.MODE_PRIVATE
                ).use { fos ->
                    if (fos != null) {
                        fos.write(("{\n \"weight\" : \"${weight}\" ,").toByteArray())
                        fos.write(("\n \"female\" : \"${sex.toString()}\"\n}").toByteArray())
                    }
                }
            }
            catch(e: IOException){
                Log.e("store", e.message + '\n' + e.stackTraceToString())
            }
            catch(e: JSONException){
                Log.e("store", e.message + '\n' + e.stackTraceToString())
            }
        }
        val t = Thread(r)
        t.start()
    }

    val HOUR = 1000*60*60

    /**
     * Calculates blood alcohol content if all alcohol would be drank in one moment.
     */
    fun calculateAlcohol(widmarkSexConstant:Double, weightInKg: Double): Double {
        var bac = currentDrinkingSession.calculateAlcohol(widmarkSexConstant,weightInKg)
        var noAlcoholTimeString= ""
        if(currentDrinkingSession.timeOfFirstAlcoholicBeer == Date(0)){
            noAlcoholTimeString= SimpleDateFormat("dd/MM HH:mm").format(Date());
        }
        else{
            val noAlcoholTime= Date(((bac/0.015)*HOUR + currentDrinkingSession.timeOfFirstAlcoholicBeer.time).toLong())
            noAlcoholTimeString= SimpleDateFormat("dd/MM HH:mm").format(noAlcoholTime);
        }
        _soberMessageLiveData.postValue(ViewModelResponse.Success(
            resources.getString(R.string.blood_alcohol_part_one) + ' ' + noAlcoholTimeString + ".\n\n" +
                    resources.getString(R.string.blood_alcohol_part_two)))
        return bac
    }

    /**
     * Computes data for graph and then creates graph.
     * @param bac - blood alcohol content not considering the duration of drinking
     */
    fun makeGraph(bac:Double) {
        val data = getCalculatorGraphData(bac, resources)
        _lineDataLiveData.postValue(ViewModelResponse.Success(data.first))
        _axisInfoLiveData.postValue(ViewModelResponse.Success(data.second))
    }

}