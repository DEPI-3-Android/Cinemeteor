package com.acms.cinemeteor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import coil.compose.rememberAsyncImagePainter
import com.acms.cinemeteor.ui.theme.CinemeteorTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.firestore

class CreateAccountActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val langCode = prefs.getString("lang", "en")
        val localeList = if (langCode == "ar") LocaleListCompat.forLanguageTags("ar")
        else LocaleListCompat.forLanguageTags("en")

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
        "name" to name, "gender" to gender, "email" to Firebase.auth.currentUser?.email
    )
    // Collection = "users"
    store.collection("users").document(userId).set(userMap)
}

@Composable
fun CreateAccountDesign(modifier: Modifier = Modifier) {

    val user = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current

    var profileImageUrl by remember { mutableStateOf(user?.photoUrl?.toString()) }
    var nameField by remember { mutableStateOf(user?.displayName ?: "") }
    var isLoading by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            isLoading = true
            uploadToImgBBAndFirebase(
                uri = it,
                context = context,
                onSuccess = { url ->
                    profileImageUrl = url
                    isLoading = false
                },
                onError = { msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    isLoading = false

                }
            )
        }
    }


    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {

        LoadingDialog(show = isLoading)


        Text(
            text = stringResource(R.string.create_account),
            fontSize = 24.sp
        )

        Spacer(Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter =
                    if (profileImageUrl != null)
                        rememberAsyncImagePainter(profileImageUrl)
                    else
                        painterResource(R.drawable.user),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            TextButton(onClick = { imagePicker.launch("image/*") }) {
                Text(stringResource(R.string.edit))
            }
        }

        Spacer(Modifier.height(30.dp))

        OutlinedTextField(
            value = nameField,
            onValueChange = { nameField = it },
            label = { Text(stringResource(R.string.name)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.weight(1f))

        Button(
            enabled = nameField.isNotBlank(),
            onClick = {
                val updates = userProfileChangeRequest {
                    displayName = nameField
                }

                user?.updateProfile(updates)?.addOnCompleteListener {
                    Toast.makeText(context,(R.string.saved), Toast.LENGTH_SHORT).show()
                }

                val I = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Text(stringResource(R.string.save), fontSize = 16.sp)
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
