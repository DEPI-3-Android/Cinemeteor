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
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SignupActivity : AppCompatActivity() {
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

class LastCharSignup : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        if (text.isEmpty()) {
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

data class PasswordCriteria(
    val containMinimumLength: Boolean,
    val containLowerCase: Boolean,
    val containUpperCase: Boolean,
    val containDigit: Boolean,
    val containSpecialCharacter: Boolean
)

enum class StrengthLevel() {
    NONE, WEAK, MEDIUM, STRONG
}

private fun calculatePasswordStrength(password: String): StrengthLevel {
    if (password.isBlank()) return StrengthLevel.NONE
    var score = 0
    if (password.length >= 8) score++
    if (password.any { it.isLowerCase() }) score++
    if (password.any { it.isUpperCase() }) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { !it.isLetterOrDigit() }) score++
    return when (score) {
        0, 1 -> StrengthLevel.WEAK     // 0% - 25%
        2, 3 -> StrengthLevel.MEDIUM   // 50% - 75%
        else -> StrengthLevel.STRONG    // 100&
    }
}

private fun checkPasswordCriteria(password: String): PasswordCriteria {
    return PasswordCriteria(
        containMinimumLength = password.length >= 8,
        containLowerCase = password.any { it.isLowerCase() },
        containUpperCase = password.any { it.isUpperCase() },
        containDigit = password.any { it.isDigit() },
        containSpecialCharacter = password.any { !it.isLetterOrDigit() }
    )
}

private fun onSignupClick(
    context: Context,
    emailField: String,
    passwordField: String,
    confirmPasswordField: String
) {
    val auth = Firebase.auth
    if (passwordField == confirmPasswordField) {
        auth.createUserWithEmailAndPassword(emailField, passwordField)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.w("SignUpScreen", "Creating Email Success")
                    val newUser = auth.currentUser
                    newUser!!.sendEmailVerification()
                        .addOnCompleteListener { verificationTask ->
                            if (verificationTask.isSuccessful) {
                                Log.w("SignUpScreen", "Verification Email Success")
                                Toast.makeText(
                                    context,
                                    R.string.verification_sent,
                                    Toast.LENGTH_LONG
                                ).show()
                                val I = Intent(context, CreateAccountActivity::class.java)
                                context.startActivity(I)
                            } else {
                                Log.w("SignUpScreen", "Verification Email Failed")
                                Toast.makeText(
                                    context,
                                    R.string.verification_failed,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    // Ending of verification
                } else {
                    Log.w("SignUpScreen", "Creating Email Failed ${task.exception?.message}")
                    Toast.makeText(
                        context,
                        "${context.getString(R.string.signup_failed)} ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    } else Toast.makeText(context, R.string.password_not_match, Toast.LENGTH_SHORT).show()
}

suspend fun signUpWithGoogle(credential: Credential): Boolean {
    if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        try {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = googleIdTokenCredential.idToken
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = Firebase.auth.signInWithCredential(firebaseCredential).await()
            Log.d("Auth", "Firebase sign-in success: ${authResult.user?.uid}")
            return true
        } catch (e: Exception) {
            Log.e("Auth", "Firebase sign-in failed ${e.message}", e)
            return false
        }
    } else {
        Log.e("Auth", "Unexpected credential type")
        return false
    }
}

@Composable
fun RequirementItems(text: String, isExist: Boolean) {
    val color by animateColorAsState(
        targetValue = if (isExist) Color(0xFF11C700) else Color.Red,
        label = "color"
    )
    Row(
        modifier = Modifier
            .padding(vertical = 2.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = if (isExist)
                painterResource(R.drawable.baseline_check_24)
            else painterResource(
                R.drawable.baseline_close_24
            ),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = color,
            fontSize = 12.sp
        )
    }
}

@Suppress("DEPRECATION")
@Composable
fun SignupDesign(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scroll = rememberScrollState()
    var emailField by rememberSaveable { mutableStateOf("") }
    var passwordField by rememberSaveable { mutableStateOf("") }
    var confirmPasswordField by rememberSaveable { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = CredentialManager.create(context)
    var passwordVisibility by remember { mutableStateOf(false) }
    var confirmPasswordVisibility by remember { mutableStateOf(false) }
    val strength = calculatePasswordStrength(passwordField) //Linear Indicator
    val isEmailValid by remember(emailField) {
        derivedStateOf {
            emailField.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(emailField).matches()
        }
    }
    val (targetProgress, targetColor, labelText) = when (strength) {
        StrengthLevel.NONE -> Triple(0f, Color.Gray, "")
        StrengthLevel.WEAK -> Triple(0.33f, Color.Red, "Weak")
        StrengthLevel.MEDIUM -> Triple(0.66f, Color.Yellow, "Medium")
        StrengthLevel.STRONG -> Triple(1f, Color.Green, "Strong")
    }
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        label = "ProgessAnimation"
    )
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        label = "ColorAnimation"
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.cinemeteor_red),
                contentDescription = "Background",
                modifier = Modifier.size(200.dp),
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = if (isSystemInDarkTheme()) Color.White else Color.Black)) {
                        append(" ${stringResource(R.string.welcome)}")
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
            OutlinedTextField(
                value = emailField,
                onValueChange = { emailField = it },
                label = { Text("${stringResource(R.string.email)}") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = emailField.isNotBlank() && !isEmailValid,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .width(500.dp)
            )
            OutlinedTextField(
                value = passwordField,
                onValueChange = { passwordField = it },
                label = { Text("${stringResource(R.string.password)}") },
                singleLine = true,
                modifier = Modifier
                    .padding(top = 10.dp)
                    .width(500.dp),
                // Upgrading Security
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (passwordVisibility) {
                    VisualTransformation.None
                } else LastCharSignup(),
                trailingIcon = {
                    val image = if (passwordVisibility) {
                        R.drawable.baseline_visibility_off_24
                    } else {
                        R.drawable.baseline_visibility_24
                    }
                    val description = if (passwordVisibility) "Hide password" else "Show password"
                    IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                        Icon(
                            painter = painterResource(image),
                            contentDescription = description,
                            tint = if (isSystemInDarkTheme()) Color.White else Color.Black
                        )
                    }
                }
            )
            if (strength != StrengthLevel.NONE) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    color = animatedColor,
                    trackColor = Color.LightGray.copy(0.3f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp) // thickness
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            OutlinedTextField(
                value = confirmPasswordField,
                onValueChange = { confirmPasswordField = it },
                label = { Text("${stringResource(R.string.confirm_password)}") },
                singleLine = true,
                modifier = Modifier
                    .padding(top = 10.dp)
                    .width(500.dp),
                // Upgrading Security
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (confirmPasswordVisibility) {
                    VisualTransformation.None
                } else LastCharSignup(),
                trailingIcon = {
                    val image = if (confirmPasswordVisibility) {
                        R.drawable.baseline_visibility_off_24
                    } else {
                        R.drawable.baseline_visibility_24
                    }
                    val description =
                        if (confirmPasswordVisibility) "Hide password" else "Show password"
                    IconButton(onClick = {
                        confirmPasswordVisibility = !confirmPasswordVisibility
                    }) {
                        Icon(
                            painter = painterResource(image), // 'image' comes from your if/else
                            contentDescription = description,
                            tint = if (isSystemInDarkTheme()) Color.White else Color.Black
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(4.dp))
            RequirementItems(
                text = "Minimum 8 characters",
                isExist = checkPasswordCriteria(passwordField).containMinimumLength
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    RequirementItems(
                        text = "One lower case",
                        isExist = checkPasswordCriteria(passwordField).containLowerCase
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    RequirementItems(
                        text = "One upper case",
                        isExist = checkPasswordCriteria(passwordField).containUpperCase
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    RequirementItems(
                        text = "One digit",
                        isExist = checkPasswordCriteria(passwordField).containDigit
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    RequirementItems(
                        text = "One special character",
                        isExist = checkPasswordCriteria(passwordField).containSpecialCharacter
                    )
                }
            }
            Button(
                onClick = {
                    onSignupClick(
                        context,
                        emailField,
                        passwordField,
                        confirmPasswordField
                    )
                },
                enabled = emailField.isNotEmpty() && passwordField.isNotEmpty() && confirmPasswordField.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                shape = RoundedCornerShape(40),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE21220),
                    contentColor = Color.White
                )
            ) {
                Text(
                    "${stringResource(R.string.sign_up)}",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Divider(
                    thickness = 1.dp,
                    color = Color(0xFFE21220),
                    modifier = Modifier.weight(1f, true)
                )
                Text(
                    text = stringResource(R.string.or),
                    color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 12.dp)
                )
                Divider(
                    thickness = 1.dp,
                    color = Color(0xFFE21220),
                    modifier = Modifier.weight(1f, true)
                )
            }
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
                            val isSuccess = signUpWithGoogle(result.credential)
                            if (isSuccess) {
                                val I = Intent(context, CreateAccountActivity::class.java)
                                context.startActivity(I)
                            } else {
                                Toast.makeText(context, "Sign up failed", Toast.LENGTH_SHORT)
                                    .show()
                            }
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
                    containerColor = Color.Transparent,
                    contentColor = if (isSystemInDarkTheme()) Color.White else Color.Black
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
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                fontSize = 18.sp,
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = if (isSystemInDarkTheme()) Color.White else Color.Black)) {
                        append("${stringResource(R.string.old_user)} ")
                    }
                    withLink(
                        LinkAnnotation.Clickable(
                            tag = "go_to_login",
                            linkInteractionListener = {
                                val I = Intent(context, LoginActivity::class.java)
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
                            append("${stringResource(R.string.login)}")
                        }
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SignupPreview() {
    CinemeteorTheme {
        SignupDesign()
    }
}
