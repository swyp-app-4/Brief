package com.example.brife

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.brife.feature.onboarding.SplashScreen
import com.example.brife.ui.theme.BrifeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BrifeTheme {
                SplashScreen()
            }
        }
    }
}