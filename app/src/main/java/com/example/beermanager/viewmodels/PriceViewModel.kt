package com.example.beermanager.viewmodels

import android.app.Application
import android.content.res.Resources
import android.provider.Settings.Global.getString
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.core.view.children
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.beermanager.MainActivity
import com.example.beermanager.R
import com.example.beermanager.data.TypeOfBeer
import com.example.beermanager.data.ViewModelResponse

class PriceViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private val _layoutsLiveData: MutableLiveData<ViewModelResponse<MutableMap<TypeOfBeer, LinearLayout>, String>> by lazy {
        MutableLiveData<ViewModelResponse<MutableMap<TypeOfBeer, LinearLayout>, String>>()
    }
    private val _layouts :MutableMap<TypeOfBeer, LinearLayout> by lazy {
        mutableMapOf()
    }
    private val _editTexts :MutableMap<TypeOfBeer, EditText> by lazy {
        mutableMapOf()
    }

    val layoutsLiveData : LiveData<ViewModelResponse<MutableMap<TypeOfBeer, LinearLayout>, String>> = _layoutsLiveData

    fun getLayoutsAndEditTexts(){
        if(_layouts.isEmpty() || _editTexts.isEmpty()) {
            for (typeOfBeer in TypeOfBeer.values()) {
                val p = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                val id = View.generateViewId();
                val newLinearLayout = LinearLayout(context)
                newLinearLayout.layoutParams =
                    LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 0)
                newLinearLayout.gravity = Gravity.CENTER
                newLinearLayout.id = id
                val textView = TextView(context)
                textView.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                textView.text = typeOfBeer.toStringWithNumber()
                val editText = EditText(context);
                editText.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                editText.minEms = 10
                editText.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
                if (MainActivity.currentDrinkingSession.prices[typeOfBeer]!! > 0.0) {
                    editText.setText(MainActivity.currentDrinkingSession.prices[typeOfBeer].toString())
                }
                _editTexts[typeOfBeer] = editText
                newLinearLayout.post(Runnable { newLinearLayout.addView(textView, p) })
                newLinearLayout.post(Runnable { newLinearLayout.addView(editText, p) })
                _layouts[typeOfBeer] = newLinearLayout
            }
        }
        loadPrices()
        _layoutsLiveData.postValue(ViewModelResponse.Success(_layouts))
    }


    /**
     * Saves prices to current drinking activity instance.
     */
    fun savePrices() {
        try
        {
            for (typeOfBeer in TypeOfBeer.values()) {
                val value = _editTexts[typeOfBeer]?.text.toString()
                if (value != "")
                    MainActivity.currentDrinkingSession.prices[typeOfBeer] = value.toDouble()

            }
            val fullPrice = MainActivity.currentDrinkingSession.getFullPrice()
        }
        catch (e:NumberFormatException){
            _layoutsLiveData.postValue(ViewModelResponse.Error(Resources.getSystem().getString(R.string.wrong_price_value)))
            //Toast.makeText(context, getString(R.string.wrong_price_value), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Loads prices from current drinking activity instance to created edittexts.
     */
    fun loadPrices() {
        var index=0
        for (typeOfBeer in TypeOfBeer.values()) {
            if (MainActivity.currentDrinkingSession.prices.containsKey(typeOfBeer) && (MainActivity.currentDrinkingSession.prices[typeOfBeer]!! > 0.0))
                _editTexts[typeOfBeer]?.setText(MainActivity.currentDrinkingSession.prices[typeOfBeer].toString())
            index++
        }
    }
}


