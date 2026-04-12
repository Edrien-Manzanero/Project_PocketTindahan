package com.example.project_pockettindahan

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project_pockettindahan.ui.theme.PT_DarkBlue
import com.example.project_pockettindahan.ui.theme.PT_RedAccent
import com.example.project_pockettindahan.ui.theme.Project_PocketTindahanTheme

class DashboardActivity : ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContent{
            Project_PocketTindahanTheme() {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        Surface(
                            color = PT_DarkBlue,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_tindahan_dashboard),
                                contentDescription = "logo",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(all = 10.dp)
                            )
                        }
                    },
                    bottomBar = {
                        Surface(
                            color = PT_DarkBlue,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        painter = painterResource(R.drawable.ic_low_stocks),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .padding(end = 4.dp)
                                    )

                                    Text(
                                        text = "Low Stock: 67",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        painter = painterResource(R.drawable.ic_todays_sales),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .padding(end = 4.dp)
                                    )

                                    Text(
                                        text = "Today: 6,700.00",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                )
                { innerPadding ->
                    DashboardGrid(
                        modifier = Modifier.padding(innerPadding),
                        onItemClick = { storeName ->
                            when (storeName) {
                                "Inventory" -> {
                                    val intent = Intent(
                                        this@DashboardActivity,
                                        InventoryActivity::class.java
                                    )
                                    startActivity(intent)
                                }

                                "Sales History" -> {
                                    val intent = Intent(
                                        this@DashboardActivity,
                                        SalesHistoryActivity::class.java
                                    )
                                    startActivity(intent)
                                }

                                "Products" -> {
                                    val intent = Intent(
                                        this@DashboardActivity,
                                        ProductsActivity::class.java
                                    )
                                    startActivity(intent)
                                }

                                "Debts and Due" -> {
                                    val intent = Intent(
                                        this@DashboardActivity,
                                        DebtsAndDueActivity::class.java
                                    )
                                    startActivity(intent)
                                }

                                "Calculator" -> {
                                    val intent = Intent(
                                        this@DashboardActivity,
                                        CalculatorActivity::class.java
                                    )
                                    startActivity(intent)
                                }

                                "Settings" -> {
                                    val intent = Intent(
                                        this@DashboardActivity,
                                        SettingsActivity::class.java
                                    )
                                    startActivity(intent)
                                }
                            }

                        }
                    )
                }

            }
        }
    }
}

@Composable
fun MenuButton(label:String, iconId:Int, onClick: () -> Unit, modifier:Modifier = Modifier){
    Card(
        border = BorderStroke(width = 4.dp, color = PT_RedAccent),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .aspectRatio(1f)
            .clickable{onClick()}
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = iconId),
                contentDescription = label,
                modifier = Modifier.size(48.dp)
            )

            Text(
                text = label,
                color = PT_DarkBlue,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun DashboardGrid(onItemClick: (String) -> Unit, modifier:Modifier = Modifier){
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxSize()
    ) {
        item { MenuButton(label = "Inventory", iconId = R.drawable.ic_inventory, onClick = {onItemClick("Inventory")}) }
        item { MenuButton(label = "Sales History", iconId = R.drawable.ic_sales_history, onClick = {onItemClick("Sales History")}) }
        item { MenuButton(label = "Products", iconId = R.drawable.ic_products, onClick = {onItemClick("Products")}) }
        item { MenuButton(label = "DebtsAndDue", iconId = R.drawable.ic_debts_and_due, onClick = {onItemClick("Debts and Due")}) }
        item { MenuButton(label = "Calculator", iconId = R.drawable.ic_calculator, onClick = {onItemClick("Calculator")})}
        item { MenuButton(label = "Settings", iconId = R.drawable.ic_settings, onClick = {onItemClick("Settings")})}
    }
}