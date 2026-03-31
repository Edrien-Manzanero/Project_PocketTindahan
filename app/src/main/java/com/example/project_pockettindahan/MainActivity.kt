package com.example.project_pockettindahan

import AppDatabase
import Items
import ItemsDao
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.room.Room
import com.example.project_pockettindahan.ui.theme.Project_PocketTindahanTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

//    private val db by lazy {
//        Room.databaseBuilder(
//            applicationContext,
//            AppDatabase::class.java, "database-name"
//        ).build()
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Project_PocketTindahanTheme {
                Message()
                }
            }
        }
    }
@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Message() {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier.size(45.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(90.dp)
                                .padding(5.dp)
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
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){

        }
    }
}
//@Composable
//fun AddItemScreen(db: AppDatabase) {
//    // State variables for the Items entity fields
//    var itemName by remember { mutableStateOf("") }
//    var itemStock by remember { mutableStateOf("") }
//    var itemCurrentStock by remember { mutableStateOf("") }
//    var itemCategory by remember { mutableStateOf("") }
//    var itemOriginalPrice by remember { mutableStateOf("") }
//    var itemRetailPrice by remember { mutableStateOf("") }
//
//    val scope = rememberCoroutineScope()
//    val context = LocalContext.current
//
//    Column(modifier = Modifier.padding(16.dp)) {
//
//        OutlinedTextField(
//            value = itemName,
//            onValueChange = { itemName = it },
//            label = { Text("Item Name") }
//        )
//
//        OutlinedTextField(
//            value = itemCategory,
//            onValueChange = { itemCategory = it },
//            label = { Text("Category") }
//        )
//
//        OutlinedTextField(
//            value = itemStock,
//            onValueChange = { itemStock = it },
//            label = { Text("Total Stock") },
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
//        )
//
//        OutlinedTextField(
//            value = itemCurrentStock,
//            onValueChange = { itemCurrentStock = it },
//            label = { Text("Current Stock") },
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
//        )
//
//        OutlinedTextField(
//            value = itemOriginalPrice,
//            onValueChange = { itemOriginalPrice = it },
//            label = { Text("Original Price") },
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
//        )
//
//        OutlinedTextField(
//            value = itemRetailPrice,
//            onValueChange = { itemRetailPrice = it },
//            label = { Text("Retail Price") },
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
//        )
//
//        Button(
//            modifier = Modifier.padding(top = 16.dp),
//            onClick = {
//                // 1. Check if the essential text fields are empty
//                if (itemName.isBlank() || itemStock.isBlank() || itemCurrentStock.isBlank() ||
//                    itemOriginalPrice.isBlank() || itemRetailPrice.isBlank()) {
//                    Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
//                    return@Button
//                }
//
//                // 2. Safely convert string inputs to Integers
//                val stockInt = itemStock.toIntOrNull()
//                val currentStockInt = itemCurrentStock.toIntOrNull()
//                val originalPriceInt = itemOriginalPrice.toIntOrNull()
//                val retailPriceInt = itemRetailPrice.toIntOrNull()
//
//                if (stockInt == null || currentStockInt == null || originalPriceInt == null || retailPriceInt == null) {
//                    Toast.makeText(context, "Please enter valid numbers for stock and prices", Toast.LENGTH_SHORT).show()
//                    return@Button
//                }
//
//                // 3. Insert into database
//                scope.launch(Dispatchers.IO) {
//                    val newItem = Items(
//                        // uid is skipped because autoGenerate = true
//                        itemName = itemName,
//                        itemStock = stockInt,
//                        itemCurrentStock = currentStockInt,
//                        itemCategory = itemCategory.takeIf { it.isNotBlank() }, // allows category to be null if left blank
//                        itemOriginalPrice = originalPriceInt,
//                        itemRetailPrice = retailPriceInt
//                    )
//
//                    // NOTE: Make sure your AppDatabase has a DAO for Items, e.g., itemDao()
//                    db.ItemsDao().insertAll(newItem)
//
//                    withContext(Dispatchers.Main) {
//                        Toast.makeText(context, "Item Successfully added", Toast.LENGTH_SHORT).show()
//
//                        // Clear the form after successful save
//                        itemName = ""
//                        itemStock = ""
//                        itemCurrentStock = ""
//                        itemCategory = ""
//                        itemOriginalPrice = ""
//                        itemRetailPrice = ""
//                    }
//                }
//            }
//        ) {
//            Text("Add Item")
//        }
//    }
//}
//
//@Composable
//fun ShowItemsScreen(dao: ItemsDao) {
//    // 1. Collect the data from Room as a state. This automatically updates
//    // the UI whenever the database changes.
//    val itemList by dao.getAll().collectAsState(initial = emptyList())
//
//    // 2. Remember whether the user has clicked the button to show items
//    var isListVisible by remember { mutableStateOf(false) }
//
//    Column(modifier = Modifier.padding(16.dp)) {
//
//        // 3. The Simple Button
//        Button(
//            onClick = { isListVisible = !isListVisible },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text(text = if (isListVisible) "Hide Items" else "Show My Items")
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // 4. The List of Items
//        if (isListVisible) {
//            if (itemList.isEmpty()) {
//                Text("No items found in the database.")
//            } else {
//                LazyColumn(
//                    verticalArrangement = Arrangement.spacedBy(8.dp),
//                    modifier = Modifier.fillMaxSize()
//                ) {
//                    items(itemList) { item ->
//                        // Using a Card to make each item look clean
//                        Card(
//                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
//                            modifier = Modifier.fillMaxWidth()
//                        ) {
//                            Column(modifier = Modifier.padding(16.dp)) {
//                                // Handling your nullable variables with the ?: (Elvis) operator
//                                Text(text = "Name: ${item.itemName ?: "No Name"}")
//                                Text(text = "Category: ${item.itemCategory ?: "Uncategorized"}")
//                                Text(text = "Current Stock: ${item.itemCurrentStock ?: 0}")
//                                Text(text = "Retail Price: ₱${item.itemRetailPrice ?: 0}")
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun MessageCard(user: Items) {
//
//    Row(modifier = Modifier.padding(all = 8.dp)) {
//        Spacer(modifier = Modifier.width(8.dp))
//
//        // We keep track if the message is expanded or not in this
//        // variable
//        var isExpanded by remember { mutableStateOf(false) }
//        // surfaceColor will be updated gradually from one color to the other
//        val surfaceColor by animateColorAsState(
//            if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
//        )
//
//        // We toggle the isExpanded variable when we click on this Column
//        Column(modifier = Modifier.clickable { isExpanded = !isExpanded }) {
//            Text(
//                text = user.itemName ?: "Unknown Item",
//                color = MaterialTheme.colorScheme.secondary,
//                style = MaterialTheme.typography.titleSmall
//            )
//
//            Spacer(modifier = Modifier.height(4.dp))
//
//            Surface(
//                shape = MaterialTheme.shapes.medium,
//                shadowElevation = 1.dp,
//                // surfaceColor color will be changing gradually from primary to surface
//                color = surfaceColor,
//                // animateContentSize will change the Surface size gradually
//                modifier = Modifier.animateContentSize().padding(1.dp)
//            ) {
//                Text(
//                    text = user.itemOriginalPrice.toString() ?: "empty Data",
//                    modifier = Modifier.padding(all = 4.dp),
//                    // If the message is expanded, we display all its content
//                    // otherwise we only display the first line
//                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
//                    style = MaterialTheme.typography.bodyMedium
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun Conversation(db: AppDatabase) {
//
//    val dblist by db.ItemsDao().getAll().collectAsState(initial = emptyList())
//
//    LazyColumn {
//        items(dblist) { message ->
//            MessageCard(message)
//        }
//    }
//}