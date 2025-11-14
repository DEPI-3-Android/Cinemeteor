package com.acms.cinemeteor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.acms.cinemeteor.OnBoardingScreen.OnboardingScreen
import com.acms.cinemeteor.ui.theme.CinemeteorTheme
import com.example.compose.snippets.components.NavigationBarBottom

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val onboardingShown = sharedPref.getBoolean("onboarding_show", false)
        enableEdgeToEdge()
        setContent {
            CinemeteorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

//                    var showSplash by remember { mutableStateOf(true) }
                    var showOnboarding by rememberSaveable { mutableStateOf(!onboardingShown) }

                    if (showOnboarding) {
                        OnboardingScreen(
                            modifier = Modifier.padding(innerPadding),
                            onFinish = {
                                sharedPref.edit().putBoolean("onboarding_show", true).apply()
                                showOnboarding = false
                            }

                        )
                    } else {
                        NavigationBarBottom(modifier = Modifier.padding(innerPadding))
                    }


                }

            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun Greeting(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "Hello MainActivity",
            fontSize = 32.sp
        )
        Button(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 64.dp, end = 16.dp),
            onClick = {
                val I = Intent(context, ProfileActivity::class.java)
                context.startActivity(I)
            }
        ) {
            Text(text = "${stringResource(R.string.settings)}")
        }
    }
}

