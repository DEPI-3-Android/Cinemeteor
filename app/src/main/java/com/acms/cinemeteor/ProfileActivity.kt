package com.acms.cinemeteor

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.os.LocaleListCompat
import com.acms.cinemeteor.BuildConfig
import com.acms.cinemeteor.ui.components.LoadingScreen
import com.acms.cinemeteor.ui.theme.CinemeteorTheme
import com.acms.cinemeteor.utils.LanguageUtils
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.Box
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class ProfileActivity : AppCompatActivity() {
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
                    ProfileDesign(modifier = Modifier.padding(innerPadding))
//                    StatusBarProtection()
                }
            }
        }
    }
}
private fun onLogoutClick(context: Context) {
    val auth = Firebase.auth
    val authPrefs = context.getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)
    with(authPrefs.edit()) {
        putBoolean("isLoggedIn", false)
        apply()
    }
    auth.signOut()
    val intent = Intent(context, LoginActivity::class.java)
    (context as? Activity)?.finishAffinity()
    context.startActivity(intent)
}
@Composable
fun ProfileDesign(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var showModeDialog by remember { mutableStateOf(false) }
    var showLangDialog by remember { mutableStateOf(false) }
    
    var isLoadingUserData by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("No email") }
    var name by remember { mutableStateOf("User Name") }
    var photoUrl by remember { mutableStateOf<android.net.Uri?>(null) }
    
    val user = Firebase.auth.currentUser
    
    // Load user data when screen loads
    LaunchedEffect(Unit) {
        try {
            if (user != null) {
                // Reload user to get latest data
                suspendCancellableCoroutine<Unit> { continuation ->
                    user.reload().addOnCompleteListener { reloadTask ->
                        val currentUser = Firebase.auth.currentUser
                        email = currentUser?.email ?: "No email"
                        name = currentUser?.displayName ?: "User Name"
                        photoUrl = currentUser?.photoUrl
                        isLoadingUserData = false
                        continuation.resume(Unit)
                    }
                }
            } else {
                // If user is null, no reload needed
                email = "No email"
                name = "User Name"
                photoUrl = null
                isLoadingUserData = false
            }
        } catch (e: Exception) {
            // Fallback to current user data
            email = user?.email ?: "No email"
            name = user?.displayName ?: "User Name"
            photoUrl = user?.photoUrl
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
            if (showLangDialog) {
                LanguageSetupDialog(
                    prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE),
                    onDismiss = { showLangDialog = false }
                )
            }
            if (showModeDialog) {
                ModeSetupDialog(
                    prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE),
                    onDismiss = { showModeDialog = false }
                )
            }

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .padding(horizontal = 12.dp, vertical = 32.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.profile),
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
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
            .padding(top = 80.dp)
    ) {

        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 28.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.user),
                contentDescription = "Profile icon",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary),
                modifier = Modifier
                    .size(120.dp)
                    .padding(top = 12.dp)
            )
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Text(
                text = email,
                style = MaterialTheme.typography.titleSmall
            )
        }

        MixedButton(
            icon = R.drawable.person,
            text = R.string.edit_profile,
            onClickAction = {
                context.startActivity(
                    Intent(context, EditProfileActivity::class.java)
                )
            }
        )


        MixedButton(
            icon = R.drawable.language,
            text = R.string.language,
            onClickAction = { showLangDialog = true }
        )

        MixedButton(
            icon = R.drawable.mode,
            text = R.string.theme,
            onClickAction = { showModeDialog = true }
        )

        MixedButton(
            icon = R.drawable.info,
            text = R.string.about,
            onClickAction = {
                context.startActivity(
                    Intent(context, AboutActivity::class.java)
                )
            }
        )

        MixedButton(
            icon = R.drawable.logout,
            text = R.string.logout,
            onClickAction = { onLogoutClick(context) }
        )
            }
        }
    }
}

