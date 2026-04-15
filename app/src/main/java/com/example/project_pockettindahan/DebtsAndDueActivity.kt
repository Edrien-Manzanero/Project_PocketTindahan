package com.example.project_pockettindahan

import AppDatabase
import Debt

import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.ui.text.style.TextAlign
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
        setContent {
            DebtsScreen(db)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtsScreen(db: AppDatabase) {
    val allDebts by db.DebtDao().getAll().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("Unpaid") }
    var showAddDialog by remember { mutableStateOf(false) }

    // Tracks the debt being edited
    var debtToEdit by remember { mutableStateOf<Debt?>(null) }

    val filteredDebts = allDebts.filter { debt ->
        val matchesTab = debt.debtStatus == selectedTab
        val matchesSearch = debt.debtName?.contains(searchQuery, ignoreCase = true) ?: false
        matchesTab && (searchQuery.isEmpty() || matchesSearch)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Surface(shape = CircleShape, color = Color.White, modifier = Modifier.size(45.dp)) {
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
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF5F5F5))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Debts/Dues", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = colorResource(id = R.color.darkBlue), modifier = Modifier.padding(bottom = 12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(8.dp)),
                placeholder = { Text("Search Customer", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorResource(id = R.color.darkBlue), // Fixes purple border
                    cursorColor = colorResource(id = R.color.darkBlue) // Fixes purple cursor
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tabs
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                val tabs = listOf("Unpaid", "Paid", "Overdue")
                tabs.forEachIndexed { index, tab ->
                    val isSelected = selectedTab == tab
                    val shape = when (index) {
                        0 -> RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
                        tabs.size - 1 -> RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
                        else -> RoundedCornerShape(0.dp)
                    }
                    Box(
                        modifier = Modifier.weight(1f).background(if (isSelected) colorResource(id = R.color.darkBlue) else Color.White, shape).border(1.dp, if (isSelected) colorResource(id = R.color.darkBlue) else Color.Gray, shape).clickable { selectedTab = tab }.padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = tab, color = if (isSelected) Color.White else colorResource(id = R.color.darkBlue), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredDebts) { debt ->
                    DebtCard(
                        debt = debt,
                        onMarkPaid = {
                            scope.launch(Dispatchers.IO) {
                                db.DebtDao().update(debt.copy(debtStatus = "Paid"))
                            }
                        },
                        onEditClick = { debtToEdit = debt },
                        onDelete = {
                            scope.launch(Dispatchers.IO) {
                                db.DebtDao().delete(debt)
                            }
                        }
                    )
                }
            }
        }
    }

    // --- DIALOGS ---
    if (showAddDialog) {
        AddDebtDialog(onDismiss = { showAddDialog = false }, onSave = { name, amt, date ->
            scope.launch(Dispatchers.IO) {
                db.DebtDao().insertAll(Debt(debtName = name, debtAmount = amt.toIntOrNull() ?: 0, debtDate = date, debtStatus = "Unpaid"))
                withContext(Dispatchers.Main) { showAddDialog = false }
            }
        })
    }

    // --- THE EDIT DIALOG ---
    debtToEdit?.let { debt ->
        EditDebtDialog(
            debt = debt,
            onDismiss = { debtToEdit = null },
            onSave = { updatedDebt ->
                scope.launch(Dispatchers.IO) {
                    db.DebtDao().update(updatedDebt)
                    withContext(Dispatchers.Main) { debtToEdit = null }
                }
            }
        )
    }
}

