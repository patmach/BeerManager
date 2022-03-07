package com.example.beermanager

import android.content.Context
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.beermanager.data.DrinkingActivity
import com.example.beermanager.data.DrinkingActivity.Companion.allDrinkingActivities
import com.example.beermanager.data.DrinkingActivity.Companion.loadAllDrinkingActivities
import com.example.beermanager.data.DrinkingActivity.Companion.saveAllDrinkingActivities
import com.example.beermanager.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {
    companion object{
        /**
         * Current drinking activity showed to user and controlled by user.
         */
        var currentDrinkingActivity:DrinkingActivity= DrinkingActivity();

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
        loadAllDrinkingActivities()
        val lastDrinkLessThan5HoursAgo = allDrinkingActivities.any()
        {
            (Date().time-it.lastDrink.time)/(1000*60*60)<=5
        }
        if (lastDrinkLessThan5HoursAgo) {
            currentDrinkingActivity = allDrinkingActivities.last()
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

    override fun onDestroy() {
        super.onDestroy()
        saveAllDrinkingActivities()
    }


}