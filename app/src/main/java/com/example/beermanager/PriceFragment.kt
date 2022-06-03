package com.example.beermanager

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
import androidx.fragment.app.viewModels
import com.example.beermanager.MainActivity.Companion.currentDrinkingSession

import com.example.beermanager.data.TypeOfBeer
import com.example.beermanager.data.ViewModelResponse
import com.example.beermanager.viewmodels.PriceViewModel

/**
 *  Controls views of Price Fragment layout and performs required actions.
 */
class PriceFragment: DialogFragment() {
    private val priceViewModel by viewModels<PriceViewModel>()
    private var _binding: PriceFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    /**
     * Contains all edittexts displayed in fragment (one for each type of beer)
     */
    private var editTextPrices = ArrayList<EditText>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = PriceFragmentBinding.inflate(inflater, container, false)

        return binding.root

    }

    override fun onStart() {
        super.onStart()
        editTextPrices.clear()
        binding.buttonSetPrices.setOnClickListener(View.OnClickListener {
            var prices= ArrayList<String>()
            for (editText in editTextPrices){
                prices.add(editText.text.toString());
            }
            priceViewModel.savePrices(prices)
            this.dismiss()
        })
        priceViewModel.pricesLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is ViewModelResponse.Error -> Toast.makeText(context, it.error, Toast.LENGTH_LONG).show()
                is ViewModelResponse.Success -> setPrices(it.content)
            }
        }
        LoadForAllTypesOfBeer()
        priceViewModel.loadPrices();
    }

    private fun setPrices(prices: MutableMap<TypeOfBeer, String>) {
        var index = 0
        for (typeOfBeer in TypeOfBeer.values()){
            editTextPrices[index].setText(prices[typeOfBeer]);
            index++;
        }
    }

    /**
     * Creates layout that contains textview and edittext for each type of beer
     */
    fun LoadForAllTypesOfBeer(){
        val mainLayout = binding.mainlayout
        //var count = 0

        for (typeOfBeer in TypeOfBeer.values()){
            val p = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            val id=View.generateViewId();
            val newLinearLayout= LinearLayout(context)
            newLinearLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,0)
            newLinearLayout.gravity=Gravity.CENTER
            newLinearLayout.id=id

            val textView= TextView(context)
            textView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT)
            textView.text=typeOfBeer.toStringWithNumber()

            val editText=EditText(context);
            editText.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT)
            editText.minEms=10
            editText.inputType=InputType.TYPE_NUMBER_FLAG_DECIMAL
            if(currentDrinkingSession.prices[typeOfBeer]!! >0.0){
                editText.setText(currentDrinkingSession.prices[typeOfBeer].toString())

            }
            editTextPrices.add(editText)

            newLinearLayout.post(Runnable { newLinearLayout.addView(textView,p) })
            newLinearLayout.post(Runnable { newLinearLayout.addView(editText,p) })
            mainLayout.post(Runnable {
                mainLayout.addView(newLinearLayout,p)
                if (mainLayout.children.count()>1) {
                    (newLinearLayout.layoutParams as RelativeLayout.LayoutParams).addRule(
                        RelativeLayout.BELOW,
                        mainLayout.children.elementAt(mainLayout.children.count() - 2).id
                    )
                }
            })
        }
    }
  /*  fun LoadForAllTypesOfBeer(newLayouts: MutableMap<TypeOfBeer,LinearLayout>){
        val mainLayout = binding.mainlayout
        //var count = 0

        for (typeOfBeer in TypeOfBeer.values()){
            val newLinearLayout = newLayouts[typeOfBeer]
            val p = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            mainLayout.post(Runnable {
                mainLayout.addView(newLinearLayout,p)
                if (mainLayout.children.count()>1) {
                    if (newLinearLayout != null) {
                        (newLinearLayout.layoutParams as RelativeLayout.LayoutParams).addRule(
                            RelativeLayout.BELOW,
                            mainLayout.children.elementAt(mainLayout.children.count() - 2).id
                        )
                    }
                }
            })
        }
    }*/




}

