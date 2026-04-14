package com.example.project_pockettindahan


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight

val buttonList = listOf(
    "C","(",")","\u00F7",
    "7","8","9","x",
    "4","5","6","+",
    "1","2","3","-",
    "AC","0",".","="
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(modifier: Modifier = Modifier, viewModel: CalculatorViewModel) {
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
                            modifier = Modifier.fillMaxSize().padding(5.dp)
                        )
                    }
                },
                modifier = Modifier.shadow(8.dp),
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colorResource(id = R.color.darkBlue)
                )
            )
        }
    ) { innerPadding ->
        Calculator(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            viewModel = viewModel
        )
    }
}
@Composable
fun Calculator(modifier: Modifier = Modifier, viewModel : CalculatorViewModel) {

    val equationText = viewModel.equationText.observeAsState()
    val resultText = viewModel.resultText.observeAsState()

    Box(modifier = modifier) {
        Column(
            modifier = modifier.fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = equationText.value
                    ?.replace("/", "\u00F7")
                    ?.replace("*", "x")
                    ?: "",
                style = TextStyle(
                    fontSize = 30.sp,
                    textAlign = TextAlign.End
                ),
                maxLines = 5,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = resultText.value?:"",
                style = TextStyle(
                    fontSize = 60.sp,
                    textAlign = TextAlign.End
                ),
                maxLines = 2,
            )
            Spacer(modifier = Modifier.height(10.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(4)
            ) {
                items(buttonList) { btn ->
                    CalculatorButton(btn = btn, onClick = {
                        val operator = when (btn) {
                            "\u00F7" -> "/"
                            "x" -> "*"
                            else -> btn
                        }
                        viewModel.onButtonClick(operator)
                    })
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(btn: String, onClick : ()-> Unit){
    Box(modifier = Modifier.padding(8.dp)){
        FloatingActionButton(onClick = onClick,
            modifier = Modifier.aspectRatio(1f),
            shape = CircleShape,
            contentColor = Color.White,
            containerColor = getColor(btn),
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                focusedElevation = 0.dp,
                hoveredElevation = 0.dp
        )
        ){
            Text(text = btn, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        }
    }
}

fun getColor(btn : String) : Color{
    if(btn == "C" || btn == "AC")
        return Color(0xFFF44336)
    if(btn == "(" || btn == ")")
        return Color.Gray
    if(btn == "\u00F7" || btn == "x" || btn == "+" || btn == "-" || btn == "=")
        return Color(0xFFFF9800)
    return Color(0xFF3F51B5)
}
