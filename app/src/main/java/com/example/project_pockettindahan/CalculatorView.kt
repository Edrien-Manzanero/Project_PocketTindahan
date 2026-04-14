package com.example.project_pockettindahan

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable

class CalculatorViewModel: ViewModel() {
    private val _equationText = MutableLiveData("")
    val equationText : LiveData<String> = _equationText

    private val _resultText = MutableLiveData("0")
    val resultText : LiveData<String> = _resultText
    fun onButtonClick(btn : String){
        Log.i("Clicked Button", btn)

        _equationText.value?.let {
            when (btn) {
                "AC" -> {
                    _equationText.value = ""
                    _resultText.value = "0"
                    return
                }
                "C" -> {
                    if (it.isNotEmpty()) {
                        _equationText.value = it.substring(0, it.length - 1)
                    }
                    return
                }
                "=" -> {
                    _equationText.value = _resultText.value
                    return
                }
                else -> {
                    _equationText.value = it + btn
                }
            }
            //_equationText.value = it + btn
            //Log.i("Equation", _equationtext.value.toString())

            try{
                _resultText.value = calculateResult(_equationText.value.toString())
            }catch (_ : Exception){

            }
        }
    }
    private fun preprocessEquation(equation: String): String {
        var processed = equation

        processed = processed.replace(Regex("(\\d)\\("), "$1*(")

        processed = processed.replace(Regex("\\)(\\d)"), ")*$1")

        processed = processed.replace(Regex("\\)\\("), ")*(")

        return processed
    }
    fun calculateResult(equation : String) : String {
        val context : Context = Context.enter()
        context.optimizationLevel = -1
        val scriptable : Scriptable = context.initStandardObjects()

        val processedEquation = preprocessEquation(equation)

        val finalResult = context.evaluateString(
            scriptable,
            processedEquation,
            "JavaScript",
            1,
            null
        ).toString()

        return finalResult
    }
}