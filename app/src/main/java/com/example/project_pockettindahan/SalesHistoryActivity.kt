package com.example.project_pockettindahan

// Ensure these match your actual files
import AppDatabase
import Sales
import SalesItem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SalesHistoryActivity : ComponentActivity() {

    // Initialize your database
    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "pocket-tindahan-db"
        )
            .fallbackToDestructiveMigration() // IMPORTANT: Add this since we changed the database version!
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SalesScreen(db)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(db: AppDatabase) {
    val salesList by db.SalesDao().getAll().collectAsState(initial = emptyList())

    var selectedTab by remember { mutableStateOf("Daily") }
    val tabs = listOf("Daily", "Weekly", "Monthly", "Annually")

    // State to track which sale to show in the popup
    var saleToView by remember { mutableStateOf<Sales?>(null) }

    val sdfOut = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
    val currentDateDisplay = sdfOut.format(Date())

    val totalSales = salesList.sumOf { it.salesTotalSales ?: 0 }
    val totalItemsSold = salesList.sumOf { it.salesTotalsold ?: 0 }
    val transactionsCount = salesList.size
    val totalProfit = salesList.sumOf { it.salesProfit ?: 0 }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier.size(45.dp)
                    ) {
                        // --- RESTORED THE IMAGE HERE ---
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
                .background(Color(0xFFF5F5F5))
                .padding(15.dp)
                .border(4.dp, colorResource(id = R.color.darkBlue), RoundedCornerShape(16.dp)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Sales",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colorResource(id = R.color.darkBlue),
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // --- SUMMARY CARD ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color.LightGray)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE0E0E0))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Today's Sales",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colorResource(id = R.color.darkBlue)
                        )
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        SummaryRow(label = "Date:", value = currentDateDisplay)
                        SummaryRow(label = "Total Sales:", value = "₱ $totalSales.00")
                        SummaryRow(label = "Total Items Sold:", value = "$totalItemsSold")
                        SummaryRow(label = "Transactions:", value = "$transactionsCount")
                        SummaryRow(label = "Total Profit:", value = "₱ $totalProfit.00")
                    }
                }
            }

            // --- TABS ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                tabs.forEach { tab ->
                    val isSelected = selectedTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = if (isSelected) colorResource(id = R.color.darkBlue) else Color.White,
                                shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) colorResource(id = R.color.darkBlue) else Color.LightGray,
                                shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                            )
                            .clickable { selectedTab = tab }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            color = if (isSelected) Color.White else colorResource(id = R.color.darkBlue),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- TRANSACTIONS LIST ---
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White)
                    .border(1.dp, Color.LightGray)
            ) {
                if (salesList.isEmpty()) {
                    item {
                        Text(
                            text = "No sales recorded yet.",
                            color = Color.Gray,
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    items(salesList) { sale ->
                        // Pass the sale up when clicked
                        TransactionRow(sale = sale, onViewClick = { saleToView = sale })
                        HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
                    }
                }
            }
        }
    }

    // --- RECEIPT POPUP ---
    saleToView?.let { sale ->
        ReceiptDialog(
            db = db,
            sale = sale,
            onDismiss = { saleToView = null } // Closes the popup
        )
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.padding(bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = colorResource(id = R.color.darkBlue),
            fontSize = 14.sp,
            modifier = Modifier.width(130.dp)
        )
        Text(
            text = value,
            color = colorResource(id = R.color.darkBlue),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp
        )
    }
}

// Updated to accept the onClick action
@Composable
fun TransactionRow(sale: Sales, onViewClick: () -> Unit) {
    val formattedId = String.format("%03d", sale.sales_id)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${sale.salesTime ?: "Unknown Time"} | Sale #$formattedId",
            color = colorResource(id = R.color.darkBlue),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "₱ ${sale.salesTotalSales ?: 0}.00",
                color = colorResource(id = R.color.darkBlue),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = onViewClick, // Triggers the popup
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.darkBlue)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Text(text = "View", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- NEW RECEIPT DIALOG ---
@Composable
fun ReceiptDialog(db: AppDatabase, sale: Sales, onDismiss: () -> Unit) {
    // State to hold the items we fetch from the database
    var receiptItems by remember { mutableStateOf<List<SalesItem>>(emptyList()) }
    val formattedId = String.format("%03d", sale.sales_id)

    // This block automatically runs in the background to fetch the specific items for this sale
    LaunchedEffect(sale.sales_id) {
        withContext(Dispatchers.IO) {
            receiptItems = db.SalesItemDao().getItemsForSale(sale.sales_id)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(4.dp, colorResource(id = R.color.darkBlue)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {

                // Receipt Header
                Text(
                    text = "Receipt #$formattedId",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colorResource(id = R.color.darkBlue),
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

                HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable List of Items
                LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                    if (receiptItems.isEmpty()) {
                        item {
                            Text(
                                text = "Loading items...",
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
                                    Text(text = item.itemName, fontWeight = FontWeight.Bold, color = colorResource(id = R.color.darkBlue))
                                    Text(text = "${item.quantity}x @ ₱${item.pricePerUnit}.00", color = Color.Gray, fontSize = 12.sp)
                                }
                                Text(
                                    text = "₱${item.quantity * item.pricePerUnit}.00",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colorResource(id = R.color.darkBlue)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                // Total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Paid:", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colorResource(id = R.color.darkBlue))
                    Text("₱${sale.salesTotalSales}.00", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4CAF50))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Close Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.darkBlue)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(text = "Close", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}