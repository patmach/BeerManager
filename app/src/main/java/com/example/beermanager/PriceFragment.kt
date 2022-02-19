package com.example.beermanager.data

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.beermanager.databinding.PriceFragmentBinding
import android.text.InputType
import android.view.Gravity
import android.widget.*

import androidx.core.view.children
import com.example.beermanager.MainActivity.Companion.currentDrinkingActivity
import com.example.beermanager.SecondFragment.Companion.textViewPrices


class PriceFragment: DialogFragment() {
    private var _binding: PriceFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var editTextPrices = ArrayList<EditText>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = PriceFragmentBinding.inflate(inflater, container, false)
        editTextPrices.clear()
        LoadForAllTypesOfBeer()
        binding.buttonSetPrices.setOnClickListener(View.OnClickListener {
            savePrices()
        })
        return binding.root

    }

    fun LoadForAllTypesOfBeer(){
        val mainLayout = binding.mainlayout
        var count = 0
        var id=View.generateViewId();
        for (typeOfBeer in TypeOfBeer.values()){
            val p = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )




            val newLinearLayout= LinearLayout(context)
            newLinearLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,0)
            newLinearLayout.gravity=Gravity.CENTER
            newLinearLayout.id=1500+count //magic number
            //(newLinearLayout.layoutParams as LinearLayout.LayoutParams).weight=2F




            val textView= TextView(context)
            textView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT)
            textView.text=typeOfBeer.toString()

            val editText=EditText(context);
            editText.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT)
            editText.minEms=10
            editText.inputType=InputType.TYPE_NUMBER_FLAG_DECIMAL
            if(currentDrinkingActivity.prices[typeOfBeer]!! >0.0){
                editText.setText(currentDrinkingActivity.prices[typeOfBeer].toString())

            }
            editTextPrices.add(editText)

            /*newLinearLayout.addView(textView,p)
            newLinearLayout.addView(editText,p)*/
            newLinearLayout.post(Runnable { newLinearLayout.addView(textView,p) })
            newLinearLayout.post(Runnable { newLinearLayout.addView(editText,p) })
            //mainLayout.addView(newLinearLayout,p)
            //(newLinearLayout.parent as LinearLayout).removeView(newLinearLayout)
            mainLayout.post(Runnable {
                mainLayout.addView(newLinearLayout,p)
                if (mainLayout.children.count()>1) {
                    (newLinearLayout.layoutParams as RelativeLayout.LayoutParams).addRule(
                        RelativeLayout.BELOW,
                        mainLayout.children.elementAt(mainLayout.children.count() - 2).id
                    )
                }
            })

            count++
        }
    }

    fun savePrices() {
        try
        {
            var index = 0
            for (typeOfBeer in TypeOfBeer.values()) {
                val value = editTextPrices[index].text.toString()
                if (value != "")
                    currentDrinkingActivity.prices[typeOfBeer] = value.toDouble()
                index++
            }
            val fullPrice = currentDrinkingActivity.getFullPrice()
            if (fullPrice > 0.0) {
                textViewPrices?.text = " " + fullPrice.toString()
                textViewPrices?.visibility = View.VISIBLE
            } else {
                textViewPrices?.visibility = View.INVISIBLE
            }
        }
        catch (e:NumberFormatException){
            Toast.makeText(context, "Some of the values has wrong format!\n\nYou can use only numbers and decimal point!", Toast.LENGTH_LONG).show();
        }
        this.dismiss()
    }

    fun loadPrices() {
        var index=0
        for (typeOfBeer in TypeOfBeer.values()) {
            if (currentDrinkingActivity.prices.containsKey(typeOfBeer) && (currentDrinkingActivity.prices[typeOfBeer]!! > 0.0))
                editTextPrices[index].setText(currentDrinkingActivity.prices[typeOfBeer].toString())
            index++
        }
    }
}

