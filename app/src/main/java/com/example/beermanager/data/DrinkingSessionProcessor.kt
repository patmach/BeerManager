package com.example.beermanager.data

import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import com.example.beermanager.MainActivity
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStreamReader
import java.util.*
import kotlin.collections.ArrayList

class DrinkingSessionProcessor {
    /**
     * Contains all drinking sessions that started on the device since last installation of the app.
     */
    var allDrinkingSessions= ArrayList<DrinkingSession>()

    fun addNewDrinkingSession(drinkingSession: DrinkingSession){
        allDrinkingSessions.add(drinkingSession)
    }

    /**
     * Saves all drinking activities to JSON file
     */
    fun saveAllDrinkingSessions() {
        val r = Runnable {
            try {
                MainActivity.fileContext?.openFileOutput("drinking_activities.json",
                    Context.MODE_PRIVATE
                ).use { fos ->
                    if (fos != null) {
                        var gson = Gson()
                        fos.write(gson.toJson(allDrinkingSessions).toByteArray());
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

    /**
     * Loads all drinking sessions from JSON file to variable allDrinkingSession.
     */
    fun loadAllDrinkingSessions() {
        var fileContentJSON = JSONObject()
        var allDrinkingSessionsJSON: JSONArray = JSONArray()
        var readFromFile=false
        try {
            try {
                MainActivity.fileContext?.openFileInput("drinking_activities.json").use { fis ->
                    val inputStreamReader = InputStreamReader(fis)
                    allDrinkingSessions.clear()
                    val fileContent= inputStreamReader.readText()
                    if((fileContent!=null) && (fileContent!="")) {
                        allDrinkingSessionsJSON = JSONArray(fileContent)
                        readFromFile = true
                    }
                }
            }
            catch(e: IOException){
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
        catch(e: JSONException){
            Log.e("load", e.message + '\n' + e.stackTraceToString())
        }
        catch(e: JsonSyntaxException){
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
     * @return JSON representation of instance of DrinkingSessionClass
     */
    fun toJson(drinkingSession: DrinkingSession): JSONObject {
        var gson = Gson()
        val jsonString = gson.toJson(drinkingSession);
        return JSONObject(jsonString);
    }


    /**
     * @return Map where keys are month and year and values are number of all beers drank in that particular month. Starting with month before the first activity
     */
    fun getNumberOfBeersByMonth() : MutableMap<Pair<String,String>,Int> {
        val monthstats = mutableMapOf<Pair<String,String>,Int>()
        if(allDrinkingSessions.count()==0){
            val date= Date()
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
        while(date<= Date()){
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