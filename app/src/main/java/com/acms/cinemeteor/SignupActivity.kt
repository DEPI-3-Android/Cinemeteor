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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.acms.cinemeteor.ui.theme.CinemeteorTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest

class SignupActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {

        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val langCode = prefs.getString("lang", "en")
        val localeList = if (langCode == "ar")
            LocaleListCompat.forLanguageTags("ar")
        else
            LocaleListCompat.forLanguageTags("en")

        AppCompatDelegate.setApplicationLocales(localeList)
        val mode = prefs.getInt("mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(mode)

        super.onCreate(savedInstanceState)
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
    nameField: String,
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

                    val newUser = auth.currentUser

                    val profileUpdates = userProfileChangeRequest {
                        displayName = nameField
                    }
                    newUser?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                Log.d("SignUpScreen", "User display name updated to $nameField")
                            } else {
                                Log.w(
                                    "SignUpScreen",
                                    "Failed to update display name: ${updateTask.exception?.message}"
                                )
                            }
                        }

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
    var nameField by remember { mutableStateOf("") }
    var confirmPasswordField by remember { mutableStateOf("") }
    val context = LocalContext.current

    LazyColumn {
        item {
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
                modifier = modifier
                    .padding(horizontal = 12.dp, vertical = 32.dp)
            ) {
                Text(
                    text = buildAnnotatedString {
                        append(" ${stringResource(R.string.welcome)}")
                        withStyle(style = SpanStyle(color = Color(0xFFE21220))) {
                            append(" ${stringResource(R.string.app_name)}")
                        }
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .padding(start = 8.dp, top = 40.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }
        }
        item {
            Column(
                modifier = Modifier
                    .padding(top = 60.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .padding(top = 120.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextField(
                        value = nameField,
                        onValueChange = { nameField = it },
                        label = { Text(stringResource(R.string.name)) },
                        modifier = Modifier
                            .padding(bottom = 18.dp)
                            .width(500.dp)
                    )
                    TextField(
                        value = emailField,
                        onValueChange = { emailField = it },
                        label = { Text(stringResource(R.string.email)) },
                        modifier = Modifier
                            .padding(bottom = 18.dp)
                            .width(500.dp)
                    )
                    TextField(
                        value = passwordField,
                        onValueChange = { passwordField = it },
                        label = { Text(stringResource(R.string.password)) },
                        modifier = Modifier
                            .padding(bottom = 18.dp)
                            .width(500.dp)
                    )
                    TextField(
                        value = confirmPasswordField,
                        onValueChange = { confirmPasswordField = it },
                        label = { Text(stringResource(R.string.confirm_password)) },
                        modifier = Modifier
                            .padding(bottom = 18.dp)
                            .width(500.dp)
                    )
                    Button(
                        enabled = confirmPasswordField.isNotBlank() && passwordField.isNotBlank()
                                && emailField.isNotBlank() && nameField.isNotBlank(),
                        onClick = {
                            onSignupClick(
                                context,
                                nameField,
                                emailField,
                                passwordField,
                                confirmPasswordField
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 16.dp),
                        shape = RoundedCornerShape(40)
                    ) {
                        Text(
                            stringResource(R.string.sign_up),
                            fontSize = 20.sp,
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .padding(vertical = 0.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = buildAnnotatedString {
                            append(" ${stringResource(R.string.old_user)}")
                            withStyle(style = SpanStyle(color = Color(0xFFE21220))) {
                                append(" ${stringResource(R.string.login_now)}")
                            }
                        },
                        fontSize = 16.sp,
                        modifier = Modifier
                            .padding(bottom = 24.dp, top = 32.dp)
                            .clickable {
                                val I = Intent(context, LoginActivity::class.java)
                                context.startActivity(I)
                            }
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Divider(
                            thickness = 1.dp,
                            color = Color(0xFFE21220),
                            modifier = Modifier
                                .weight(1f, true)
                        )
                        Text(
                            text = stringResource(R.string.or),
                            fontSize = 16.sp,
                            modifier = Modifier
                                .padding(vertical = 4.dp, horizontal = 12.dp)
                        )
                        Divider(
                            thickness = 1.dp,
                            color = Color(0xFFE21220),
                            modifier = Modifier
                                .weight(1f, true)
                        )
                    }


                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(28)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.google),
                            contentDescription = "google logo",
                            modifier = Modifier
                                .size(40.dp)
                                .padding(8.dp)
                        )
                        Text(
                            "Google",
                            fontSize = 20.sp,
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SignupPreview() {
    SignupDesign()
}
