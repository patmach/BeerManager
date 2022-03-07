package com.example.beermanager.data
import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.text.format.DateFormat
import android.view.View
import com.example.beermanager.SecondFragment.Companion.textViewBeerCount
import org.json.JSONObject
import java.util.*
import kotlin.collections.*
import com.example.beermanager.MainActivity.Companion.fileContext
import com.example.beermanager.SecondFragment.Companion.textViewPrices
import org.json.JSONException
import java.io.IOException
import java.io.InputStreamReader
import java.lang.System.out


/**
 * Class representing drinking session and contains methods that works with these sessions.
 */
class DrinkingActivity {
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
            allDrinkingActivities.add(this)
        }
        textViewBeerCount?.text = "  "+ getNumberOfBeers()
        val fullPrice= getFullPrice()
        if(fullPrice>0.0){
            textViewPrices?.text= " $fullPrice"
            textViewPrices?.visibility= View.VISIBLE
        }
        else{
            textViewPrices?.visibility= View.INVISIBLE
        }
    }

    /**
     * @return JSON representation of instance of DrinkingActivityClass
     */
    fun toJson(): JSONObject {
        var jsonString="{"
        jsonString+="\"startOfDrinking\":\"${startOfDrinking.time}\",\n"
        jsonString+="\"timeOfFirstAlcoholicBeer\":\"${timeOfFirstAlcoholicBeer.time}\",\n"
        jsonString+="\"currentTypeOfBeer\":\"${TypeOfBeer.values().indexOf(currentTypeOfBeer)}\",\n"
        jsonString+="\"drinkedBeers\": ["
        var first=true
        for (typeOfBeer in TypeOfBeer.values()){
            if (first) {
                first = false
            }
            else {
                jsonString+=","
            }
            jsonString+="\"${drankBeers[typeOfBeer]}\""
        }
        jsonString+="],\n"
        jsonString+="\"prices\": ["
        first=true
        for (typeOfBeer in TypeOfBeer.values()){
            if (first) {
                first = false
            }
            else {
                jsonString+=","
            }
            jsonString+="\"${prices[typeOfBeer]}\""
        }
        jsonString+="],\n"
        jsonString+="\"timeOfLastAlcoholicBeer\":\"${timeOfLastAlcoholicBeer.time}\",\n"
        jsonString+="\"lastDrink\":\"${lastDrink.time}\""
        jsonString+="}"
        return JSONObject(jsonString)
    }

    /**
     * Sets prices for new drinking activity from last values for each type.
     */
    private fun setPrices(){
        if (allDrinkingActivities.count()>0) {
            for (activity in allDrinkingActivities.reversed()) {
                for (typeOfBeer in TypeOfBeer.values()) {
                    if (activity.prices.containsKey(typeOfBeer) && (activity.prices[typeOfBeer]!! > 0.0)) {
                        prices[typeOfBeer] = activity.prices[typeOfBeer]!!.toDouble()
                    }
                }
                if (prices.all { it.value > 0.0 })
                    break
            }
        }
    }

    companion object {
        /**
         * Contains all drinking activities that started on the device since last installation of the app.
         */
        var allDrinkingActivities= ArrayList<DrinkingActivity>()

        /**
         * Saves all drinking activities to JSON file
         */
        fun saveAllDrinkingActivities() {
            try {
                fileContext?.openFileOutput("drinking_activities.json",MODE_PRIVATE).use { fos ->
                    if (fos != null) {
                        fos.write("{ \"allDrinkingActivities\" : [".toByteArray())
                        var first = true
                        allDrinkingActivities.forEach {
                            if (first) {
                                first = false
                            }
                            else {
                                fos.write(",".toByteArray())
                            }
                            fos.write("${it.toJson().toString()}\n".toByteArray())
                        }
                        fos.write("]}".toByteArray())
                    }
                }

            }
            catch(e:IOException){
                var debug=1
            }
            catch(e:JSONException){
                var debug=1
            }


        }

        /**
         * Loads all drinking activities from JSON file to variable allDrinkingActivities.
         */
        fun loadAllDrinkingActivities() {
            var fileContentJSON = JSONObject()
            var readFromFile=false
            try {
                try {
                    fileContext?.openFileInput("drinking_activities.json").use { fis ->
                        val inputStreamReader = InputStreamReader(fis)
                        allDrinkingActivities.clear()
                        val fileContent= inputStreamReader.readText()
                        fileContentJSON = JSONObject(fileContent)
                        readFromFile=true
                    }
                }
                catch(e:IOException){
                    var debug=1
                }

                if (readFromFile) {
                    val allDrinkingActivitiesJSON = fileContentJSON.getJSONArray("allDrinkingActivities")
                    for (i in 0 until allDrinkingActivitiesJSON.length()) {
                        val drinkingActivityJSON = allDrinkingActivitiesJSON.getJSONObject(i)
                        allDrinkingActivities.add(fromJSON(drinkingActivityJSON))
                    }
                }
            }
            catch(e:JSONException){
                var debug=1
            }


        }

        /**
         * Loads DrinkingActivity instance from JSON object
         */
        fun fromJSON(drinkingActivityJSON: JSONObject):DrinkingActivity{
            val drinkingActivity= DrinkingActivity()
            drinkingActivity.startOfDrinking=Date(drinkingActivityJSON.getLong("startOfDrinking"))
            drinkingActivity.timeOfFirstAlcoholicBeer=Date(drinkingActivityJSON.getLong("timeOfFirstAlcoholicBeer"))
            drinkingActivity.timeOfLastAlcoholicBeer=Date(drinkingActivityJSON.getLong("timeOfLastAlcoholicBeer"))
            drinkingActivity.lastDrink=Date(drinkingActivityJSON.getLong("lastDrink"))
            drinkingActivity.currentTypeOfBeer= TypeOfBeer.values()[drinkingActivityJSON.getInt("currentTypeOfBeer")]
            val drinkedBeersJSON = drinkingActivityJSON.getJSONArray("drinkedBeers")
            for (i in 0 until drinkedBeersJSON.length())  {
                val key= drinkingActivity.drankBeers.keys.elementAt(i)
                drinkingActivity.drankBeers[key] = drinkedBeersJSON.getInt(i)
            }
            if(drinkingActivityJSON.has("prices")) {
                val pricesJSON = drinkingActivityJSON.getJSONArray("prices")
                for (i in 0 until pricesJSON.length()) {
                    val key = drinkingActivity.prices.keys.elementAt(i)
                    drinkingActivity.prices[key] = pricesJSON.getDouble(i)
                }
            }
            return drinkingActivity
        }


        /**
         * @return Map where keys are month and year and values are number of all beers drank in that particular month. Starting with month before the first activity
         */
        fun getNumberOfBeersByMonth() : MutableMap<Pair<String,String>,Int> {
            val monthstats = mutableMapOf<Pair<String,String>,Int>()
            if(allDrinkingActivities.count()==0){
                val date=Date()
                val monthNumber = DateFormat.format("MM", date) as String
                val year = DateFormat.format("yyyy", date) as String
                monthstats[Pair(monthNumber,year)]=0
                return monthstats
            }
            var date = allDrinkingActivities.first().startOfDrinking
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.MONTH, -1)
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
            for (activity in allDrinkingActivities){
                val monthNumber = DateFormat.format("MM", activity.startOfDrinking) as String
                val year = DateFormat.format("yyyy", activity.startOfDrinking) as String
                val beersDrankInActivity:Int=activity.drankBeers.values.sum()
                monthstats[Pair(monthNumber,year)] = beersDrankInActivity + monthstats[Pair(monthNumber,year)]!!
            }
            return monthstats
        }
    }



}





