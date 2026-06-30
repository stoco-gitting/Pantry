package com.pantryplus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pantryplus.ui.navigation.PantryNavHost
import com.pantryplus.ui.theme.PantryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PantryTheme {
                PantryNavHost()
            }
        }
    }
}
