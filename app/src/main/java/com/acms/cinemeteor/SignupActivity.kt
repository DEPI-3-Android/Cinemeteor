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

class SignupActivity : ComponentActivity() {
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
                    SignupDesign(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

private fun onSignupClick(
    context: Context,
    emailField: String,
    passwordField: String,
    confirmPasswordField: String
) {
    lateinit var auth: FirebaseAuth
    auth = Firebase.auth
    if (passwordField == confirmPasswordField) {
        auth.createUserWithEmailAndPassword(emailField, passwordField)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.w("SignUpScreen", "Creating Email Success")
                    // Sign up was successful
                    // Get the user and starting verification of his/her email
                    val newUser = auth.currentUser
                    newUser!!.sendEmailVerification()
                        .addOnCompleteListener { verificationTask ->
                            if (verificationTask.isSuccessful) {
                                Log.w("SignUpScreen", "Verification Email Success")
                                Toast.makeText(context, "Open your Inbox", Toast.LENGTH_LONG).show()
//                                Snackbar.make(context, R.string.check_email_inbox).show()
                            } else {
                                Log.w("SignUpScreen", "Verification Email Failed")
                                Toast.makeText(context, "Don't open your inbox", Toast.LENGTH_LONG)
                                    .show()
                            }
                        }
                    // Ending of verification
                } else {
                    Log.w("SignUpScreen", "Creating Email Failed ${task.exception?.message}")
                    Toast.makeText(
                        context,
                        "Sign up failed ! ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    } else {
        Toast.makeText(context, R.string.password_not_match, Toast.LENGTH_SHORT)
            .show()
    }
}

@Composable
fun SignupDesign(modifier: Modifier = Modifier) {
//    val snackbarHostState = remember { SnackbarHostState() }
//    val scope = rememberCoroutineScope()
    var emailField by remember { mutableStateOf("") }
    var passwordField by remember { mutableStateOf("") }
    var confirmPasswordField by remember { mutableStateOf("") }
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.background_screen),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
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
                text = "${stringResource(R.string.welcome) + " " + stringResource(R.string.app_name)}",
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
            TextField(
                value = confirmPasswordField,
                onValueChange = { confirmPasswordField = it },
                label = { Text("${stringResource(R.string.confirm_password)}") },
                modifier = Modifier
                    .padding(bottom = 18.dp)
                    .width(500.dp)
            )
            Text(
                text = "${stringResource(R.string.old_user)}",
                fontSize = 18.sp,
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .clickable {
                        val I = Intent(context, LoginActivity::class.java)
                        context.startActivity(I)
                    }
            )
            Button(
                enabled = emailField.isNotBlank() && passwordField.isNotBlank() && confirmPasswordField.isNotBlank(),
                onClick = {
                    onSignupClick(
                        context,
                        emailField,
                        passwordField,
                        confirmPasswordField
                    )
                }
            ) {
                Text(
                    "${stringResource(R.string.sign_up)}",
                    fontSize = 20.sp
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SignupPreview() {
    SignupDesign()
}
