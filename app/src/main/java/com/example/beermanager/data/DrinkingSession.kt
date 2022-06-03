package com.example.beermanager.data
import android.content.Context.MODE_PRIVATE

import android.text.format.DateFormat
import android.util.Log
import android.view.View
import com.example.beermanager.MainActivity
import org.json.JSONObject
import java.util.*
import kotlin.collections.*
import com.example.beermanager.MainActivity.Companion.fileContext
import com.example.beermanager.R
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat


/**
 * Class representing drinking session and contains methods that works with these sessions.
 */
class DrinkingSession {
    /**
     * Time when first beer was added
     */
    var startOfDrinking: Date = Date(0)

    /**
     * Time when first alcoholic beer was added
     */
    var timeOfFirstAlcoholicBeer: Date = Date(0)

    /**
     * Stores number of beer for each type of beer
     */
    var drankBeers: MutableMap<TypeOfBeer, Int> = mutableMapOf()

    /**
     * Stores price of 1 beer of each type
     */
    var prices: MutableMap<TypeOfBeer, Double> = mutableMapOf()

    /**
     * Time when last alcoholic beer was added
     */
    var timeOfLastAlcoholicBeer: Date = Date(0)

    /**
     * Time when last beer was added
     */
    var lastDrink: Date = Date(0)

    /**
     * Type of beer currently chosen by user
     */
    var currentTypeOfBeer:TypeOfBeer= TypeOfBeer.ELEVEN

    companion object {
        val drinkingSessionProcessor = DrinkingSessionProcessor()
    }

    /**
     * Constructor that initially sets both mutable maps
     */
    init{
        for (typeOfBeer in TypeOfBeer.values()) {
            drankBeers[typeOfBeer] = 0
            prices[typeOfBeer]= 0.0
        }
        setPrices()
    }

    /**
     * @return Sum of all beers drank in this drinking session
     */
    fun getNumberOfBeers(): Int{
        return drankBeers.values.sum()
    }

    /**
     * @return Price for all beers drank in this drinking session. (must be set for all types of beer)
     */
    fun getFullPrice():Double{
        var sum=0.0
       for (typeOfBeer in TypeOfBeer.values()){
           sum+= drankBeers[typeOfBeer]!! * prices[typeOfBeer]!!
       }
        return sum
    }

    /**
     * Adds one beer of currently chosen type. And sets other class variables accordingly.
     */
    fun addBeer(){
        drankBeers[currentTypeOfBeer]= drankBeers.getOrDefault(currentTypeOfBeer,0) + 1
        lastDrink= Date()
        if((currentTypeOfBeer!=TypeOfBeer.NONALCOHOLIC)) {
            timeOfLastAlcoholicBeer = Date()
            if(timeOfFirstAlcoholicBeer==Date(0))
                timeOfFirstAlcoholicBeer=Date()
        }
        if(getNumberOfBeers()==1) {
            startOfDrinking = Date()
            drinkingSessionProcessor.addNewDrinkingSession(this)
        }
    }



    /**
     * Sets prices for new drinking activity from last values for each type.
     */
    private fun setPrices(){
        if (drinkingSessionProcessor.allDrinkingSessions.count()>0) {
            for (session in drinkingSessionProcessor.allDrinkingSessions.reversed()) {
                for (typeOfBeer in TypeOfBeer.values()) {
                    if (session.prices.containsKey(typeOfBeer) && (session.prices[typeOfBeer]!! > 0.0)) {
                        prices[typeOfBeer] = session.prices[typeOfBeer]!!.toDouble()
                    }
                }
                if (prices.all { it.value > 0.0 })
                    break
            }
        }
    }


    /**
     * Calculates blood alcohol content if all alcohol would be drank in one moment.
     */
    fun calculateAlcohol(widmarkSexConstant:Double, weightInKg: Double): Double {
        var bac = 0.0
        val weightInOunces = weightInKg * 35.2739619
        for (typeOfBeer in TypeOfBeer.values()){
            val volume = this.drankBeers[typeOfBeer]?.times(500) ?: 0
            val alcoholPercentage = typeOfBeer.getAlcoholPercentage()
            bac += (volume*alcoholPercentage*5.14)/(weightInOunces*widmarkSexConstant);
        }
        return bac
    }




}





