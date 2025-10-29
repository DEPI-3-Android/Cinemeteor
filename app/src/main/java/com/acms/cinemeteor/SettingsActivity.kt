package com.acms.cinemeteor

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.acms.cinemeteor.ui.theme.CinemeteorTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CinemeteorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SettingsDesign(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
private fun onLogoutClick(context: Context){
    lateinit var auth: FirebaseAuth
    auth = Firebase.auth
    val myPrefs = context.getSharedPreferences("MyAppPrefs" , Context.MODE_PRIVATE)
    with(myPrefs.edit()) {
        putBoolean("isLoggedIn" , false)
        apply()
    }
    auth.signOut()
    val I = Intent(context, LoginActivity::class.java)
    (context as? Activity)?.finishAffinity()
    context.startActivity(I)
}
@Composable
fun SettingsDesign(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Box(modifier = modifier.fillMaxSize()) {
        Button(
            onClick = { onLogoutClick(context) },
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 550.dp)
                .width(300.dp)
        ) {
            Text(
                text = "${stringResource(R.string.logout)}",
                fontSize = 20.sp
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsPreview() {
    SettingsDesign()
}