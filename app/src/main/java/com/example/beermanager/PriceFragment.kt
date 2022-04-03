package com.example.beermanager

import android.content.res.Resources
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
            priceViewModel.savePrices()
            this.dismiss()
        })
        priceViewModel.layoutsLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is ViewModelResponse.Error -> Toast.makeText(context, it.error, Toast.LENGTH_LONG).show()
                is ViewModelResponse.Success -> LoadForAllTypesOfBeer(it.content)
            }
        }
        priceViewModel.getLayoutsAndEditTexts()
    }

    /**
     * Creates layout that contains textview and edittext for each type of beer
     */
    fun LoadForAllTypesOfBeer(newLayouts: MutableMap<TypeOfBeer,LinearLayout>){
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
    }




}

