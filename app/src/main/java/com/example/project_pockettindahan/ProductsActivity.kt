package com.example.project_pockettindahan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class ProductsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CategoryGrid()
        }
    }
}
data class CategoryItem(val title: String, val iconRes: Int)
@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryGrid() {
    val items = listOf(
        CategoryItem("Drinks", R.drawable.ic_drinks),
        CategoryItem("Food", R.drawable.ic_food),
        CategoryItem("Sanitation", R.drawable.ic_sanitation),
        CategoryItem("Hygiene", R.drawable.ic_hygiene),
        CategoryItem("Others", R.drawable.ic_others)
    )

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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(15.dp)
                .border(4.dp, Color(0xFF004399), RoundedCornerShape(16.dp)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(20.dp),
                modifier = Modifier.wrapContentHeight()
            ) {
                items(items) { item ->
                    CategoryButton(
                        title = item.title,
                        iconRes = item.iconRes,
                        onClick = { }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryButton(title: String, iconRes: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(150.dp)
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
                    canvas.drawRoundRect(
                        0f, 0f, size.width, size.height,
                        12.dp.toPx(), 12.dp.toPx(),
                        paint
                    )
                }
            }
            .background(Color(0xFFC62828), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(5.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White, RoundedCornerShape(8.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = title,
                    modifier = Modifier.size(70.dp).padding(bottom = 8.dp),
                    contentScale = ContentScale.Fit
                )
                Text(
                    text = title,
                    color = Color(0xFF0D47A1),
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp
                )
            }
        }
    }
}