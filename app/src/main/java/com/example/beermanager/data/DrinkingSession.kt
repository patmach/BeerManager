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
            allDrinkingSessions.add(this)
        }
    }

    /**
     * @return JSON representation of instance of DrinkingSessionClass
     */
    fun toJson(): JSONObject {
        var gson = Gson()
        val jsonString = gson.toJson(this);
        return JSONObject(jsonString);
    }

    /**
     * Sets prices for new drinking activity from last values for each type.
     */
    private fun setPrices(){
        if (allDrinkingSessions.count()>0) {
            for (session in allDrinkingSessions.reversed()) {
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

    companion object {
        /**
         * Contains all drinking sessions that started on the device since last installation of the app.
         */
        var allDrinkingSessions= ArrayList<DrinkingSession>()

        /**
         * Saves all drinking activities to JSON file
         */
        fun saveAllDrinkingSessions() {
            val r = Runnable {
                try {
                    fileContext?.openFileOutput("drinking_activities.json",MODE_PRIVATE).use { fos ->
                        if (fos != null) {
                            var gson = Gson()
                            fos.write(gson.toJson(allDrinkingSessions).toByteArray());
                        }
                    }

                }
                catch(e:IOException){
                    Log.e("store", e.message + '\n' + e.stackTraceToString())
                }
                catch(e:JSONException){
                    Log.e("store", e.message + '\n' + e.stackTraceToString())
                }
            }
            val t = Thread(r)
            t.start()
        }

        /**
         * Loads all drinking sessions from JSON file to variable allDrinkingSession.
         */
        fun loadAllDrinkingSessions() {
            var fileContentJSON = JSONObject()
            var allDrinkingSessionsJSON: JSONArray = JSONArray()
            var readFromFile=false
            try {
                try {
                    fileContext?.openFileInput("drinking_activities.json").use { fis ->
                        val inputStreamReader = InputStreamReader(fis)
                        allDrinkingSessions.clear()
                        val fileContent= inputStreamReader.readText()
                        if((fileContent!=null) && (fileContent!="")) {
                            allDrinkingSessionsJSON = JSONArray(fileContent)
                            readFromFile = true
                        }
                    }
                }
                catch(e:IOException){
                    Log.e("load", e.message + '\n' + e.stackTraceToString())
                }

                if (readFromFile) {
                    //val allDrinkingSessionsJSON = fileContentJSON.getJSONArray("allDrinkingSessions")
                    for (i in 0 until (allDrinkingSessionsJSON.length())) {
                        val drinkingSessionJSON = allDrinkingSessionsJSON.getJSONObject(i)
                        allDrinkingSessions.add(fromJSON(drinkingSessionJSON))
                    }
                }
            }
            catch(e:JSONException){
                Log.e("load", e.message + '\n' + e.stackTraceToString())
            }
            catch(e:JsonSyntaxException){
                Log.e("load", e.message + '\n' + e.stackTraceToString())
            }


        }


        /**
         * Loads drinkingSession instance from JSON object
         */
        fun fromJSON(drinkingSessionJSON: JSONObject):DrinkingSession{
            var gson = Gson()
            val drinkingSession= gson.fromJson(drinkingSessionJSON.toString(), DrinkingSession::class.java)
            return drinkingSession;
        }


        /**
         * @return Map where keys are month and year and values are number of all beers drank in that particular month. Starting with month before the first activity
         */
        fun getNumberOfBeersByMonth() : MutableMap<Pair<String,String>,Int> {
            val monthstats = mutableMapOf<Pair<String,String>,Int>()
            if(allDrinkingSessions.count()==0){
                val date=Date()
                val monthNumber = DateFormat.format("MM", date) as String
                val year = DateFormat.format("yyyy", date) as String
                monthstats[Pair(monthNumber,year)]=0
                return monthstats
            }
            var date = allDrinkingSessions.first().startOfDrinking
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.MONTH, -1)
            cal.set(Calendar.DAY_OF_MONTH,1)
            cal.set(Calendar.HOUR_OF_DAY,0)
            cal.set(Calendar.MINUTE,0)
            cal.set(Calendar.SECOND,0)
            date= cal.time
            while(date<=Date()){
                val monthNumber = DateFormat.format("MM", date) as String
                val year = DateFormat.format("yyyy", date) as String
                monthstats[Pair(monthNumber,year)]=0
                val cal = Calendar.getInstance()
                cal.time = date
                cal.add(Calendar.MONTH, 1)
                date= cal.time
            }
            for (activity in allDrinkingSessions){
                val monthNumber = DateFormat.format("MM", activity.startOfDrinking) as String
                val year = DateFormat.format("yyyy", activity.startOfDrinking) as String
                val beersDrankInActivity:Int=activity.drankBeers.values.sum()
                monthstats[Pair(monthNumber,year)] = beersDrankInActivity + monthstats[Pair(monthNumber,year)]!!
            }
            return monthstats
        }
    }



}





