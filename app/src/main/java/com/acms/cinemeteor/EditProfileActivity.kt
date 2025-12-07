package com.acms.cinemeteor

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.acms.cinemeteor.ui.components.LoadingScreen
import com.acms.cinemeteor.ui.theme.CinemeteorTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.Box
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class EditProfileActivity : AppCompatActivity() {
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
                    EditProfileDesign(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun EditProfileDesign(modifier: Modifier = Modifier) {
    val user = Firebase.auth.currentUser
    val context = LocalContext.current
    val activity = context as? Activity

    var nameField by remember { mutableStateOf("") }
    var emailField by remember { mutableStateOf("") }
    var isLoadingUserData by remember { mutableStateOf(true) }
    
    // Load user data when screen loads
    LaunchedEffect(Unit) {
        try {
            if (user != null) {
                // Reload user to get latest data
                suspendCancellableCoroutine<Unit> { continuation ->
                    user.reload().addOnCompleteListener { reloadTask ->
                        val currentUser = Firebase.auth.currentUser
                        nameField = currentUser?.displayName ?: ""
                        emailField = currentUser?.email ?: ""
                        isLoadingUserData = false
                        continuation.resume(Unit)
                    }
                }
            } else {
                // If user is null, no reload needed
                nameField = ""
                emailField = ""
                isLoadingUserData = false
            }
        } catch (e: Exception) {
            // Fallback to current user data
            nameField = user?.displayName ?: ""
            emailField = user?.email ?: ""
            isLoadingUserData = false
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        // Loading screen overlay - shows first until all user data is loaded
        LoadingScreen(
            isLoading = isLoadingUserData,
            message = null,
            modifier = Modifier.fillMaxSize()
        )
        
        // Content shown only after loading is complete
        if (!isLoadingUserData) {
                    Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 32.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.edit_profile),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .padding(start = 8.dp, top = 8.dp)
                            .weight(1f, true)
                    )
                }
            }
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
                    .padding(top = 32.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 28.dp)
                ) {
            Image(
                painter = painterResource(R.drawable.user),
                contentDescription = "Profile icon",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary),
                modifier = Modifier
                    .size(160.dp)
                    .padding(top = 12.dp)

            )
            TextButton(
                onClick = {},
                modifier = Modifier
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    text = stringResource(R.string.edit),
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            TextField(
                value = nameField,
                onValueChange = { nameField = it },
                label = { Text(stringResource(R.string.name)) },
                modifier = Modifier
                    .padding(vertical = 20.dp)
                    .fillMaxWidth()
            )
            TextField(
                value = emailField,
                onValueChange = { emailField = it },
                label = { Text(stringResource(R.string.email)) },
                modifier = Modifier
                    .padding(bottom = 18.dp)
                    .fillMaxWidth()
            )
        }
                Row(
                    modifier = Modifier.weight(1f, true),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Button(
                onClick = {
                    val profileUpdates = userProfileChangeRequest {
                        displayName = nameField
                    }
                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show()
                                Log.d("SignUpScreen", "User display name updated to $nameField")
                            } else {
                                Log.w(
                                    "SignUpScreen",
                                    "Failed to update display name: ${updateTask.exception?.message}"
                                )
                            }
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    text = stringResource(R.string.save),
                    fontSize = 16.sp
                    )
                }
            }
        }
        }
    }
}

@Preview(showBackground = true , showSystemUi = true)
@Composable
fun EditProfilePreview() {
    CinemeteorTheme {
        EditProfilePreview()
    }
}