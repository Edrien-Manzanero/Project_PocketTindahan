package com.example.project_pockettindahan

import AppDatabase
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import com.example.project_pockettindahan.ui.theme.PT_DarkBlue
import com.example.project_pockettindahan.ui.theme.PT_RedAccent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardActivity : AppCompatActivity () {

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "pocket-tindahan-db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    private val isDarkModeState = mutableStateOf(false)
    //lang
    private val appLocalesState = mutableStateOf(AppCompatDelegate.getApplicationLocales())
    private lateinit var prefs: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = PreferencesManager(this)
        isDarkModeState.value = prefs.isDarkMode()

        setContent {
            val context = LocalContext.current
            val currentLocales = appLocalesState.value

            val localizedContext: android.content.Context = remember(currentLocales) {
                val localeTag = if (currentLocales.toLanguageTags().contains("tl")) "tl" else "en"
                val locale = java.util.Locale(localeTag)
                java.util.Locale.setDefault(locale)

                val config = Configuration(context.resources.configuration)
                config.setLocale(locale)

                context.createConfigurationContext(config)
            }

            val lightColors = lightColorScheme(
                surface = Color.White,
                onSurface = PT_DarkBlue,
                background = Color(0xFFF5F5F5)
            )
            val darkColors = darkColorScheme(
                surface = Color(0xFF1E1E1E),
                onSurface = Color.White,
                background = Color(0xFF121212)
            )

            // PLUGGED IN: We wrap the theme setup inside the CompositionLocalProvider
            CompositionLocalProvider(LocalContext provides localizedContext) {
                MaterialTheme(colorScheme = if (isDarkModeState.value) darkColors else lightColors) {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        DashboardScreen(
                            db = db,
                            onNavigate = { storeName ->
                                when (storeName) {
                                    "Inventory" -> startActivity(Intent(this, InventoryActivity::class.java))
                                    "Sales History" -> startActivity(Intent(this, SalesHistoryActivity::class.java))
                                    "Products" -> startActivity(Intent(this, ProductsActivity::class.java))
                                    "Debts and Due" -> startActivity(Intent(this, DebtsAndDueActivity::class.java))
                                    "Calculator" -> startActivity(Intent(this, CalculatorActivity::class.java))
                                    "Settings" -> startActivity(Intent(this, SettingsActivity::class.java))
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::prefs.isInitialized) {
            isDarkModeState.value = prefs.isDarkMode()
        }
        // PLUGGED IN: Force update the locale state snapshot when returning from Settings
        appLocalesState.value = AppCompatDelegate.getApplicationLocales()
    }
}

@Composable
fun DashboardScreen(db: AppDatabase, onNavigate: (String) -> Unit) {

    val allSales by db.SalesDao().getAll().collectAsState(initial = emptyList())

    val sdfDate = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).apply {
        timeZone = java.util.TimeZone.getTimeZone("Asia/Manila")
    }
    val todayDateString = sdfDate.format(Date())

    val todaysSalesList = allSales.filter { it.salesDate == todayDateString }
    val realTotalSales = todaysSalesList.sumOf { it.salesTotalSales ?: 0 }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            Surface(
                color = PT_DarkBlue,
                modifier = Modifier.fillMaxWidth().height(80.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_tindahan_dashboard),
                    contentDescription = "logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(all = 10.dp)
                )
            }
        },
        bottomBar = {
            Surface(
                color = PT_DarkBlue,
                modifier = Modifier.fillMaxWidth().height(80.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_todays_sales),
                        contentDescription = "Today's Sales",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    // PLUGGED IN: Pulls "Today's Sales" or "Benta Ngayong Araw" dynamically from XML
                    Text(
                        text = "${stringResource(id = R.string.todays_sale)}: ₱ $realTotalSales.00",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                }
            }
        }
    ) { innerPadding ->
        DashboardGrid(
            modifier = Modifier.padding(innerPadding),
            onItemClick = onNavigate
        )
    }
}

@Composable
fun MenuButton(label: String, iconId: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        border = BorderStroke(width = 4.dp, color = PT_RedAccent),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onClick() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = iconId),
                contentDescription = label,
                modifier = Modifier.size(48.dp)
            )

            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun DashboardGrid(onItemClick: (String) -> Unit, modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        item { MenuButton(label = stringResource(id = R.string.inventory), iconId = R.drawable.ic_inventory, onClick = { onItemClick("Inventory") }) }
        item { MenuButton(label = stringResource(id = R.string.Sales_history), iconId = R.drawable.ic_sales_history, onClick = { onItemClick("Sales History") }) }
        item { MenuButton(label = stringResource(id = R.string.Products), iconId = R.drawable.ic_products, onClick = { onItemClick("Products") }) }
        item { MenuButton(label = stringResource(id = R.string.Debts_and_due), iconId = R.drawable.ic_debts_and_due, onClick = { onItemClick("Debts and Due") }) }
        item { MenuButton(label = stringResource(id = R.string.Calculator), iconId = R.drawable.ic_calculator, onClick = { onItemClick("Calculator") }) }
        item { MenuButton(label = stringResource(id = R.string.Settings), iconId = R.drawable.ic_settings, onClick = { onItemClick("Settings") }) }
    }
}