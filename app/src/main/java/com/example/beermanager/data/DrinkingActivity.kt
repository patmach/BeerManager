package com.example.beermanager.data
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
import android.text.format.DateUtils





class DrinkingActivity {
    var startOfDrinking: Date = Date(0);
    var timeOfFirstAlcoholicBeer: Date = Date(0);
    var drankBeers: MutableMap<TypeOfBeer, Int> = mutableMapOf<TypeOfBeer,Int>()
    var prices: MutableMap<TypeOfBeer, Double> = mutableMapOf<TypeOfBeer,Double>()
    var timeOfLastAlcoholicBeer: Date = Date(0);
    var lastDrink: Date = Date(0);

    var currentTypeOfBeer:TypeOfBeer= TypeOfBeer.ELEVEN
    init{
        for (typeOfBeer in TypeOfBeer.values()) {
            drankBeers[typeOfBeer] = 0
            prices[typeOfBeer]= 0.0
        }
    }

    fun getNumberOfBeers(): Int{
        return drankBeers.values.sum()
    }

    fun getFullPrice():Double{
        var sum=0.0
       for (typeOfBeer in TypeOfBeer.values()){
           sum+= drankBeers[typeOfBeer]!! * prices[typeOfBeer]!!
       }
        return sum
    }



    fun addBeer(){
        drankBeers[currentTypeOfBeer]= drankBeers.getOrDefault(currentTypeOfBeer,0) + 1;
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
            textViewPrices?.text= " " + fullPrice.toString()
            textViewPrices?.visibility= View.VISIBLE
        }
        else{
            textViewPrices?.visibility= View.INVISIBLE
        }
    }


    fun toJson(): JSONObject {
        var jsonString="{"
        jsonString+="\"startOfDrinking\":\"${startOfDrinking.time}\",\n"
        jsonString+="\"timeOfFirstAlcoholicBeer\":\"${timeOfFirstAlcoholicBeer.time}\",\n"
        jsonString+="\"currentTypeOfBeer\":\"${TypeOfBeer.values().indexOf(currentTypeOfBeer)}\",\n"
        jsonString+="\"drinkedBeers\": ["
        var first=true
        for (typeOfBeer in TypeOfBeer.values()){
            if (first) {
                first = false;
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
                first = false;
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

    companion object {
        var allDrinkingActivities= ArrayList<DrinkingActivity>()
        fun saveAllDrinkingActivities() {
            try {
                fileContext?.openFileOutput("drinking_activities.json",MODE_PRIVATE).use { fos ->
                    if (fos != null) {
                        fos.write("{ \"allDrinkingActivities\" : [".toByteArray())
                        var first = true;
                        allDrinkingActivities.forEach {
                            if (first) {
                                first = false;
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
                out.println(e.stackTrace)
            }
            catch(e:JSONException){
                out.println(e.stackTrace)
            }


        }


        fun loadAllDrinkingActivities() {
            var fileContentJSON:JSONObject=JSONObject()
            var readFromFile=false
            try {
                try {
                    fileContext?.openFileInput("drinking_activities.json").use { fis ->
                        val inputStreamReader = InputStreamReader(fis)
                        allDrinkingActivities.clear()
                        var debug= inputStreamReader.readText()
                        fileContentJSON = JSONObject(debug)
                        readFromFile=true
                    }
                }
                catch(e:IOException){
                    out.println(e.stackTrace)
                }

                if (readFromFile) {
                    var allDrinkingActivitiesJSON = fileContentJSON.getJSONArray("allDrinkingActivities")
                    for (i in 0 until allDrinkingActivitiesJSON.length()) {
                        val drinkingActivityJSON = allDrinkingActivitiesJSON.getJSONObject(i)
                        allDrinkingActivities.add(fromJSON(drinkingActivityJSON))
                    }
                }
            }
            catch(e:JSONException){
                out.println(e.stackTrace)
            }


        }

        fun fromJSON(drinkingActivityJSON: JSONObject):DrinkingActivity{
            var drinkingActivity= DrinkingActivity()
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



        fun setPrices(){
            if (allDrinkingActivities.count()>0) {
                for (activity in allDrinkingActivities.reversed()) {
                    for (typeOfBeer in TypeOfBeer.values()) {
                        if (activity.prices.containsKey(typeOfBeer) && (activity.prices[typeOfBeer]!! > 0.0)) {
                            allDrinkingActivities.last().prices[typeOfBeer] = activity.prices[typeOfBeer]!!.toDouble()
                        }
                    }
                    if (allDrinkingActivities.last().prices.all { it.value > 0.0 })
                        break;
                }
            }
        }

        fun getNumberOfBeersByMonth() : MutableMap<Pair<String,String>,Int> {
            var monthstats = mutableMapOf<Pair<String,String>,Int>()
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
                val monthString = DateFormat.format("MMM", activity.startOfDrinking) as String
                val monthNumber = DateFormat.format("MM", activity.startOfDrinking) as String
                val year = DateFormat.format("yyyy", activity.startOfDrinking) as String
                val beersDrankInActivity:Int=activity.drankBeers.values.sum()
                monthstats[Pair(monthNumber,year)] = beersDrankInActivity + monthstats[Pair(monthNumber,year)]!!
            }
            return monthstats
        }
    }



}





