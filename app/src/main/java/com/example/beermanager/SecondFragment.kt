package com.example.beermanager

import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.beermanager.MainActivity.Companion.currentDrinkingActivity
import com.example.beermanager.databinding.FragmentSecondBinding
import java.util.*

import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.app.AlertDialog
import com.example.beermanager.MainActivity.Companion.loadLastCanvas
import com.example.beermanager.data.DrinkingActivity
import android.content.DialogInterface
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import com.example.beermanager.MyCanvasView.Companion.pathList
import com.example.beermanager.PriceFragment
import com.example.beermanager.data.TypeOfBeer
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {
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

        /**
         * The text view for beer count in this fragment.
         */
        var textViewBeerCount:TextView?= null

        /**
         * The text view for fullprice in this fragment.
         */
        var textViewPrices:TextView?= null
    }

    private var _binding: FragmentSecondBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        startMinuteUpdater()
        binding.textviewBeerCount.text= "  " + currentDrinkingActivity.getNumberOfBeers()
        val fullPrice= currentDrinkingActivity.getFullPrice()
        if(fullPrice>0.0){
            binding.textviewPrice.visibility=VISIBLE
            binding.textviewPrice.text= fullPrice.toString()
        }
        else{
            binding.textviewPrice.visibility=INVISIBLE
        }
        textViewBeerCount = binding.textviewBeerCount
        textViewPrices=binding.textviewPrice
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
        binding.textviewTime.textSize = 20F
        binding.textviewBeerCount.textSize = 40F
        binding.textviewPrice.textSize = 30F

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
        if(currentDrinkingActivity.startOfDrinking!=Date(0)) {
            val diff: Long = Date().time - currentDrinkingActivity.startOfDrinking.time
            val seconds = diff / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            timeOfDrinking= String.format("%02d:%02d", hours, minutes)
        }
        binding.textviewTime.text=timeOfDrinking;
        refresh(60000)
    }

    /**
     * Used by the minute update method.
     */
    fun refresh(miliseconds:Int){
        Handler(Looper.getMainLooper()).postDelayed({
            startMinuteUpdater()
        }, miliseconds.toLong())
    }

    /**
     * Shows dialog before starting new drinking sessions. And potentially handles creating new drinking session.
     */
    fun confirmingDialogNewDrinking(): AlertDialog? {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setMessage("Are you sure you want to start new drinking?\n You will not be able return to the previous one")
            .setTitle("CONFIRM")
            .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, id ->
                currentDrinkingActivity= DrinkingActivity()
                loadLastCanvas=false
                binding.textviewBeerCount.text= "  " + currentDrinkingActivity.getNumberOfBeers()
                val fullPrice= currentDrinkingActivity.getFullPrice()
                if(fullPrice>0.0){
                    binding.textviewPrice.visibility=VISIBLE
                    binding.textviewPrice.text= " " + fullPrice.toString()
                }
                else{
                    binding.textviewPrice.visibility=INVISIBLE
                }
                binding.textviewTime.text="00:00"
                pathList.clear()
             })
            .setNegativeButton("No", DialogInterface.OnClickListener { dialog, id ->
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
            values.add(typeOfBeer.toString())
        }
        picker.maxValue=values.size-1
        picker.minValue=0
        picker.displayedValues= values.toTypedArray()
        picker.value=TypeOfBeer.values().indexOf(currentDrinkingActivity.currentTypeOfBeer)
        picker.setOnValueChangedListener {picker, oldVal, newVal ->
            currentDrinkingActivity.currentTypeOfBeer= TypeOfBeer.values()[newVal]
        }
    }

}