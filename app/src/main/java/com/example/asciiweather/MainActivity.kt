package com.example.asciiweather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "BOM ACCESS-G ASCII WEATHER",
                        color = Color.Green,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.sp))
                    Text(
                        text = " \\  |  / \n _ O _ \n /  |  \\ ",
                        color = Color.Yellow,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.height(16.sp))
                    Text(
                        text = "STATUS: ONLINE (24.5°C)",
                        color = Color.Green,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
