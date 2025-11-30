package com.acms.cinemeteor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.acms.cinemeteor.ui.theme.CinemeteorTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginActivity : AppCompatActivity() {
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
                    LoginDesign(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

class LastChar : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        if (text.isBlank()) {
            return TransformedText(text, OffsetMapping.Identity)
        }
        val transformedText = buildAnnotatedString {
            repeat(text.length - 1) {
                append('•')
            }
            append(text.last())
        }
        return TransformedText(transformedText, OffsetMapping.Identity)
    }
}

private fun resetPassword(emailField: String) {
    val auth by lazy { FirebaseAuth.getInstance() }
    auth.sendPasswordResetEmail(emailField)
}

private fun checkProfile(context: Context) {
    val auth = Firebase.auth
    val store = Firebase.firestore
    val userId = auth.currentUser?.uid
    if (userId == null) return
    store.collection("users").document(userId).get()
        .addOnSuccessListener { document ->
            val name = document.getString("name")
            val gender = document.getString("gender")
            if (document.exists() && !name.isNullOrEmpty() && !gender.isNullOrEmpty()) {
                // Profile is complete
                val I = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                context.startActivity(I)
                val authPrefs = context.getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)
                with(authPrefs.edit()) {
                    putBoolean("isLoggedIn", true)
                    apply()
                }
            } else {
                // Profile is not completed
                val intent = Intent(context, CreateAccountActivity::class.java)
                context.startActivity(intent)
            }
        }
        .addOnFailureListener { e ->
            Log.e("Auth", "Error checking database", e)
            Toast.makeText(context, "Connection Error", Toast.LENGTH_SHORT).show()
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

                    // ⭐ SAVE USER UID HERE
                    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
                    prefs.edit().putString("current_user_uid", oldUser.uid).apply()

                    Log.d("LoginScreen", "Login Successful")

                    // Redirect
                    val I = Intent(context, MainActivity::class.java)
                    I.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(I)
                } else if (oldUser != null && !oldUser.isEmailVerified) {
                    Log.w("LoginScreen", "Verification Required")
                    Toast.makeText(context, R.string.verification_required, Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
}


suspend fun loginWithGoogle(credential: Credential): AuthResult? {
    if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        try {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = googleIdTokenCredential.idToken
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            // Return the result object directly
            return Firebase.auth.signInWithCredential(firebaseCredential).await()
        } catch (e: Exception) {
            Log.e("Auth", "Firebase sign-in failed", e)
            return null
        }
    } else {
        Log.e("Auth", "Unexpected credential type")
        return null
    }
}

@Composable
fun LoginDesign(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var emailField by remember { mutableStateOf("") }
    var passwordField by remember { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = CredentialManager.create(context)
    val isEmailValid by remember(emailField) {
        derivedStateOf {
            emailField.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(emailField).matches()
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
    )
    {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.cinemeteor_red),
                contentDescription = "red_logo",
                modifier = Modifier
                    .size(200.dp)
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.Black)) {
                        append(" ${stringResource(R.string.welcome_back)}")
                    }
                    withStyle(style = SpanStyle(color = Color(0xFFE21220))) {
                        append(" ${stringResource(R.string.app_name)}")
                    }
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            TextField(
                value = emailField,
                onValueChange = { emailField = it },
                label = { Text("${stringResource(R.string.email)}") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = emailField.isNotBlank() && !isEmailValid,
                modifier = Modifier
                    .padding(top = 18.dp)
                    .width(500.dp)
            )
            TextField(
                value = passwordField,
                onValueChange = { passwordField = it },
                label = { Text("${stringResource(R.string.password)}") },
                singleLine = true,
                modifier = Modifier
                    .padding(top = 18.dp)
                    .width(500.dp),
                // Upgrading Security
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (passwordVisibility) {
                    VisualTransformation.None
                } else LastChar(),
                trailingIcon = {
                    val image = if (passwordVisibility) {
                        R.drawable.baseline_visibility_off_24
                    } else {
                        R.drawable.baseline_visibility_24
                    }
                    val description =
                        if (passwordVisibility) "Hide password" else "Show password"
                    IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                        Image(
                            painter = painterResource(id = image),
                            contentDescription = description
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = { onLoginClick(context, emailField, passwordField) },
                enabled = emailField.isNotBlank() && passwordField.isNotBlank(),
                shape = RoundedCornerShape(40),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE21220),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 20.dp),
            ) {
                Text(
                    "${stringResource(R.string.login)}",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                style = MaterialTheme.typography.bodySmall,
                fontSize = 18.sp,
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color(0xFFE21220))) {
                        append(" ${stringResource(R.string.forget_password)}")
                    }
                },
                modifier = Modifier
                    .clickable(
                        onClick = {
                            if (isEmailValid) {
                                Toast.makeText(
                                    context,
                                    "Password reset link will be sent on ${emailField}",
                                    Toast.LENGTH_LONG
                                ).show()
                                resetPassword(emailField)
                            } else Toast.makeText(
                                context,
                                "Enter valid email to receive password reset link",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Divider(
                    thickness = 1.dp,
                    color = Color(0xFFE21220),
                    modifier = Modifier
                        .weight(1f, true)
                )
                Text(
                    text = stringResource(R.string.or),
                    color = Color(0xFF626262),
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
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedButton(
                onClick = {
                    coroutineScope.launch {
                        try {
                            val googleIdOption = GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId("368922193795-3qgcf0ijphqttcmmd4lp1pc14nbpf391.apps.googleusercontent.com")
                                .setAutoSelectEnabled(true)
                                .build()
                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()
                            val result = credentialManager.getCredential(
                                request = request,
                                context = context
                            )
                            val authResult = loginWithGoogle(result.credential)
                            if (authResult != null) {
                                val user = authResult.user
                                val isNewUser = authResult.additionalUserInfo?.isNewUser == true
                                if (isNewUser) {
                                    user?.delete()
                                    FirebaseAuth.getInstance().signOut()
                                    Toast.makeText(
                                        context,
                                        R.string.nonexistent_account,
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else checkProfile(context)
                            } else Toast.makeText(
                                context,
                                "Google sign in failed",
                                Toast.LENGTH_LONG
                            )
                                .show()
                        } catch (e: GetCredentialException) {
                            Toast.makeText(
                                context,
                                "Credential Error: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Exception Error: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                shape = RoundedCornerShape(40),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Image(
                    painter = painterResource(R.drawable.google),
                    contentDescription = "google logo",
                    modifier = Modifier
                        .size(40.dp)
                        .padding(8.dp)
                )
                Text(
                    "${stringResource(R.string.google)}",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                fontSize = 18.sp,
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color(0xFF626262))) {
                        append("${stringResource(R.string.new_user)} ")
                    }
                    withLink(
                        LinkAnnotation.Clickable(
                            tag = "go_to_signup",
                            linkInteractionListener = {
                                val I = Intent(context, SignupActivity::class.java)
                                context.startActivity(I)
                            }
                        )
                    ) {
                        withStyle(
                            style = SpanStyle(
                                color = Color(0xFFE21220),
                                textDecoration = TextDecoration.None
                            )
                        ) {
                            append("${stringResource(R.string.click_here)}")
                        }
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginPreview() {
    LoginDesign()
}
