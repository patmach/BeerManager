package com.example.beermanager.data

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.beermanager.MainActivity
import com.example.beermanager.SecondFragment
import com.example.beermanager.databinding.PriceFragmentBinding
import android.R
import android.provider.ContactsContract
import android.text.InputType
import android.view.Gravity
import android.widget.EditText

import android.widget.TextView

import android.widget.LinearLayout





class PriceFragment: DialogFragment() {
    private var _binding: PriceFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = PriceFragmentBinding.inflate(inflater, container, false)
        LoadForAllTypesOfBeer()
        /*button.setOnClickListener(View.OnClickListener {

        })*/
        return binding.root

    }

    fun LoadForAllTypesOfBeer(){
        val mainLayout = binding.mainLayout
        var count = 0
        var id=View.generateViewId();
        for (typeOfBeer in TypeOfBeer.values()){
            val newLinearLayout= LinearLayout(context)
            newLinearLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,0)
            newLinearLayout.gravity=Gravity.CENTER
            (newLinearLayout.layoutParams as LinearLayout.LayoutParams).weight=2F

            val textView= TextView(context)
            textView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT)
            textView.text=typeOfBeer.toString()

            val editText=EditText(context);
            editText.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT)
            editText.minEms=10
            editText.inputType=InputType.TYPE_NUMBER_FLAG_DECIMAL
            newLinearLayout.addView(textView)
            newLinearLayout.addView(editText)
            mainLayout.addView(newLinearLayout)

            count++
        }
    }
}