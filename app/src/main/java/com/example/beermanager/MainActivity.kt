package com.example.beermanager

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import android.view.Menu
import android.view.MenuItem
import com.example.beermanager.data.DrinkingSession
import com.example.beermanager.data.DrinkingSession.Companion.drinkingSessionProcessor
import com.example.beermanager.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {
    companion object{
        /**
         * Current drinking activity showed to user and controlled by user.
         */
        var currentDrinkingSession:DrinkingSession= DrinkingSession()

        /**
         * Used for writing and reading files in app.
         */
        var fileContext: Context? =null

        /**
         * Specifies if canvas of last run of the app should be loaded to current canvas.
         */
        var loadLastCanvas=false
    }
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fileContext=applicationContext
        drinkingSessionProcessor.loadAllDrinkingSessions()
        val lastDrinkLessThan5HoursAgo = drinkingSessionProcessor.allDrinkingSessions.any()
        {
            (Date().time-it.lastDrink.time)/(1000*60*60)<=5
        }
        if (lastDrinkLessThan5HoursAgo) {
            currentDrinkingSession = drinkingSessionProcessor.allDrinkingSessions.last()
            loadLastCanvas=true
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onPause() {
        super.onPause()
        drinkingSessionProcessor.saveAllDrinkingSessions()
    }


}