package com.example.project_pockettindahan

import androidx.compose.ui.text.style.TextAlign
import AppDatabase
import Items
import android.content.res.Configuration
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InventoryActivity: ComponentActivity() {

    private val isDarkModeState = mutableStateOf(false)
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

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        prefs = PreferencesManager(this)
        isDarkModeState.value = prefs.isDarkMode()
        fontScaleState.value = prefs.getFontScale()

        setContent {
            val context = LocalContext.current

            val isTagalog = java.util.Locale.getDefault().language == "tl"

            val localizedContext: android.content.Context = remember(isTagalog) {
                val localeTag = if (isTagalog) "tl" else "en"
                val locale = java.util.Locale(localeTag)
                java.util.Locale.setDefault(locale)

                val config = Configuration(context.resources.configuration)
                config.setLocale(locale)

                context.createConfigurationContext(config)
            }

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

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalDensity provides customDensity
            ) {
                MaterialTheme(colorScheme = if (isDarkModeState.value) darkColors else lightColors) {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        Message(db)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::prefs.isInitialized) {
            isDarkModeState.value = prefs.isDarkMode()
            fontScaleState.value = prefs.getFontScale()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Message(db: AppDatabase) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Surface(
                        shape = CircleShape,
                        color = Color.White, // <--- FIXED: Changed from Transparent to White
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
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
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
    val fontScale = LocalDensity.current.fontScale
    val isTagalog = java.util.Locale.getDefault().language == "tl"

    val filteredItems = itemList.filter { item ->
        val matchesSearch = item.itemName?.contains(searchQuery, ignoreCase = true) ?: false
        val matchesCategory = if (selectedCategory == "All") true else item.itemCategory == selectedCategory
        (searchQuery.isBlank() || matchesSearch) && matchesCategory
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<Items?>(null) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.inventory_app),
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            if (fontScale > 1.0f) {
                // LARGE FONT: Stack the Search Bar on top, Dropdown and Add underneath
                Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(text = stringResource(id = R.string.search_products), color = Color.Gray, fontSize = 14.sp) },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon", tint = Color.Gray, modifier = Modifier.size(20.dp)) },
                        // FIXED: Replaced .height(50.dp) with .heightIn(min = 50.dp) to stop text clipping
                        modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Gray, focusedBorderColor = colorResource(id = R.color.darkBlue),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        CategoryDropdown(
                            expanded = expanded, selectedCategory = selectedCategory, categories = categories, isTagalog = isTagalog,
                            onExpand = { expanded = true }, onDismiss = { expanded = false }, onSelect = { selectedCategory = it },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { showAddDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.darkBlue)),
                            // FIXED: Replaced .height(50.dp) with .heightIn(min = 50.dp)
                            modifier = Modifier.weight(1f).heightIn(min = 50.dp)
                        ) {
                            Text(text = stringResource(id = R.string.add_btn_label), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
            } else {
                // NORMAL FONT: Keep it side by side
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(text = stringResource(id = R.string.search_products), color = Color.Gray, fontSize = 14.sp) },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon", tint = Color.Gray, modifier = Modifier.size(20.dp)) },
                        // FIXED: Replaced .height(50.dp) with .heightIn(min = 50.dp) to stop text clipping
                        modifier = Modifier.weight(1f).heightIn(min = 50.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Gray, focusedBorderColor = colorResource(id = R.color.darkBlue),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    CategoryDropdown(
                        expanded = expanded, selectedCategory = selectedCategory, categories = categories, isTagalog = isTagalog,
                        onExpand = { expanded = true }, onDismiss = { expanded = false }, onSelect = { selectedCategory = it },
                        modifier = Modifier.width(110.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { showAddDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.darkBlue)),
                        // FIXED: Replaced .height(50.dp) with .heightIn(min = 50.dp)
                        modifier = Modifier.heightIn(min = 50.dp)
                    ) {
                        Text(text = stringResource(id = R.string.add_btn_label), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(filteredItems) { item ->
                ItemCard(
                    item = item,
                    isTagalog = isTagalog,
                    onEditClick = { itemToEdit = item },
                    onDeleteClick = {
                        scope.launch(Dispatchers.IO) {
                            db.ItemsDao().delete(item)
                            withContext(Dispatchers.Main) { Toast.makeText(context, "${item.itemName} Deleted", Toast.LENGTH_SHORT).show() }
                        }
                    }
                )
            }
        }
    }

    if (showAddDialog) {
        AddItemDialog(
            isTagalog = isTagalog,
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

    itemToEdit?.let { editingItem ->
        EditItemDialog(
            item = editingItem,
            isTagalog = isTagalog,
            onDismiss = { itemToEdit = null },
            onSave = { updatedItem ->
                scope.launch(Dispatchers.IO) {
                    db.ItemsDao().update(updatedItem)
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
fun CategoryDropdown(
    expanded: Boolean, selectedCategory: String, categories: List<String>, isTagalog: Boolean,
    onExpand: () -> Unit, onDismiss: () -> Unit, onSelect: (String) -> Unit, modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Box(
            // FIXED: Replaced .height(50.dp) with .heightIn(min = 50.dp)
            modifier = Modifier.heightIn(min = 50.dp).fillMaxWidth().background(Color.Gray, shape = RoundedCornerShape(8.dp)).border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp)).clickable { onExpand() }.padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = stringResource(id = R.string.category_label), color = Color.White, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = Color.White)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = onDismiss, modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            categories.forEach { category ->
                val displayCat = if (isTagalog) {
                    when(category) {
                        "Drinks" -> "Mga Inumin"
                        "Food" -> "Pagkain"
                        "Cleaning Supplies" -> "Panlinis"
                        "Hygiene" -> "Kalinisan"
                        "Miscellaneous" -> "Iba pa"
                        "All" -> "Lahat"
                        else -> category
                    }
                } else category

                DropdownMenuItem(
                    text = { Text(text = displayCat, color = MaterialTheme.colorScheme.onSurface) },
                    onClick = { onSelect(category); onDismiss() }
                )
            }
        }
    }
}

@Composable
fun AddItemDialog(isTagalog: Boolean, onDismiss: () -> Unit, onAddItem: (Items) -> Unit) {
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
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(4.dp, colorResource(id = R.color.darkBlue)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                Text(
                    text = stringResource(id = R.string.add_item_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 16.dp)
                )

                DialogTextField(label = stringResource(id = R.string.product_name_label), value = productName, onValueChange = { productName = it })
                DialogTextField(label = stringResource(id = R.string.stock_pieces_label), value = stock, onValueChange = { stock = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

                Column(modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth()) {
                    Text(text = stringResource(id = R.string.category_label), color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp))
                    Box {
                        Box(
                            modifier = Modifier.fillMaxWidth().heightIn(min = 36.dp).background(MaterialTheme.colorScheme.background).border(1.dp, Color.Gray, RoundedCornerShape(4.dp)).clickable { expanded = true }.padding(horizontal = 8.dp, vertical = 6.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                val displayCat = if (isTagalog) {
                                    when(selectedCategory) {
                                        "Drinks" -> "Mga Inumin"
                                        "Food" -> "Pagkain"
                                        "Cleaning Supplies" -> "Panlinis"
                                        "Hygiene" -> "Kalinisan"
                                        "Miscellaneous" -> "Iba pa"
                                        "Input Category" -> "Ilagay ang Kategorya"
                                        else -> selectedCategory
                                    }
                                } else selectedCategory

                                Text(text = displayCat, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                            }
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                            categories.forEach { category ->
                                val listCat = if (isTagalog) {
                                    when(category) {
                                        "Drinks" -> "Mga Inumin"; "Food" -> "Pagkain"; "Cleaning Supplies" -> "Panlinis"; "Hygiene" -> "Kalinisan"; "Miscellaneous" -> "Iba pa"; else -> category
                                    }
                                } else category
                                DropdownMenuItem(text = { Text(text = listCat, color = MaterialTheme.colorScheme.onSurface) }, onClick = { selectedCategory = category; expanded = false })
                            }
                        }
                    }
                }

                DialogTextField(label = stringResource(id = R.string.original_price_label), value = originalPrice, onValueChange = { originalPrice = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                DialogTextField(label = stringResource(id = R.string.retail_price_label), value = retailPrice, onValueChange = { retailPrice = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
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
                                itemName = productName, itemStock = stockInt, itemCurrentStock = stockInt,
                                itemCategory = if (selectedCategory == "Input Category") "" else selectedCategory,
                                itemOriginalPrice = origPriceInt, itemRetailPrice = retailPriceInt
                            )
                            onAddItem(newItem)
                        },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    ) {
                        Text(text = stringResource(id = R.string.add_action), color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun EditItemDialog(item: Items, isTagalog: Boolean, onDismiss: () -> Unit, onSave: (Items) -> Unit) {
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
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(4.dp, colorResource(id = R.color.darkBlue)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                Text(
                    text = "${stringResource(id = R.string.edit_item_title)}: \"${item.itemName}\"",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 16.dp)
                )

                DialogTextField(label = stringResource(id = R.string.product_name_label), value = productName, onValueChange = { productName = it })
                DialogTextField(label = stringResource(id = R.string.added_stock_label), value = addedStock, onValueChange = { addedStock = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                DialogTextField(label = stringResource(id = R.string.remaining_stock_label), value = remainingStock, onValueChange = { remainingStock = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

                Column(modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth()) {
                    Text(text = stringResource(id = R.string.category_label), color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp))
                    Box {
                        Box(
                            modifier = Modifier.fillMaxWidth().heightIn(min = 36.dp).background(MaterialTheme.colorScheme.background).border(1.dp, Color.Gray, RoundedCornerShape(4.dp)).clickable { expanded = true }.padding(horizontal = 8.dp, vertical = 6.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                val displayCat = if (isTagalog) {
                                    when(selectedCategory) {
                                        "Drinks" -> "Mga Inumin"; "Food" -> "Pagkain"; "Cleaning Supplies" -> "Panlinis"; "Hygiene" -> "Kalinisan"; "Miscellaneous" -> "Iba pa"; "Input Category" -> "Ilagay ang Kategorya"; else -> selectedCategory
                                    }
                                } else selectedCategory
                                Text(text = displayCat, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                            }
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                            categories.forEach { category ->
                                val listCat = if (isTagalog) {
                                    when(category) {
                                        "Drinks" -> "Mga Inumin"; "Food" -> "Pagkain"; "Cleaning Supplies" -> "Panlinis"; "Hygiene" -> "Kalinisan"; "Miscellaneous" -> "Iba pa"; else -> category
                                    }
                                } else category
                                DropdownMenuItem(text = { Text(text = listCat, color = MaterialTheme.colorScheme.onSurface) }, onClick = { selectedCategory = category; expanded = false })
                            }
                        }
                    }
                }

                DialogTextField(label = stringResource(id = R.string.original_price_label), value = originalPrice, onValueChange = { originalPrice = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                DialogTextField(label = stringResource(id = R.string.retail_price_label), value = retailPrice, onValueChange = { retailPrice = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
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
                                itemName = productName, itemStock = (item.itemStock ?: 0) + addedInt, itemCurrentStock = remainingInt + addedInt,
                                itemCategory = if (selectedCategory == "Input Category") "" else selectedCategory,
                                itemOriginalPrice = origPriceInt, itemRetailPrice = retailPriceInt
                            )
                            onSave(updatedItem)
                        },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    ) {
                        Text(text = stringResource(id = R.string.save_action), color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun DialogTextField(label: String, value: String, onValueChange: (String) -> Unit, keyboardOptions: KeyboardOptions = KeyboardOptions.Default) {
    Column(modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth()) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            keyboardOptions = keyboardOptions,
            textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp),
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).border(1.dp, Color.Gray, RoundedCornerShape(4.dp)).padding(horizontal = 12.dp, vertical = 8.dp),
            singleLine = true
        )
    }
}

@Composable
fun ItemCard(item: Items, isTagalog: Boolean, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    val fontScale = LocalDensity.current.fontScale

    val displayCategory = if (isTagalog) {
        when(item.itemCategory) {
            "Drinks" -> "Mga Inumin"; "Food" -> "Pagkain"; "Cleaning Supplies" -> "Panlinis"; "Hygiene" -> "Kalinisan"; "Miscellaneous" -> "Iba pa"; else -> item.itemCategory
        }
    } else item.itemCategory

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            if (fontScale > 1.0f) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(text = item.itemName ?: "Unknown", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Text(text = displayCategory ?: "Uncategorized", color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        Text(text = "Stock: ", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                        Text(text = "${item.itemCurrentStock ?: 0}", color = Color(0xFF4CAF50), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "₱${item.itemRetailPrice ?: 0}.00", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onEditClick, modifier = Modifier.weight(1f), shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.darkBlue))
                        ) { Text(text = stringResource(id = R.string.edit_action), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White) }

                        Button(
                            onClick = onDeleteClick, modifier = Modifier.weight(1f), shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                        ) { Text(text = stringResource(id = R.string.delete_action), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White) }
                    }
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(text = item.itemName ?: "Unknown", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(text = displayCategory ?: "Uncategorized", color = Color.Gray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row {
                            Text(text = "Stock: ", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                            Text(text = "${item.itemCurrentStock ?: 0}", color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "₱${item.itemRetailPrice ?: 0}.00", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = onEditClick, modifier = Modifier.heightIn(min = 30.dp).padding(end = 4.dp), contentPadding = PaddingValues(horizontal = 8.dp),
                            shape = RoundedCornerShape(4.dp), colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.darkBlue))
                        ) { Text(text = stringResource(id = R.string.edit_action), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White) }

                        Button(
                            onClick = onDeleteClick, modifier = Modifier.heightIn(min = 30.dp), contentPadding = PaddingValues(horizontal = 8.dp),
                            shape = RoundedCornerShape(4.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                        ) { Text(text = stringResource(id = R.string.delete_action), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White) }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                Row {
                    Text(text = "${stringResource(id = R.string.original_price_label)}: ", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                    Text(text = "₱${item.itemOriginalPrice ?: 0}.00", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}