@Composable
fun MixedButton(
    icon: Int,
    text: Int,
    onClickAction: () -> Unit
) {
    TextButton(
        onClick = { onClickAction() },
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth()
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = stringResource(text),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary),
                modifier = Modifier.size(30.dp)
            )

            Text(
                text = stringResource(text),
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .weight(1f, fill = true)
            )
            Image(
                painter = painterResource(R.drawable.arrow_forward),
                contentDescription = stringResource(text),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary),
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
fun ModeSetupDialog(
    prefs: SharedPreferences,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val editor = prefs.edit()
    val currentMode = prefs.getInt("mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    var selectedMode by remember { mutableStateOf(currentMode) }

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.choose_theme),
                    style = MaterialTheme.typography.titleLarge
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    RadioButtonWithText(
                        text = stringResource(R.string.system_default),
                        selected = selectedMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    ) { selectedMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM }

                    RadioButtonWithText(
                        text = stringResource(R.string.dark),
                        selected = selectedMode == AppCompatDelegate.MODE_NIGHT_YES
                    ) { selectedMode = AppCompatDelegate.MODE_NIGHT_YES }

                    RadioButtonWithText(
                        text = stringResource(R.string.light),
                        selected = selectedMode == AppCompatDelegate.MODE_NIGHT_NO
                    ) { selectedMode = AppCompatDelegate.MODE_NIGHT_NO }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { onDismiss() }) {
                        Text(text = stringResource(id = R.string.cancel), color = Color(0xFFE21220))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = {
                        editor.putInt("mode", selectedMode).apply()
                        AppCompatDelegate.setDefaultNightMode(selectedMode)

                        (context as? Activity)?.recreate()

                        onDismiss()
                    }) {
                        Text(text = stringResource(id = R.string.ok), color = Color(0xFFE21220))
                    }
                }
            }
        }
    }
}

@Composable
fun LanguageSetupDialog(
    prefs: SharedPreferences,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val editor = prefs.edit()
    val currentLang = prefs.getString("lang", "en") ?: "en"

    var selectedLang by remember { mutableStateOf(currentLang) }

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.choose_language),
                    style = MaterialTheme.typography.titleLarge
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    RadioButtonWithText(
                        text = "English",
                        selected = selectedLang == "en"
                    ) { selectedLang = "en" }

                    RadioButtonWithText(
                        text = "العربية",
                        selected = selectedLang == "ar"
                    ) { selectedLang = "ar" }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { onDismiss() }) {
                        Text(text = stringResource(id = R.string.cancel), color = Color(0xFFE21220))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = {
                        val previousLang = currentLang
                        
                        // Refresh saved movies with new language before recreating activity
                        if (previousLang != selectedLang) {
                            val apiKey = BuildConfig.TMDB_API_KEY.trim()
                            if (apiKey.isNotEmpty() && apiKey != "\"\"") {
                                // Calculate the new language code for TMDB API
                                val newLanguageCode = when (selectedLang) {
                                    "en" -> "en-US"
                                    "ar" -> "ar"
                                    else -> "en-US"
                                }
                                // Refresh movies with new language before changing UI language
                                // Use withContext to properly await completion
                                CoroutineScope(Dispatchers.IO).launch {
                                    val refreshResult = LocalSavedMovies.refreshMoviesWithLanguage(
                                        context,
                                        apiKey,
                                        newLanguageCode
                                    )
                                    
                                    refreshResult.onSuccess {
                                        Log.d("ProfileActivity", "Movies refreshed successfully, changing language...")
                                        // After refresh completes, save language preference, change language and recreate
                                        Handler(Looper.getMainLooper()).post {
                                            editor.putString("lang", selectedLang).apply()
                                            LanguageChangeHelper().changeLanguage(context, selectedLang)
                                            onDismiss()
                                            // Give time for SharedPreferences to persist
                                            Handler(Looper.getMainLooper()).postDelayed({
                                                (context as? Activity)?.recreate()
                                            }, 500) // Increased delay to ensure everything completes
                                        }
                                    }.onFailure { exception ->
                                        Log.e("ProfileActivity", "Failed to refresh movies: ${exception.message}")
                                        // Still change language even if refresh fails
                                        Handler(Looper.getMainLooper()).post {
                                            editor.putString("lang", selectedLang).apply()
                                            LanguageChangeHelper().changeLanguage(context, selectedLang)
                                            onDismiss()
                                            Handler(Looper.getMainLooper()).postDelayed({
                                                (context as? Activity)?.recreate()
                                            }, 300)
                                        }
                                    }
                                }
                            } else {
                                // If no API key, just save language preference, change language and recreate
                                editor.putString("lang", selectedLang).apply()
                                LanguageChangeHelper().changeLanguage(context, selectedLang)
                                onDismiss()
                                Handler(Looper.getMainLooper()).post {
                                    (context as? Activity)?.recreate()
                                }
                            }
                        } else {
                            // Language didn't change, just dismiss
                            onDismiss()
                        }

                        onDismiss()
                    }) {
                        Text(text = stringResource(id = R.string.ok), color = Color(0xFFE21220))
                    }

                }
            }
        }
    }
}

@Composable
fun RadioButtonWithText(
    text: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(
                selectedColor = Color(0xFFE21220),
                unselectedColor = Color.Gray
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text)
    }
}
@Preview(showBackground = true)
@Composable
fun ProfilePreview() {
    CinemeteorTheme {
        ProfileDesign()
    }
}
