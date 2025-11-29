package com.acms.cinemeteor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.acms.cinemeteor.ui.theme.CinemeteorTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class CreateAccountActivity : AppCompatActivity() {
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
                    CreateAccountDesign(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

private fun checkEmailVerification(context: Context) {
    val user = FirebaseAuth.getInstance().currentUser
    user?.reload()?.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            if (user.isEmailVerified) {
                val I = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                context.startActivity(I)
                val authPrefs = context.getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)
                with(authPrefs.edit()) {
                    putBoolean("isLoggedIn", true)
                    apply()
                }
            } else Toast.makeText(context, R.string.verification_required, Toast.LENGTH_LONG).show()
        } else Toast.makeText(context, "Error checking verification", Toast.LENGTH_SHORT).show()
    }
}

private fun saveUserProfile(name: String, gender: String) {
    val userId = Firebase.auth.currentUser?.uid ?: return
    val store = Firebase.firestore
    // Data map
    val userMap = hashMapOf(
        "name" to name,
        "gender" to gender,
        "email" to Firebase.auth.currentUser?.email
    )
    // Collection = "users"
    store.collection("users").document(userId).set(userMap)
}
@Composable
fun CreateAccountDesign(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var nameField by remember { mutableStateOf("") }
    var expand by remember { mutableStateOf(false) }
    var selectedGender by remember { mutableStateOf("Gender") }
    val expandList = listOf("Male", "Female")
    val rotationState by animateFloatAsState(
        targetValue = if (expand) 180f else 0f,
        label = "Selection Array Rotation"
    )
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 64.dp),
        )
        {
            Text(
                text = "Fill your profile",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            OutlinedTextField(
                value = nameField,
                onValueChange = { nameField = it },
                label = { Text("Enter your name") },
                singleLine = true,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .width(500.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                OutlinedTextField(
                    value = selectedGender,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Select Gender") },
                    trailingIcon = {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Drop Down Arrow",
                            modifier = Modifier.rotate(rotationState)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expand = !expand },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { expand = !expand }
                )
                DropdownMenu(
                    expanded = expand,
                    onDismissRequest = { expand = false },
                    modifier = Modifier.fillMaxWidth(0.87f)
                ) {
                    expandList.forEach { label ->
                        DropdownMenuItem(
                            text = { Text(text = label) },
                            onClick = {
                                selectedGender = label
                                expand = false
                            }
                        )
                    }
                }
            }
            Button(
                enabled = nameField.isNotBlank(),
                onClick = {
                    saveUserProfile(nameField, selectedGender)
                    checkEmailVerification(context)
                },
                shape = RoundedCornerShape(40),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE21220),
                    contentColor = Color.White
                ),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 20.dp),
            ) {
                Text(
                    "${stringResource(R.string.create_account)}",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CreateAccountPreview() {
    CinemeteorTheme {
        CreateAccountDesign()
    }
}
