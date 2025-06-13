package com.sandhirizki0088.snaptrip

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sandhirizki0088.snaptrip.ui.theme.SnapTripTheme
import com.sandhirizki0088.snaptrip.ui.theme.screen.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SnapTripTheme {
                MainScreen()
            }
        }
    }
}
