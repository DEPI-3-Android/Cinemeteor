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
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.os.LocaleListCompat
import com.acms.cinemeteor.ui.theme.CinemeteorTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

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
         Log.d("STARTUP_DEBUG", "onCreate: lang=${langCode}, mode=$mode, AppCompat default=${AppCompatDelegate.getDefaultNightMode()}")


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

private fun onLogoutClick(context: Context) {
    val auth = Firebase.auth
    val myPrefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    with(myPrefs.edit()) {
        putBoolean("isLoggedIn", false)
        apply()
    }
    auth.signOut()
    val intent = Intent(context, LoginActivity::class.java)
    (context as? Activity)?.finishAffinity()
    context.startActivity(intent)
}

@Composable
fun SettingsDesign(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var showLangDialog by remember { mutableStateOf(false) }

    if (showLangDialog) {
        LanguageSetupDialog(
            prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE),
            onDismiss = { showLangDialog = false }
        )
    }



    if (showDialog) {
        ModeSetupDialog(
            prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE),
            onDismiss = { showDialog = false }
        )
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {

        TextButton(onClick = { showDialog = true }) {
            Image(
                painter = painterResource(R.drawable.mode),
                contentDescription = "Theme"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "App Theme")
        }

        Divider(
            color = Color.LightGray,
            thickness = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )

        TextButton(onClick = {showLangDialog = true}) {
            Image(
                painter = painterResource(R.drawable.language),
                contentDescription = "Language"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "App Language")
        }

        Divider(
            color = Color.LightGray,
            thickness = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )

        Button(
            onClick = { onLogoutClick(context) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.logout),
                fontSize = 20.sp
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
                        Text(text = stringResource(id = R.string.cancel), color = Color(0xFF2196F3))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = {
                        editor.putInt("mode", selectedMode).apply()
                        AppCompatDelegate.setDefaultNightMode(selectedMode)

                        (context as? Activity)?.recreate()

                        onDismiss()
                    }) {
                        Text(text = stringResource(id = R.string.ok), color = Color(0xFF2196F3))
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
    val currentLang = prefs.getString("lang", "system") ?: "system"

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
                        text = stringResource(R.string.system_default),
                        selected = selectedLang == "system"
                    ) { selectedLang = "system" }

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
                        Text(text = stringResource(id = R.string.cancel), color = Color(0xFF2196F3))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = {
                        Log.d("LANG_DEBUG", "Selected lang before save: $selectedLang")
                        editor.putString("lang", selectedLang).apply()

                        val localeList = when (selectedLang) {
                            "en" -> LocaleListCompat.forLanguageTags("en")
                            "ar" -> LocaleListCompat.forLanguageTags("ar")
                            else -> LocaleListCompat.getEmptyLocaleList()
                        }

                        AppCompatDelegate.setApplicationLocales(localeList)
                        (context as? Activity)?.recreate()
                        Log.d("LANG_DEBUG", "setApplicationLocales called: $selectedLang")
                        Log.d("LANG_DEBUG", "recreate() after setApplicationLocales")


                        onDismiss()
                    }) {
                        Text(text = stringResource(id = R.string.ok), color = Color(0xFF2196F3))
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
                selectedColor = Color(0xFF2196F3),
                unselectedColor = Color.Gray
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsPreview() {
    SettingsDesign()
}

