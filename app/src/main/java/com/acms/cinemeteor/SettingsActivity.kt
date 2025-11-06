package com.acms.cinemeteor

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.os.LocaleListCompat
import com.acms.cinemeteor.ui.theme.CinemeteorTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class SettingsActivity : ComponentActivity() {
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
    var showModeDialog by remember { mutableStateOf(false) }
    var showLangDialog by remember { mutableStateOf(false) }

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
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 32.dp)
    ) {

        TextButton(
            onClick = { showModeDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .align(alignment = Alignment.Start),
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
                    painter = painterResource(R.drawable.mode),
                    contentDescription = "Theme",
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary),
                    modifier = Modifier.size(30.dp)
                )
                Text(
                    text = stringResource(R.string.theme),
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        Divider(
            color = Color.Transparent,
            thickness = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
        )

        TextButton(
            onClick = { showLangDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .align(alignment = Alignment.Start),
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
                    painter = painterResource(R.drawable.language),
                    contentDescription = stringResource(R.string.language),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary),
                    modifier = Modifier.size(30.dp)
                )
                Text(
                    text = stringResource(R.string.language),
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        Divider(
            color = Color.Transparent,
            thickness = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 0.dp)
        )

        TextButton(
            onClick = { },
            modifier = Modifier

                .fillMaxWidth()
                .align(alignment = Alignment.Start),
            shape = RoundedCornerShape(0.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.info),
                    contentDescription = stringResource(R.string.about),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary),
                    modifier = Modifier.size(30.dp)
                )

                Text(
                    text = stringResource(R.string.about),
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        Divider(
            color = Color.Transparent,
            thickness = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 0.dp)
        )

        TextButton(
            onClick = { onLogoutClick(context) },
            modifier = Modifier

                .fillMaxWidth()
                .align(alignment = Alignment.Start),
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
                    painter = painterResource(R.drawable.logout),
                    contentDescription = stringResource(R.string.logout),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary),
                    modifier = Modifier.size(30.dp)
                )

                Text(
                    text = stringResource(R.string.logout),
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
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
                        editor.putString("lang", selectedLang).apply()

                        LanguageChangeHelper().changeLanguage(context, selectedLang)

                        Handler(Looper.getMainLooper()).post {
                            (context as? Activity)?.recreate()
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsPreview() {
    SettingsDesign()
}

