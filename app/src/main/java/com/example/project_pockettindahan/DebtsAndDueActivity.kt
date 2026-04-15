package com.example.project_pockettindahan

import AppDatabase
import Debt
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DebtsAndDueActivity : ComponentActivity() {

    private val isDarkModeState = mutableStateOf(false)
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

        setContent {
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

            MaterialTheme(colorScheme = if (isDarkModeState.value) darkColors else lightColors) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    DebtsScreen(db)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::prefs.isInitialized) {
            isDarkModeState.value = prefs.isDarkMode()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtsScreen(db: AppDatabase) {
    val allDebts by db.DebtDao().getAll().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("Unpaid") }
    var showAddDialog by remember { mutableStateOf(false) }
    var debtToEdit by remember { mutableStateOf<Debt?>(null) }

    val filteredDebts = allDebts.filter { debt ->
        var actualStatus = debt.debtStatus
        if (actualStatus == "Unpaid" && !debt.debtDate.isNullOrEmpty()) {
            try {
                val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                val dueDate = sdf.parse(debt.debtDate)
                val todayDate = sdf.parse(sdf.format(Date()))
                if (dueDate != null && todayDate != null && dueDate.before(todayDate)) {
                    actualStatus = "Overdue"
                }
            } catch (e: Exception) {}
        }
        val matchesTab = actualStatus == selectedTab
        val matchesSearch = debt.debtName?.contains(searchQuery, ignoreCase = true) ?: false
        matchesTab && (searchQuery.isEmpty() || matchesSearch)
    }

    Scaffold(
        containerColor = Color.Transparent,
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
        },
        floatingActionButton = {
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = "Add Entry", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Debts/Dues", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp)),
                placeholder = { Text("Search Customer", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorResource(id = R.color.darkBlue),
                    cursorColor = colorResource(id = R.color.darkBlue),
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                val tabs = listOf("Unpaid", "Paid", "Overdue")
                tabs.forEachIndexed { index, tab ->
                    val isSelected = selectedTab == tab
                    val shape = when (index) {
                        0 -> RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
                        tabs.size - 1 -> RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
                        else -> RoundedCornerShape(0.dp)
                    }
                    Box(
                        modifier = Modifier.weight(1f).background(if (isSelected) colorResource(id = R.color.darkBlue) else MaterialTheme.colorScheme.surface, shape).border(1.dp, if (isSelected) colorResource(id = R.color.darkBlue) else Color.Gray, shape).clickable { selectedTab = tab }.padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = tab, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredDebts) { debt ->
                    DebtCard(
                        debt = debt,
                        onMarkPaid = { scope.launch(Dispatchers.IO) { db.DebtDao().update(debt.copy(debtStatus = "Paid")) } },
                        onEditClick = { debtToEdit = debt },
                        onDelete = { scope.launch(Dispatchers.IO) { db.DebtDao().delete(debt) } }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddDebtDialog(onDismiss = { showAddDialog = false }, onSave = { name, amt, date ->
            scope.launch(Dispatchers.IO) {
                db.DebtDao().insertAll(Debt(debtName = name, debtAmount = amt.toIntOrNull() ?: 0, debtDate = date, debtStatus = "Unpaid"))
                withContext(Dispatchers.Main) { showAddDialog = false }
            }
        })
    }

    debtToEdit?.let { debt ->
        EditDebtDialog(debt = debt, onDismiss = { debtToEdit = null }, onSave = { updatedDebt ->
            scope.launch(Dispatchers.IO) {
                db.DebtDao().update(updatedDebt)
                withContext(Dispatchers.Main) { debtToEdit = null }
            }
        })
    }
}

@Composable
fun DebtCard(debt: Debt, onMarkPaid: () -> Unit, onEditClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = debt.debtName ?: "Unknown", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Row {
                    Text("Amount: ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text("₱ ${debt.debtAmount}.00", fontSize = 12.sp, color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                }
                Text("Due: ${debt.debtDate}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (debt.debtStatus != "Paid") ActionButton("Mark Paid", Color(0xFF2E7D32), onMarkPaid)
                ActionButton("Edit", colorResource(id = R.color.darkBlue), onEditClick)
                ActionButton("Delete", Color(0xFFC62828), onDelete)
            }
        }
    }
}

@Composable
fun ActionButton(text: String, color: Color, onClick: () -> Unit) {
    Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = color), contentPadding = PaddingValues(horizontal = 8.dp), shape = RoundedCornerShape(4.dp), modifier = Modifier.height(30.dp)) {
        Text(text = text, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDebtDialog(debt: Debt, onDismiss: () -> Unit, onSave: (Debt) -> Unit) {
    var name by remember { mutableStateOf(debt.debtName ?: "") }
    var amount by remember { mutableStateOf(debt.debtAmount.toString()) }
    var payment by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf(debt.debtDate ?: "") }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
                        dueDate = sdf.format(Date(it))
                    }
                    showDatePicker = false
                }) { Text("OK", color = colorResource(id = R.color.darkBlue)) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = Color.Gray) } },
            colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface, titleContentColor = colorResource(id = R.color.darkBlue))
        ) { DatePicker(state = datePickerState) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Edit Entry", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

                val customColors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorResource(id = R.color.darkBlue), cursorColor = colorResource(id = R.color.darkBlue), focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface)

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Customer Name") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = customColors)
                OutlinedTextField(value = payment, onValueChange = { payment = it }, label = { Text("Deduct Payment (₱)") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2E7D32), focusedTextColor = MaterialTheme.colorScheme.onSurface))
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Total Balance (₱)") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = customColors)

                Box(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }) {
                    OutlinedTextField(value = dueDate, onValueChange = {}, label = { Text("Due Date") }, modifier = Modifier.fillMaxWidth(), enabled = false, readOnly = true, trailingIcon = { Icon(Icons.Default.DateRange, null, tint = colorResource(id = R.color.darkBlue)) }, colors = customColors)
                }

                Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
                    Button(
                        onClick = {
                            val finalAmount = (amount.toIntOrNull() ?: 0) - (payment.toIntOrNull() ?: 0)
                            onSave(debt.copy(debtName = name, debtAmount = if (finalAmount > 0) finalAmount else 0, debtDate = dueDate, debtStatus = if (finalAmount <= 0) "Paid" else debt.debtStatus))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.darkBlue))
                    ) { Text("Update", color = Color.White) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDebtDialog(onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
                        dueDate = sdf.format(Date(it))
                    }
                    showDatePicker = false
                }) { Text("OK", color = colorResource(id = R.color.darkBlue)) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = Color.Gray) } }
        ) { DatePicker(state = datePickerState) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Add New Debt", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 12.dp))
                val customColors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorResource(id = R.color.darkBlue), focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface)
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Customer Name") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = customColors)
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount (₱)") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = customColors)
                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).clickable { showDatePicker = true }) {
                    OutlinedTextField(value = dueDate.ifEmpty { "Select Date" }, onValueChange = {}, label = { Text("Due Date") }, modifier = Modifier.fillMaxWidth(), enabled = false, readOnly = true, trailingIcon = { Icon(Icons.Default.DateRange, null, tint = colorResource(id = R.color.darkBlue)) }, colors = customColors)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
                    Button(onClick = { onSave(name, amount, dueDate) }, colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.darkBlue))) { Text("Save", color = Color.White) }
                }
            }
        }
    }
}