package com.example.project_pockettindahan

import AppDatabase
import Sales
import SalesItem
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow

class SalesHistoryActivity : AppCompatActivity() {

    private val isDarkModeState = mutableStateOf(false)
    private val appLocalesState = mutableStateOf(AppCompatDelegate.getApplicationLocales())
    // FIXED: Added state to track the font scale
    private val fontScaleState = mutableStateOf(1.0f)
    private lateinit var prefs: PreferencesManager

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "pocket-tindahan-db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = PreferencesManager(this)
        isDarkModeState.value = prefs.isDarkMode()
        fontScaleState.value = prefs.getFontScale() // Read saved font scale

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

            // FIXED: Grab current density and apply the font scale multiplier
            val currentDensity = LocalDensity.current
            val customDensity = Density(density = currentDensity.density, fontScale = fontScaleState.value)

            val lightColors = lightColorScheme(
                surface = Color.White,
                onSurface = colorResource(id = R.color.darkBlue),
                background = Color(0xFFF5F5F5)
            )
            val darkColors = darkColorScheme(
                surface = Color(0xFF1E1E1E),
                onSurface = Color.White,
                background = Color(0xFF121212)
            )

            // FIXED: Provided both LocalContext (for language) and LocalDensity (for font size)
            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalDensity provides customDensity
            ) {
                MaterialTheme(colorScheme = if (isDarkModeState.value) darkColors else lightColors) {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        SalesScreen(db)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::prefs.isInitialized) {
            isDarkModeState.value = prefs.isDarkMode()
            fontScaleState.value = prefs.getFontScale() // FIXED: Instantly refresh font scale on return
        }
        appLocalesState.value = AppCompatDelegate.getApplicationLocales()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(db: AppDatabase) {

    val salesList by db.SalesDao().getAll().collectAsState(initial = emptyList())

    var selectedTab by remember { mutableStateOf("Daily") }
    val tabs = listOf(
        "Daily" to R.string.tab_daily,
        "Weekly" to R.string.tab_weekly,
        "Monthly" to R.string.tab_monthly,
        "Annually" to R.string.tab_annually
    )

    var saleToView by remember { mutableStateOf<Sales?>(null) }

    val sdfOut = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
    val currentDateDisplay = sdfOut.format(Date())

    val totalSales = salesList.sumOf { it.salesTotalSales ?: 0 }
    val totalItemsSold = salesList.sumOf { it.salesTotalsold ?: 0 }
    val transactionsCount = salesList.size
    val totalProfit = salesList.sumOf { it.salesProfit ?: 0 }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Surface(
                        shape = CircleShape,
                        color = Color.Transparent,
                        modifier = Modifier.size(45.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img),
                            contentDescription = "Logo",
                            modifier = Modifier.fillMaxSize().padding(5.dp)
                        )
                    }
                },
                modifier = Modifier.shadow(elevation = 8.dp),
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colorResource(id = R.color.darkBlue)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(15.dp)
                .border(4.dp, colorResource(id = R.color.darkBlue), RoundedCornerShape(16.dp)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = stringResource(id = R.string.sales_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.Gray)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (MaterialTheme.colorScheme.background == Color(0xFF121212)) Color.DarkGray else Color(0xFFE0E0E0))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.todays_sales),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        SummaryRow(label = stringResource(id = R.string.date_label), value = currentDateDisplay)
                        SummaryRow(label = stringResource(id = R.string.total_sales_label), value = "₱ $totalSales.00")
                        SummaryRow(label = stringResource(id = R.string.total_items_sold_label), value = "$totalItemsSold")
                        SummaryRow(label = stringResource(id = R.string.transactions_label), value = "$transactionsCount")
                        SummaryRow(label = stringResource(id = R.string.total_profit_label), value = "₱ $totalProfit.00")
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                tabs.forEach { (tabKey, tabResId) ->
                    val isSelected = selectedTab == tabKey
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = if (isSelected) colorResource(id = R.color.darkBlue) else MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) colorResource(id = R.color.darkBlue) else Color.Gray,
                                shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                            )
                            .clickable { selectedTab = tabKey }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = tabResId),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, Color.Gray)
            ) {
                if (salesList.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(id = R.string.no_sales_recorded),
                            color = Color.Gray,
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    items(salesList) { sale ->
                        TransactionRow(sale = sale, onViewClick = { saleToView = sale })
                        HorizontalDivider(color = Color.Gray, thickness = 1.dp)
                    }
                }
            }
        }
    }

    saleToView?.let { sale ->
        ReceiptDialog(
            db = db,
            sale = sale,
            onDismiss = { saleToView = null }
        )
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(
        // FIXED: Added fillMaxWidth so it stretches to the edges
        modifier = Modifier.padding(bottom = 4.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            // FIXED: Removed the hardcoded 135.dp width and replaced it with weight.
            // This prevents "Transactions:" from breaking into two lines!
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun TransactionRow(sale: Sales, onViewClick: () -> Unit) {
    val formattedId = String.format("%03d", sale.sales_id)
    val fallbackTime = stringResource(id = R.string.unknown_time)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            // NEW: Adds the Double Tap gesture to the entire row!
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = { onViewClick() })
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${sale.salesTime ?: fallbackTime} | Sale #$formattedId",
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp,
            // FIXED: Adding weight(1f) stops this text from pushing the button off-screen.
            // It will now truncate with an ellipsis (...) if it gets too long.
            modifier = Modifier.weight(1f).padding(end = 8.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "₱ ${sale.salesTotalSales ?: 0}.00",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = onViewClick,
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.darkBlue)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Text(text = stringResource(id = R.string.view_action), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ReceiptDialog(db: AppDatabase, sale: Sales, onDismiss: () -> Unit) {
    var receiptItems by remember { mutableStateOf<List<SalesItem>>(emptyList()) }
    val formattedId = String.format("%03d", sale.sales_id)

    LaunchedEffect(sale.sales_id) {
        withContext(Dispatchers.IO) {
            receiptItems = db.SalesItemDao().getItemsForSale(sale.sales_id)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(4.dp, colorResource(id = R.color.darkBlue)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {

                Text(
                    text = stringResource(id = R.string.receipt_title, formattedId),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${sale.salesDate} at ${sale.salesTime}",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )

                HorizontalDivider(color = Color.Gray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                    if (receiptItems.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(id = R.string.loading_items),
                                color = Color.Gray,
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        items(receiptItems) { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = item.itemName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text(text = "${item.quantity}x @ ₱${item.pricePerUnit}.00", color = Color.Gray, fontSize = 12.sp)
                                }
                                Text(
                                    text = "₱${item.quantity * item.pricePerUnit}.00",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.Gray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(id = R.string.total_paid_label), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("₱${sale.salesTotalSales}.00", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4CAF50))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.darkBlue)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(text = stringResource(id = R.string.close_action), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}