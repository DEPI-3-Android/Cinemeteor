package com.acms.cinemeteor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.acms.cinemeteor.ui.theme.CinemeteorTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class LoginActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val langCode = prefs.getString("lang", "system")
        val localeList = when (langCode) {
            "en" -> LocaleListCompat.forLanguageTags("en")
            "ar" -> LocaleListCompat.forLanguageTags("ar")
            else -> LocaleListCompat.getEmptyLocaleList()
        }
        AppCompatDelegate.setApplicationLocales(localeList)
        val mode = prefs.getInt("mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(mode)

        enableEdgeToEdge()
        setContent {
            CinemeteorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginDesign(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

private fun onLoginClick(context: Context, emailField: String, passwordField: String) {
    lateinit var auth: FirebaseAuth
    auth = Firebase.auth
    auth.signInWithEmailAndPassword(emailField, passwordField)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val oldUser = auth.currentUser

                if (oldUser != null && oldUser.isEmailVerified) {
                    Log.d("LoginScreen", "Login Successful")
                    // Saving the logged in State
                    val myPrefs = context.getSharedPreferences("MyAppPrefs" , Context.MODE_PRIVATE)
                    with(myPrefs.edit()) {
                        putBoolean("isLoggedIn" , true)
                        apply() // Saves the changes
                    }
                    val I = Intent(context, MainActivity::class.java)
                    I.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(I)
                } else if (oldUser != null && !oldUser.isEmailVerified) {
                    Log.w("LoginScreen", "Verification Required")
                    Toast.makeText(context, R.string.verification_required, Toast.LENGTH_LONG)
                        .show()
                }
            } else {
                Log.w("LoginScreen", "Login Failed")
                Toast.makeText(context, R.string.login_failed, Toast.LENGTH_LONG).show()
            }
        }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginDesign(modifier: Modifier = Modifier) {
    var emailField by remember { mutableStateOf("") }
    var passwordField by remember { mutableStateOf("") }
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.background_screen),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize()
                .blur(
                    radiusX = 4.dp,
                    radiusY = 4.dp,
                    edgeTreatment = BlurredEdgeTreatment(RoundedCornerShape(0.dp))
                ),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Welcome to ${stringResource(R.string.app_name)}",
                fontSize = 28.sp,
                modifier = Modifier.padding(bottom = 32.dp),
            )
            TextField(
                value = emailField,
                onValueChange = { emailField = it },
                label = { Text("${stringResource(R.string.email)}") },
                modifier = Modifier
                    .padding(bottom = 18.dp)
                    .width(500.dp)
            )
            TextField(
                value = passwordField,
                onValueChange = { passwordField = it },
                label = { Text("${stringResource(R.string.password)}") },
                modifier = Modifier
                    .padding(bottom = 18.dp)
                    .width(500.dp)
            )
            Text(
                text = "${stringResource(R.string.new_user)}",
                fontSize = 18.sp,
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .clickable {
                        val I = Intent(context, SignupActivity::class.java)
                        context.startActivity(I)
                    }
            )
            Text(
                text = "${stringResource(R.string.forget_password)}",
                fontSize = 18.sp,
                modifier = Modifier
                    .padding(bottom = 18.dp)
            )
            Button(
                onClick = { onLoginClick(context, emailField, passwordField) },
                enabled = emailField.isNotBlank() && passwordField.isNotBlank()
            ) {
                Text(
                    "${stringResource(R.string.login)}",
                    fontSize = 20.sp
                )
            }
        }
    }
}
