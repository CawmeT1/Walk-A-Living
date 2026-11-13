package com.example.walk_a_living

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.walk_a_living.ui.MainScreen
import com.example.walk_a_living.ui.theme.WalkALivingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WalkALivingTheme {
                MainScreen()
            }
        }
    }
}
