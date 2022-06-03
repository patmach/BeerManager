package com.example.beermanager.viewmodels

import android.app.Application
import android.content.res.Resources
import android.widget.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.beermanager.MainActivity
import com.example.beermanager.R
import com.example.beermanager.data.TypeOfBeer
import com.example.beermanager.data.ViewModelResponse

class PriceViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private val _pricesLiveData: MutableLiveData<ViewModelResponse<MutableMap<TypeOfBeer, String>, String>> by lazy {
        MutableLiveData<ViewModelResponse<MutableMap<TypeOfBeer, String>, String>>()
    }
    private val _prices :MutableMap<TypeOfBeer, String> by lazy {
        mutableMapOf()
    }

    val pricesLiveData : LiveData<ViewModelResponse<MutableMap<TypeOfBeer, String>, String>> = _pricesLiveData

    /**
     * Saves prices to current drinking activity instance.
     */
    fun savePrices(texts: ArrayList<String>) {

        for (i in 0..(TypeOfBeer.values().size-1)) {
            val value = texts[i].toString()
            if ((value != "") && (value!=null))
                MainActivity.currentDrinkingSession.prices[TypeOfBeer.values()[i]] = value.toDouble()

        }
        val fullPrice = MainActivity.currentDrinkingSession.getFullPrice()

    }

    /**
     * Loads prices from current drinking activity instance to created edittexts.
     */
    fun loadPrices() {
        var index=0
        for (typeOfBeer in TypeOfBeer.values()) {
            if (MainActivity.currentDrinkingSession.prices.containsKey(typeOfBeer) && (MainActivity.currentDrinkingSession.prices[typeOfBeer]!! > 0.0))
                _prices[typeOfBeer]= MainActivity.currentDrinkingSession.prices[typeOfBeer].toString()
            else
                _prices[typeOfBeer]=""
            index++
        }
        _pricesLiveData.postValue(ViewModelResponse.Success(_prices));
    }
}


