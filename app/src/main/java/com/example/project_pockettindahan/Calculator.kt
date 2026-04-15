package com.example.project_pockettindahan

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val buttonList = listOf("C","(",")","\u00F7","7","8","9","x","4","5","6","+","1","2","3","-","AC","0",".","=")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(viewModel: CalculatorViewModel) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Surface(shape = CircleShape, color = Color.White, modifier = Modifier.size(45.dp)) {
                        Image(painter = painterResource(id = R.drawable.img), contentDescription = "Logo", modifier = Modifier.fillMaxSize().padding(5.dp))
                    }
                },
                modifier = Modifier.shadow(8.dp),
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = colorResource(id = R.color.darkBlue))
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize().padding(10.dp)) {
            val equation = viewModel.equationText.observeAsState("")
            val result = viewModel.resultText.observeAsState("0")

            Column(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Bottom) {
                Text(text = equation.value, color = MaterialTheme.colorScheme.onSurface, style = TextStyle(fontSize = 30.sp, textAlign = TextAlign.End))
                Text(text = result.value, color = MaterialTheme.colorScheme.onSurface, style = TextStyle(fontSize = 60.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End))
            }
            LazyVerticalGrid(columns = GridCells.Fixed(4)) {
                items(buttonList) { btn ->
                    CalculatorButton(btn) { viewModel.onButtonClick(btn) }
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(btn: String, onClick: () -> Unit) {
    Box(modifier = Modifier.padding(5.dp)) {
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier.aspectRatio(1f),
            shape = CircleShape,
            containerColor = when {
                btn == "C" || btn == "AC" -> Color(0xFFF44336)
                btn == "=" || btn == "+" || btn == "-" || btn == "x" || btn == "\u00F7" -> Color(0xFFFF9800)
                else -> Color(0xFF3F51B5)
            },
            contentColor = Color.White
        ) { Text(text = btn, fontSize = 24.sp, fontWeight = FontWeight.Bold) }
    }
}