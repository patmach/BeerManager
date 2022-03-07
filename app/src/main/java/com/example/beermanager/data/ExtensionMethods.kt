package com.example.beermanager.data

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment

/**
 * Hides keyboard in fragment.
 */
fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

/**
 * Hides keyboard in activity.
 */
    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

/**
 * Used by methods with same name for fragment and activity for hiding the keyboard
 */
    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
