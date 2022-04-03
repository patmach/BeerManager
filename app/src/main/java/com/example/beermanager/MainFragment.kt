package com.example.beermanager

import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.beermanager.MainActivity.Companion.currentDrinkingSession
import com.example.beermanager.databinding.FragmentMainBinding
import java.util.*

import android.os.Handler
import android.os.Looper
import android.app.AlertDialog
import com.example.beermanager.MainActivity.Companion.loadLastCanvas
import com.example.beermanager.data.DrinkingSession
import android.content.DialogInterface
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import com.example.beermanager.data.TypeOfBeer
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class MainFragment : Fragment() {
    companion object {
        /**
         * Stores path of user touch.
         */
        @JvmStatic
        var path = Path()

        /**
         * Stores type of paint to be used to record user touch on screen.
         */
        var paint = Paint()

    }

    private var _binding: FragmentMainBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var canvas : MyCanvasView? = null;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        startMinuteUpdater()
        startSecondUpdater()
        binding.buttonNewDrinking.setOnClickListener(View.OnClickListener {
            confirmingDialogNewDrinking()?.show()
        })
        binding.buttonSetPrices.setOnClickListener(View.OnClickListener {
            var priceFragment= PriceFragment()
            priceFragment.show(childFragmentManager,"")
        })
        binding.buttonMonthStats.setOnClickListener(View.OnClickListener {
            var statsFragment= StatsFragment()
            statsFragment.show(childFragmentManager,"")
        })
        binding.buttonAlcoholCalculator.setOnClickListener(View.OnClickListener {
            var calculatorFragment= CalculatorFragment()
            calculatorFragment.show(childFragmentManager,"")
        })
        setPicker()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Starts updater that passes every minute and updates the time of drinking textview
     */
    fun startMinuteUpdater() {
        var timeOfDrinking="00:00"
        if(currentDrinkingSession.startOfDrinking!=Date(0)) {
            val diff: Long = Date().time - currentDrinkingSession.startOfDrinking.time
            val seconds = diff / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            timeOfDrinking= String.format("%02d:%02d", hours, minutes - (hours*60))
        }
        if (_binding!=null)
            binding.textviewTime.text=timeOfDrinking;
        refreshTimeUpdater(60000)
    }

    fun startSecondUpdater(){
        if (_binding!=null) {
            var fullPrice = currentDrinkingSession.getFullPrice();
            if (fullPrice > 0) {
                binding.textviewPrice.text = " " + fullPrice.toString()
                binding.textviewPrice.visibility = VISIBLE;
            } else
                binding.textviewPrice.visibility = INVISIBLE
            binding.textviewBeerCount.text = "  " + currentDrinkingSession.getNumberOfBeers();
        }
        refreshBeerCountAndPriceUpdater(1000)

    }

    /**
     * Used by the minute update method.
     */
    fun refreshTimeUpdater(miliseconds:Int){
        Handler(Looper.getMainLooper()).postDelayed({
            startMinuteUpdater()
        }, miliseconds.toLong())
    }

    fun refreshBeerCountAndPriceUpdater(miliseconds:Int){
        Handler(Looper.getMainLooper()).postDelayed({
            startSecondUpdater()
        }, miliseconds.toLong())
    }

    /**
     * Shows dialog before starting new drinking sessions. And potentially handles creating new drinking session.
     */
    fun confirmingDialogNewDrinking(): AlertDialog? {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setMessage(getString(R.string.new_drinking_confirm_question))
            .setTitle(getString(R.string.confirm))
            .setPositiveButton(getString(R.string.yes), DialogInterface.OnClickListener { dialog, id ->
                currentDrinkingSession= DrinkingSession()
                loadLastCanvas=false
                binding.textviewBeerCount.text= "  " + currentDrinkingSession.getNumberOfBeers()
                val fullPrice= currentDrinkingSession.getFullPrice()
                if(fullPrice>0.0){
                    binding.textviewPrice.visibility=VISIBLE
                    binding.textviewPrice.text= " " + fullPrice.toString()
                }
                else{
                    binding.textviewPrice.visibility=INVISIBLE
                }
                binding.textviewTime.text="00:00"
                binding.myCanvasView.pathList.clear()
             })
            .setNegativeButton(getString(R.string.no), DialogInterface.OnClickListener { dialog, id ->
            })
        return builder.create()
    }

    /**
     * Load values for picker of types of beer
     */
    fun setPicker(){
        val picker = binding.pickerTypeOfBeer
        var values:ArrayList<String> = ArrayList()
        for (typeOfBeer in TypeOfBeer.values()){
            values.add(typeOfBeer.toStringWithNumber())
        }
        picker.maxValue=values.size-1
        picker.minValue=0
        picker.displayedValues= values.toTypedArray()
        picker.value=TypeOfBeer.values().indexOf(currentDrinkingSession.currentTypeOfBeer)
        picker.setOnValueChangedListener {picker, oldVal, newVal ->
            currentDrinkingSession.currentTypeOfBeer= TypeOfBeer.values()[newVal]
        }
    }

}