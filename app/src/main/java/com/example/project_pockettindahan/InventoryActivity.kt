package com.example.project_pockettindahan

import AppDatabase
import Items
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InventoryActivity: ComponentActivity(){

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "pocket-tindahan-db"
        ).build()
    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContent {
            Message(db)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Message(db: AppDatabase) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier.size(45.dp)
                    ) {
                        /*Image(
                            painter = painterResource(id = R.drawable.img),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(90.dp)
                                .padding(5.dp)
                        )*/
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
                .background(Color(0xFFF5F5F5)),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Spacer(modifier = Modifier.height(24.dp))
            InventorySearchBar(db)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventorySearchBar(db: AppDatabase) {
    var searchQuery by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Drinks", "Food", "Cleaning Supplies", "Hygiene", "Miscellaneous")

    val itemList by db.ItemsDao().getAll().collectAsState(initial = emptyList())

    // State for Dialogs
    var showAddDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<Items?>(null) } // Keeps track of what item is being edited

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Inventory",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = colorResource(id = R.color.darkBlue),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "search products",
                            color = Color.LightGray,
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Icon",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray,
                        focusedBorderColor = colorResource(id = R.color.darkBlue),
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box {
                    Box(
                        modifier = Modifier
                            .height(50.dp)
                            .background(Color(0xFF9E9E9E), shape = RoundedCornerShape(8.dp))
                            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                            .clickable { expanded = true }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Category: $selectedCategory",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown",
                                tint = Color.White
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(text = category) },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.darkBlue)
                    ),
                    modifier = Modifier.height(50.dp)
                ) {
                    Text(
                        text = "+ Add",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display Database Items
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(itemList) { item ->
                ItemCard(
                    item = item,
                    onEditClick = { itemToEdit = item }, // Opens Edit Dialog
                    onDeleteClick = {
                        // Deletes from Database
                        scope.launch(Dispatchers.IO) {
                            db.ItemsDao().delete(item)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "${item.itemName} Deleted", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }
        }
    }

    // --- DIALOG TRIGGERS ---

    // Add Item Dialog
    if (showAddDialog) {
        AddItemDialog(
            onDismiss = { showAddDialog = false },
            onAddItem = { newItem ->
                scope.launch(Dispatchers.IO) {
                    db.ItemsDao().insertAll(newItem)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Item Successfully Added", Toast.LENGTH_SHORT).show()
                        showAddDialog = false
                    }
                }
            }
        )
    }

    // Edit Item Dialog
    itemToEdit?.let { editingItem ->
        EditItemDialog(
            item = editingItem,
            onDismiss = { itemToEdit = null },
            onSave = { updatedItem ->
                scope.launch(Dispatchers.IO) {
                    db.ItemsDao().update(updatedItem) // Needs @Update in ItemsDao
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Item Updated", Toast.LENGTH_SHORT).show()
                        itemToEdit = null
                    }
                }
            }
        )
    }
}

