package com.example.project_pockettindahan

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SettingsScreen()
        }
    }
}

// --- HELPER TO SAVE SETTINGS ---
class PreferencesManager(context: Context) {
    private val prefs = context.getSharedPreferences("PocketTindahanSettings", Context.MODE_PRIVATE)

    fun saveDarkMode(isDark: Boolean) {
        prefs.edit().putBoolean("DARK_MODE", isDark).apply()
    }

    fun isDarkMode(): Boolean {
        return prefs.getBoolean("DARK_MODE", false)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val prefsManager = remember { PreferencesManager(context) }

    // Load the saved dark mode state
    var isDarkMode by remember { mutableStateOf(prefsManager.isDarkMode()) }

    // Language state variables
    var showLanguageDialog by remember { mutableStateOf(false) }

    val localeList = AppCompatDelegate.getApplicationLocales()

    var selectedLanguage by remember(localeList) {
        mutableStateOf(if (localeList.toLanguageTags().contains("tl")) "Tagalog" else "English")
    }

    // FIXED: We now use the full path 'java.util.Locale' to bypass the auto-import bug
    val localizedContext = remember(localeList) {
        val localeTag = if (localeList.toLanguageTags().contains("tl")) "tl" else "en"
        val locale = java.util.Locale(localeTag)
        java.util.Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.createConfigurationContext(config)
    }

    val lightColors = lightColorScheme(surface = Color.White, onSurface = Color.Black, background = Color(0xFFF5F5F5))
    val darkColors = darkColorScheme(surface = Color(0xFF1E1E1E), onSurface = Color.White, background = Color(0xFF121212))

    CompositionLocalProvider(LocalContext provides localizedContext) {
        MaterialTheme(colorScheme = if (isDarkMode) darkColors else lightColors) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Surface(shape = CircleShape, color = Color.White, modifier = Modifier.size(45.dp)) {
                                Image(painter = painterResource(id = R.drawable.img), contentDescription = "Logo", modifier = Modifier.fillMaxSize().padding(5.dp))
                            }
                        },
                        modifier = Modifier.shadow(elevation = 8.dp),
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = colorResource(id = R.color.darkBlue))
                    )
                }
            ) { innerPadding ->
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding).background(colorResource(id = R.color.darkBlue)).padding(16.dp)) {
                    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface).border(4.dp, colorResource(id = R.color.darkBlue))) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(16.dp).shadow(6.dp, RoundedCornerShape(0.dp)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(0.dp)
                        ) {
                            Column {
                                // --- LANGUAGE ROW ---
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showLanguageDialog = true }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Language", tint = colorResource(id = R.color.darkBlue), modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Text(text = stringResource(id = R.string.label_language), fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                    Text(text = selectedLanguage, fontSize = 14.sp, color = Color.Gray)
                                }

                                HorizontalDivider(color = Color.LightGray, thickness = 2.dp)

                                // --- DARK MODE ROW ---
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Spacer(modifier = Modifier.width(40.dp))
                                        Text(text = stringResource(id = R.string.label_dark_mode), fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                    Switch(
                                        checked = isDarkMode,
                                        onCheckedChange = {
                                            isDarkMode = it
                                            prefsManager.saveDarkMode(it)
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White, checkedTrackColor = colorResource(id = R.color.darkBlue),
                                            uncheckedThumbColor = Color.Gray, uncheckedTrackColor = Color.LightGray, uncheckedBorderColor = Color.Transparent
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- LANGUAGE SELECTION POPUP DIALOG ---
            if (showLanguageDialog) {
                AlertDialog(
                    onDismissRequest = { showLanguageDialog = false },
                    title = { Text(text = stringResource(id = R.string.dialog_select_language), color = colorResource(id = R.color.darkBlue), fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            // English Selection Option
                            Row(modifier = Modifier.fillMaxWidth().clickable {
                                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
                                showLanguageDialog = false
                            }.padding(vertical = 12.dp)) {
                                Text(text = "•  ${stringResource(id = R.string.lang_english)}", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                            }

                            // Tagalog Selection Option
                            Row(modifier = Modifier.fillMaxWidth().clickable {
                                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("tl"))
                                showLanguageDialog = false
                            }.padding(vertical = 12.dp)) {
                                Text(text = "•  ${stringResource(id = R.string.lang_tagalog)}", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showLanguageDialog = false }) {
                            Text(text = stringResource(id = R.string.btn_cancel), color = Color.Gray)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface
                )
            }
        }
    }
}