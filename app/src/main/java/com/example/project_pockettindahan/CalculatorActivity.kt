package com.example.project_pockettindahan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.lifecycle.ViewModelProvider

class CalculatorActivity : ComponentActivity() {
    private val isDarkModeState = mutableStateOf(false)
    private lateinit var prefs: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferencesManager(this)
        isDarkModeState.value = prefs.isDarkMode()

        // This line links the Activity to your separate ViewModel file
        val viewModel = ViewModelProvider(this)[CalculatorViewModel::class.java]

        setContent {
            val lightColors = lightColorScheme(surface = Color.White, onSurface = colorResource(id = R.color.darkBlue), background = Color(0xFFF5F5F5))
            val darkColors = darkColorScheme(surface = Color(0xFF1E1E1E), onSurface = Color.White, background = Color(0xFF121212))

            MaterialTheme(colorScheme = if (isDarkModeState.value) darkColors else lightColors) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    // This calls the Screen defined in Calculator.kt
                    CalculatorScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::prefs.isInitialized) isDarkModeState.value = prefs.isDarkMode()
    }
}