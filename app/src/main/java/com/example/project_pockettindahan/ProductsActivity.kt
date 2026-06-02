package com.example.project_pockettindahan

import AppDatabase
import Items
import Sales
import SalesItem
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
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

data class CartItem(val product: Items, val quantity: Int)

// FIXED 1: Reverted to ComponentActivity to stop the Theme Crash
class ProductsActivity : ComponentActivity() {

    private val isDarkModeState = mutableStateOf(false)
    private val fontScaleState = mutableStateOf(1.0f)
    private val appLocalesState = mutableStateOf(AppCompatDelegate.getApplicationLocales())
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
        fontScaleState.value = prefs.getFontScale()

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
                        ProductsScreen(db)
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
        appLocalesState.value = AppCompatDelegate.getApplicationLocales()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(db: AppDatabase) {
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var showCartDialog by remember { mutableStateOf(false) }

    var cartItems by remember { mutableStateOf(listOf<CartItem>()) }
    val totalCartQuantity = cartItems.sumOf { it.quantity }
    val totalCartPrice = cartItems.sumOf { it.quantity * (it.product.itemRetailPrice ?: 0) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // FIXED 2: A crash-proof way to check if Tagalog is active
    val isTagalog = java.util.Locale.getDefault().language == "tl"

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
        bottomBar = {
            if (selectedCategory == null) {
                Surface(
                    color = colorResource(id = R.color.darkBlue),
                    shadowElevation = 16.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp), // slightly reduced horizontal padding
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // FIXED: Added weight(1f) so this side takes leftover space but STOPS at the button
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = if (isTagalog) "Kabuuang Cart: ₱${totalCartPrice}.00" else "Total Cart: ₱${totalCartPrice}.00",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp, // Dropped slightly from 22 to 20 for safety
                                maxLines = 1, // Stops it from stacking awkwardly
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isTagalog) "Mga Item: $totalCartQuantity" else "Items in Cart: $totalCartQuantity",
                                color = Color.LightGray,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }

                        Button(
                            onClick = { showCartDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            shape = RoundedCornerShape(8.dp),
                            // FIXED: Changed height to heightIn so it can flex if the text needs it
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Icon(
                                painter = painterResource(android.R.drawable.ic_menu_agenda),
                                contentDescription = "Cart",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isTagalog) "Tingnan" else "View Cart",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1 // Forces the button text to stay on one line
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(15.dp)
                .border(4.dp, colorResource(id = R.color.darkBlue), RoundedCornerShape(16.dp)),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (selectedCategory == null) {
                CategoryGrid(onCategoryClick = { category -> selectedCategory = category })
            } else {
                ProductListScreen(
                    db = db,
                    category = selectedCategory!!,
                    onBackClick = { selectedCategory = null },
                    onAddToCart = { newlyAddedItems ->
                        val newCart = cartItems.toMutableList()
                        for (added in newlyAddedItems) {
                            val existingIndex = newCart.indexOfFirst { it.product.uid == added.product.uid }
                            if (existingIndex != -1) {
                                val existing = newCart[existingIndex]
                                newCart[existingIndex] = existing.copy(quantity = existing.quantity + added.quantity)
                            } else {
                                newCart.add(added)
                            }
                        }
                        cartItems = newCart
                        selectedCategory = null
                    }
                )
            }
        }
    }

    if (showCartDialog) {
        CartDialog(
            cartItems = cartItems,
            onDismiss = { showCartDialog = false },
            onCheckout = {
                scope.launch(Dispatchers.IO) {
                    val totalSales = cartItems.sumOf { (it.product.itemRetailPrice ?: 0) * it.quantity }
                    val totalOriginalCost = cartItems.sumOf { (it.product.itemOriginalPrice ?: 0) * it.quantity }
                    val totalProfit = totalSales - totalOriginalCost
                    val totalItemsSold = cartItems.sumOf { it.quantity }

                    val sdfDate = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).apply {
                        timeZone = java.util.TimeZone.getTimeZone("Asia/Manila")
                    }
                    val sdfTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).apply {
                        timeZone = java.util.TimeZone.getTimeZone("Asia/Manila")
                    }
                    val currentDate = sdfDate.format(Date())
                    val currentTime = sdfTime.format(Date())

                    val newSale = Sales(
                        salesDate = currentDate,
                        salesTime = currentTime,
                        salesTotalSales = totalSales,
                        salesTotalsold = totalItemsSold,
                        salesTransaction = 1,
                        salesProfit = totalProfit
                    )

                    val newlyCreatedSaleId = db.SalesDao().insertSale(newSale).toInt()

                    val salesItemsList = cartItems.map { cartItem ->
                        SalesItem(
                            parentSaleId = newlyCreatedSaleId,
                            itemName = cartItem.product.itemName ?: "Unknown",
                            quantity = cartItem.quantity,
                            pricePerUnit = cartItem.product.itemRetailPrice ?: 0
                        )
                    }
                    db.SalesItemDao().insertAll(*salesItemsList.toTypedArray())

                    cartItems.forEach { cartItem ->
                        val currentStock = cartItem.product.itemCurrentStock ?: 0
                        val updatedProduct = cartItem.product.copy(
                            itemCurrentStock = currentStock - cartItem.quantity
                        )
                        db.ItemsDao().update(updatedProduct)
                    }

                    withContext(Dispatchers.Main) {
                        cartItems = emptyList()
                        showCartDialog = false
                        Toast.makeText(
                            context,
                            if (isTagalog) "Matagumpay ang Checkout!" else "Checkout Successful!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        )
    }
}

data class CategoryItem(val title: String, val iconRes: Int)

@Composable
fun CategoryGrid(onCategoryClick: (String) -> Unit) {
    val categoryList = listOf(
        CategoryItem("Drinks", R.drawable.ic_drinks),
        CategoryItem("Food", R.drawable.ic_food),
        CategoryItem("Cleaning Supplies", R.drawable.ic_sanitation),
        CategoryItem("Hygiene", R.drawable.ic_hygiene),
        CategoryItem("Miscellaneous", R.drawable.ic_others)
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.wrapContentHeight()
        ) {
            items(categoryList) { item ->
                CategoryButton(
                    title = item.title,
                    iconRes = item.iconRes,
                    onClick = { onCategoryClick(item.title) }
                )
            }
        }
    }
}

@Composable
fun CategoryButton(title: String, iconRes: Int, onClick: () -> Unit) {
    val isTagalog = java.util.Locale.getDefault().language == "tl"
    val displayTitle = if (isTagalog) {
        when (title) {
            "Drinks" -> "Mga Inumin"
            "Food" -> "Pagkain"
            "Cleaning Supplies" -> "Panlinis"
            "Hygiene" -> "Kalinisan"
            "Miscellaneous" -> "Iba pa"
            else -> title
        }
    } else {
        title
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .drawBehind {
                val shadowColor = Color.Black.copy(alpha = 0.5f)
                val spread = 8.dp.toPx()
                val offsetX = -6.dp.toPx()
                val offsetY = 6.dp.toPx()

                val paint = Paint()
                val frameworkPaint = paint.asFrameworkPaint()
                frameworkPaint.color = shadowColor.toArgb()
                frameworkPaint.setShadowLayer(spread, offsetX, offsetY, shadowColor.toArgb())

                drawIntoCanvas { canvas ->
                    canvas.drawRoundRect(0f, 0f, size.width, size.height, 12.dp.toPx(), 12.dp.toPx(), paint)
                }
            }
            .background(Color(0xFFC62828), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(5.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp)).padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = displayTitle,
                    modifier = Modifier.weight(1f).padding(bottom = 8.dp),
                    contentScale = ContentScale.Fit
                )
                Text(
                    text = displayTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun ProductListScreen(
    db: AppDatabase,
    category: String,
    onBackClick: () -> Unit,
    onAddToCart: (List<CartItem>) -> Unit
) {
    val allItems by db.ItemsDao().getAll().collectAsState(initial = emptyList())
    val categoryItems = allItems.filter { it.itemCategory == category }

    val itemQuantities = remember { mutableStateMapOf<Int, Int>() }
    val context = LocalContext.current

    val isTagalog = java.util.Locale.getDefault().language == "tl"
    val displayCategory = if (isTagalog) {
        when (category) {
            "Drinks" -> "Mga Inumin"
            "Food" -> "Pagkain"
            "Cleaning Supplies" -> "Panlinis"
            "Hygiene" -> "Kalinisan"
            "Miscellaneous" -> "Iba pa"
            else -> category
        }
    } else category

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = displayCategory,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (categoryItems.isEmpty()) {
                item {
                    Text(
                        text = if (isTagalog) "Walang nahanap na item." else "No items found.",
                        color = Color.Gray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
                    )
                }
            } else {
                items(categoryItems) { item ->
                    ProductCard(
                        item = item,
                        quantity = itemQuantities[item.uid] ?: 0,
                        onQuantityChange = { newQty -> itemQuantities[item.uid] = newQty }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBackClick,
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.darkBlue)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = if (isTagalog) "Bumalik" else "Back", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    val itemsToAdd = categoryItems.mapNotNull { item ->
                        val qty = itemQuantities[item.uid] ?: 0
                        if (qty > 0) CartItem(item, qty) else null
                    }

                    if (itemsToAdd.isNotEmpty()) {
                        onAddToCart(itemsToAdd)
                        Toast.makeText(context, if (isTagalog) "Naidagdag sa cart!" else "Added to cart!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, if (isTagalog) "Walang napiling item" else "No items selected", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, colorResource(id = R.color.darkBlue)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = if (isTagalog) "Idagdag" else "Add", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProductCard(item: Items, quantity: Int, onQuantityChange: (Int) -> Unit) {
    val isTagalog = java.util.Locale.getDefault().language == "tl"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = item.itemName ?: "Unknown Item", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)

                    val translatedCat = if (isTagalog) {
                        when (item.itemCategory) {
                            "Drinks" -> "Mga Inumin"
                            "Food" -> "Pagkain"
                            "Cleaning Supplies" -> "Panlinis"
                            "Hygiene" -> "Kalinisan"
                            "Miscellaneous" -> "Iba pa"
                            else -> item.itemCategory
                        }
                    } else item.itemCategory

                    Text(text = translatedCat ?: "Uncategorized", color = Color.Gray, fontSize = 14.sp)
                }

                Text(
                    text = "₱${item.itemRetailPrice ?: 0}.00",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = if (isTagalog) "Stock: " else "Stock: ", color = Color.Gray, fontSize = 14.sp)
                    Text(text = "${item.itemCurrentStock ?: 0}", color = Color(0xFF4CAF50), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.border(1.dp, Color.LightGray, RoundedCornerShape(50)).padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "-", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.clickable { if (quantity > 0) onQuantityChange(quantity - 1) }.padding(horizontal = 8.dp)
                    )
                    Text(
                        text = quantity.toString(), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    Text(
                        text = "+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.clickable { if (quantity < (item.itemCurrentStock ?: 0)) onQuantityChange(quantity + 1) }.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CartDialog(cartItems: List<CartItem>, onDismiss: () -> Unit, onCheckout: () -> Unit) {
    val isTagalog = java.util.Locale.getDefault().language == "tl"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(4.dp, colorResource(id = R.color.darkBlue)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    text = if (isTagalog) "Iyong Cart" else "Your Cart",
                    fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), textAlign = TextAlign.Center
                )

                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (cartItems.isEmpty()) {
                        item { Text(text = if (isTagalog) "Wala pang laman ang iyong cart." else "Your cart is currently empty.", color = Color.Gray, modifier = Modifier.fillMaxWidth().padding(32.dp), textAlign = TextAlign.Center) }
                    } else {
                        items(cartItems) { cartItem ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = cartItem.product.itemName ?: "Unknown", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        Text(text = if (isTagalog) "Dami: ${cartItem.quantity}" else "Qty: ${cartItem.quantity}", color = Color.Gray, fontSize = 14.sp)
                                    }
                                    Text(
                                        text = "₱${(cartItem.product.itemRetailPrice ?: 0) * cartItem.quantity}.00",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val total = cartItems.sumOf { (it.product.itemRetailPrice ?: 0) * it.quantity }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(if (isTagalog) "Kabuuan:" else "Total:", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("₱${total}.00", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4CAF50))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.darkBlue)), shape = RoundedCornerShape(8.dp)) {
                        Text(text = if (isTagalog) "Bumalik" else "Back", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    if (cartItems.isNotEmpty()) {
                        Button(onClick = onCheckout, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), shape = RoundedCornerShape(8.dp)) {
                            Text(text = if (isTagalog) "I-checkout" else "Checkout", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}