@Composable
fun DebtCard(debt: Debt, onMarkPaid: () -> Unit, onEditClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = debt.debtName ?: "Unknown", color = colorResource(id = R.color.darkBlue), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Row {
                    Text("Amount: ", fontSize = 12.sp, color = colorResource(id = R.color.darkBlue))
                    Text("₱ ${debt.debtAmount}.00", fontSize = 12.sp, color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                }
                Text("Due: ${debt.debtDate}", fontSize = 12.sp, color = colorResource(id = R.color.darkBlue))
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

// --- EDIT DIALOG WITH PARTIAL PAYMENT LOGIC ---
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
                }) { Text("OK", color = colorResource(id = R.color.darkBlue)) } // Fixed Purple
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = Color.Gray) }
            },
            colors = DatePickerDefaults.colors(
                containerColor = Color.White,
                titleContentColor = colorResource(id = R.color.darkBlue),
                headlineContentColor = colorResource(id = R.color.darkBlue),
                weekdayContentColor = colorResource(id = R.color.darkBlue),
                dayContentColor = colorResource(id = R.color.darkBlue),
                todayDateBorderColor = colorResource(id = R.color.darkBlue),
                todayContentColor = colorResource(id = R.color.darkBlue),
                selectedDayContainerColor = colorResource(id = R.color.darkBlue),
                selectedDayContentColor = Color.White
            )
        ) { DatePicker(state = datePickerState) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Edit Entry", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colorResource(id = R.color.darkBlue))

                // Added Custom Colors to eliminate purple
                val customTextFieldColors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorResource(id = R.color.darkBlue),
                    focusedLabelColor = colorResource(id = R.color.darkBlue),
                    cursorColor = colorResource(id = R.color.darkBlue)
                )

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Customer Name") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = customTextFieldColors)

                OutlinedTextField(
                    value = payment,
                    onValueChange = { payment = it },
                    label = { Text("Deduct Payment (₱)") },
                    placeholder = { Text("Enter amount paid today") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2E7D32), focusedLabelColor = Color(0xFF2E7D32), cursorColor = Color(0xFF2E7D32)) // Green for payment
                )

                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Total Balance (₱)") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = customTextFieldColors)

                Box(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }) {
                    OutlinedTextField(value = dueDate, onValueChange = {}, label = { Text("Due Date") }, modifier = Modifier.fillMaxWidth(), enabled = false, readOnly = true, trailingIcon = { Icon(Icons.Default.DateRange, null, tint = colorResource(id = R.color.darkBlue)) }, colors = customTextFieldColors)
                }

                Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
                    Button(
                        onClick = {
                            val currentBalance = amount.toIntOrNull() ?: 0
                            val paidAmount = payment.toIntOrNull() ?: 0
                            val finalAmount = currentBalance - paidAmount

                            val finalStatus = if (finalAmount <= 0) "Paid" else debt.debtStatus

                            onSave(debt.copy(
                                debtName = name,
                                debtAmount = if (finalAmount > 0) finalAmount else 0,
                                debtDate = dueDate,
                                debtStatus = finalStatus ?: "Unpaid"
                            ))
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
                }) { Text("OK", color = colorResource(id = R.color.darkBlue)) } // Fixed Purple
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = Color.Gray) }
            },
            colors = DatePickerDefaults.colors(
                containerColor = Color.White,
                titleContentColor = colorResource(id = R.color.darkBlue),
                headlineContentColor = colorResource(id = R.color.darkBlue),
                weekdayContentColor = colorResource(id = R.color.darkBlue),
                dayContentColor = colorResource(id = R.color.darkBlue),
                todayDateBorderColor = colorResource(id = R.color.darkBlue),
                todayContentColor = colorResource(id = R.color.darkBlue),
                selectedDayContainerColor = colorResource(id = R.color.darkBlue),
                selectedDayContentColor = Color.White
            )
        ) { DatePicker(state = datePickerState) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Add New Debt", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colorResource(id = R.color.darkBlue), modifier = Modifier.padding(bottom = 12.dp))

                // Added Custom Colors to eliminate purple
                val customTextFieldColors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorResource(id = R.color.darkBlue),
                    focusedLabelColor = colorResource(id = R.color.darkBlue),
                    cursorColor = colorResource(id = R.color.darkBlue)
                )

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Customer Name") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = customTextFieldColors)
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount (₱)") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = customTextFieldColors)

                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).clickable { showDatePicker = true }) {
                    OutlinedTextField(
                        value = dueDate.ifEmpty { "Select Date" },
                        onValueChange = {},
                        label = { Text("Due Date") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        readOnly = true,
                        trailingIcon = { Icon(Icons.Default.DateRange, null, tint = colorResource(id = R.color.darkBlue)) },
                        colors = customTextFieldColors
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
                    Button(onClick = { onSave(name, amount, dueDate) }, colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.darkBlue))) { Text("Save", color = Color.White) }
                }
            }
        }
    }
}