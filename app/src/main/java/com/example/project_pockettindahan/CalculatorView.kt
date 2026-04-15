package com.example.project_pockettindahan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable

class CalculatorViewModel : ViewModel() {
    private val _equationText = MutableLiveData("")
    val equationText: LiveData<String> = _equationText

    private val _resultText = MutableLiveData("0")
    val resultText: LiveData<String> = _resultText

    fun onButtonClick(btn: String) {
        _equationText.value?.let { current ->
            when (btn) {
                "AC" -> { _equationText.value = ""; _resultText.value = "0"; return }
                "C" -> { if (current.isNotEmpty()) _equationText.value = current.dropLast(1); return }
                "=" -> { if (_resultText.value != "Error") _equationText.value = _resultText.value; return }
                else -> _equationText.value = current + btn
            }
            try {
                _resultText.value = calculateResult(_equationText.value.toString())
            } catch (e: Exception) { }
        }
    }

    private fun calculateResult(equation: String): String {
        if (equation.isEmpty()) return "0"
        val context = Context.enter().apply { optimizationLevel = -1 }
        val scriptable: Scriptable = context.initStandardObjects()
        return try {
            var result = context.evaluateString(scriptable, equation.replace("x", "*").replace("\u00F7", "/"), "JS", 1, null).toString()
            if (result.endsWith(".0")) result = result.replace(".0", "")
            result
        } catch (e: Exception) { "Error" } finally { Context.exit() }
    }
}