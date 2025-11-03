package com.acms.cinemeteor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.acms.cinemeteor.ui.theme.CinemeteorTheme

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CinemeteorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(modifier = Modifier.padding(innerPadding))
                    val myPrefs = getSharedPreferences("MyAppPrefs" , Context.MODE_PRIVATE)
                    val isLoggedIn = myPrefs.getBoolean("isLoggedIn" , true)
                    if (isLoggedIn) {
                        val I = Intent(this, MainActivity::class.java)
                        startActivity(I)
                    } else {
                        val I = Intent(this, LoginActivity::class.java)
                        startActivity(I)
                    }
                    finish()
                }
            }
        }
    }
}