@Composable
fun AddItemDialog(
    onDismiss: () -> Unit,
    onAddItem: (Items) -> Unit
) {
    var productName by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var originalPrice by remember { mutableStateOf("") }
    var retailPrice by remember { mutableStateOf("") }

    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("Input Category") }
    val categories = listOf("Drinks", "Food", "Cleaning Supplies", "Hygiene", "Miscellaneous")

    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(0.dp),
            border = BorderStroke(8.dp, colorResource(id = R.color.darkBlue)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                Text(
                    text = "Add an Item",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colorResource(id = R.color.darkBlue),
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 16.dp)
                )

                DialogTextField(label = "Product Name", value = productName, onValueChange = { productName = it })
                DialogTextField(label = "Stock (In Pieces)", value = stock, onValueChange = { stock = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    Text(text = "Category", color = colorResource(id = R.color.darkBlue), fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp))
                    Box {
                        Box(
                            modifier = Modifier.fillMaxWidth(0.6f).height(28.dp).background(Color.LightGray).border(1.dp, Color.Gray).clickable { expanded = true }.padding(horizontal = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(text = selectedCategory, color = Color.DarkGray, fontSize = 12.sp)
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = Color.DarkGray, modifier = Modifier.size(16.dp))
                            }
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            categories.forEach { category ->
                                DropdownMenuItem(text = { Text(text = category) }, onClick = { selectedCategory = category; expanded = false })
                            }
                        }
                    }
                }

                DialogTextField(label = "Original Price", value = originalPrice, onValueChange = { originalPrice = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                DialogTextField(label = "Retail Price", value = retailPrice, onValueChange = { retailPrice = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (productName.isBlank() || stock.isBlank() || originalPrice.isBlank() || retailPrice.isBlank()) {
                            Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val stockInt = stock.toIntOrNull()
                        val origPriceInt = originalPrice.toIntOrNull()
                        val retailPriceInt = retailPrice.toIntOrNull()

                        if (stockInt == null || origPriceInt == null || retailPriceInt == null) {
                            Toast.makeText(context, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val newItem = Items(
                            itemName = productName,
                            itemStock = stockInt,
                            itemCurrentStock = stockInt,
                            itemCategory = if (selectedCategory == "Input Category") "" else selectedCategory,
                            itemOriginalPrice = origPriceInt,
                            itemRetailPrice = retailPriceInt
                        )
                        onAddItem(newItem)
                    },
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    modifier = Modifier.align(Alignment.End).height(40.dp).width(80.dp)
                ) {
                    Text(text = "Add", color = Color.White, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun EditItemDialog(
    item: Items,
    onDismiss: () -> Unit,
    onSave: (Items) -> Unit
) {
    var productName by remember { mutableStateOf(item.itemName ?: "") }
    var addedStock by remember { mutableStateOf("") }
    var remainingStock by remember { mutableStateOf(item.itemCurrentStock?.toString() ?: "") }
    var originalPrice by remember { mutableStateOf(item.itemOriginalPrice?.toString() ?: "") }
    var retailPrice by remember { mutableStateOf(item.itemRetailPrice?.toString() ?: "") }

    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(if (item.itemCategory.isNullOrBlank()) "Input Category" else item.itemCategory) }
    val categories = listOf("Drinks", "Food", "Cleaning Supplies", "Hygiene", "Miscellaneous")

    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(0.dp),
            border = BorderStroke(8.dp, colorResource(id = R.color.darkBlue)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                Text(
                    text = "Edit \"${item.itemName}\"",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colorResource(id = R.color.darkBlue),
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 16.dp)
                )

                DialogTextField(label = "Product Name", value = productName, onValueChange = { productName = it })
                DialogTextField(label = "Added Stock (In Pieces)", value = addedStock, onValueChange = { addedStock = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                DialogTextField(label = "Remaining Stock (In Pieces)", value = remainingStock, onValueChange = { remainingStock = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    Text(text = "Category", color = colorResource(id = R.color.darkBlue), fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp))
                    Box {
                        Box(
                            modifier = Modifier.fillMaxWidth(0.6f).height(28.dp).background(Color.LightGray).border(1.dp, Color.Gray).clickable { expanded = true }.padding(horizontal = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(text = selectedCategory, color = Color.DarkGray, fontSize = 12.sp)
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = Color.DarkGray, modifier = Modifier.size(16.dp))
                            }
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            categories.forEach { category ->
                                DropdownMenuItem(text = { Text(text = category) }, onClick = { selectedCategory = category; expanded = false })
                            }
                        }
                    }
                }

                DialogTextField(label = "Original Price", value = originalPrice, onValueChange = { originalPrice = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                DialogTextField(label = "Retail Price", value = retailPrice, onValueChange = { retailPrice = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (productName.isBlank() || remainingStock.isBlank() || originalPrice.isBlank() || retailPrice.isBlank()) {
                            Toast.makeText(context, "Please fill required fields", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val addedInt = addedStock.toIntOrNull() ?: 0
                        val remainingInt = remainingStock.toIntOrNull() ?: 0
                        val origPriceInt = originalPrice.toIntOrNull() ?: 0
                        val retailPriceInt = retailPrice.toIntOrNull() ?: 0

                        val updatedItem = item.copy(
                            itemName = productName,
                            itemStock = (item.itemStock ?: 0) + addedInt,
                            itemCurrentStock = remainingInt + addedInt,
                            itemCategory = if (selectedCategory == "Input Category") "" else selectedCategory,
                            itemOriginalPrice = origPriceInt,
                            itemRetailPrice = retailPriceInt
                        )
                        onSave(updatedItem)
                    },
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    modifier = Modifier.align(Alignment.End).height(40.dp).width(100.dp)
                ) {
                    Text(text = "Save", color = Color.White, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
fun DialogTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(text = label, color = colorResource(id = R.color.darkBlue), fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            keyboardOptions = keyboardOptions,
            modifier = Modifier.fillMaxWidth(0.6f).background(Color.White).border(1.dp, Color.Gray).padding(horizontal = 8.dp, vertical = 6.dp),
            singleLine = true
        )
    }
}

@Composable
fun ItemCard(
    item: Items,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = item.itemName ?: "Unknown", color = colorResource(id = R.color.darkBlue), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    Text(text = item.itemCategory ?: "Uncategorized", color = Color.Gray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        Text(text = "Stock: ", color = colorResource(id = R.color.darkBlue), fontSize = 12.sp)
                        Text(text = "${item.itemCurrentStock ?: 0}", color = Color(0xFF4CAF50), fontSize = 12.sp)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "₱${item.itemRetailPrice ?: 0}.00", color = colorResource(id = R.color.darkBlue), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onEditClick,
                        modifier = Modifier.height(28.dp).padding(end = 4.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.darkBlue))
                    ) {
                        Text("Edit", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onDeleteClick,
                        modifier = Modifier.height(28.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                    ) {
                        Text("Delete", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFE0E0E0)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                Row {
                    Text(text = "Original Price: ", color = colorResource(id = R.color.darkBlue), fontSize = 12.sp)
                    Text(text = "₱${item.itemOriginalPrice ?: 0}.00", color = colorResource(id = R.color.darkBlue), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}