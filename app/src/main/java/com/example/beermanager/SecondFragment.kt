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
import android.R
import android.accessibilityservice.GestureDescription
import android.app.AlertDialog
import com.example.beermanager.MainActivity.Companion.loadLastCanvas
import com.example.beermanager.data.DrinkingActivity
import android.content.DialogInterface
import com.example.beermanager.MyCanvasView.Companion.pathList
import com.example.beermanager.data.PriceFragment
import com.example.beermanager.data.TypeOfBeer
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {
    companion object {
        @JvmStatic
        var path = Path()
        var paint = Paint()
        var textViewBeerCount:TextView?= null
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
        textViewBeerCount = binding.textviewBeerCount
        binding.buttonNewDrinking.setOnClickListener(View.OnClickListener {
            confirmingDialogNewDrinking()?.show()
        })
        binding.buttonSetPrices.setOnClickListener(View.OnClickListener {
            var priceFragment= PriceFragment()
            priceFragment.show(childFragmentManager,"")
        })
        setPicker()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.textviewTime.textSize = 20F
        binding.textviewBeerCount.textSize = 40F

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

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

    fun refresh(miliseconds:Int){
        Handler(Looper.getMainLooper()).postDelayed({
            startMinuteUpdater()
        }, miliseconds.toLong())
    }

    fun confirmingDialogNewDrinking(): AlertDialog? {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setMessage("Are you sure you want to start new drinking?\n You will not be able return to the previous one")
            .setTitle("CONFIRM")
            .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, id ->
                currentDrinkingActivity= DrinkingActivity()
                loadLastCanvas=false
                binding.textviewBeerCount.text= "  " + currentDrinkingActivity.getNumberOfBeers()
                binding.textviewTime.text="00:00"
                pathList.clear()
             })
            .setNegativeButton("No", DialogInterface.OnClickListener { dialog, id ->
            })
        // Create the AlertDialog object and return it
        // Create the AlertDialog object and return it
        return builder.create()
    }

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