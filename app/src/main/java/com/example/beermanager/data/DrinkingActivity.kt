package com.example.beermanager.data
import android.content.Context.MODE_PRIVATE
import com.example.beermanager.SecondFragment.Companion.textViewBeerCount
import org.json.JSONObject
import java.io.File
import java.util.*
import kotlin.collections.*
import com.example.beermanager.MainActivity.Companion.fileContext
import org.json.JSONException
import java.io.IOException
import java.io.InputStreamReader
import java.lang.System.out


class DrinkingActivity {
    var startOfDrinking: Date = Date(0);
    var timeOfFirstAlcoholicBeer: Date = Date(0);
    var drinkedBeers: MutableMap<TypeOfBeer, Int> = mutableMapOf<TypeOfBeer,Int>()
    var timeOfLastAlcoholicBeer: Date = Date(0);
    var lastDrink: Date = Date(0);

    var currentTypeOfBeer:TypeOfBeer= TypeOfBeer.ELEVEN
    init{
        for (typeOfBeer in TypeOfBeer.values()) {
            drinkedBeers[typeOfBeer] = 0
        }
    }

    fun getNumberOfBeers(): Int{
        return drinkedBeers.values.sum()
    }
    fun addBeer(){
        drinkedBeers[currentTypeOfBeer]= drinkedBeers.getOrDefault(currentTypeOfBeer,0) + 1;
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
        textViewBeerCount?.text ="  "+ getNumberOfBeers()
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
            jsonString+="\"${drinkedBeers[typeOfBeer]}\""
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
                val key= drinkingActivity.drinkedBeers.keys.elementAt(i)
                drinkingActivity.drinkedBeers[key] = drinkedBeersJSON.getInt(i)
            }
            return drinkingActivity
        }
    }
}





