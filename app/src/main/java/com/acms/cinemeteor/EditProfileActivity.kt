package com.acms.cinemeteor

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.os.LocaleListCompat
import coil.compose.rememberAsyncImagePainter
import com.acms.cinemeteor.ui.theme.CinemeteorTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class EditProfileActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val langCode = prefs.getString("lang", "en")
        val localeList =
            if (langCode == "ar") LocaleListCompat.forLanguageTags("ar")
            else LocaleListCompat.forLanguageTags("en")

        AppCompatDelegate.setApplicationLocales(localeList)

        val mode = prefs.getInt("mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(mode)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CinemeteorTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    SettingProfile(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun SettingProfile(modifier: Modifier = Modifier) {

    val user = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current

    var profileImageUrl by remember { mutableStateOf(user?.photoUrl?.toString()) }
    var nameField by remember { mutableStateOf(user?.displayName ?: "") }
    var emailField by remember { mutableStateOf(user?.email ?: "") }
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
            text = stringResource(R.string.edit_profile),
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

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = emailField,
            onValueChange = { emailField = it },
            label = { Text(stringResource(R.string.email)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.weight(1f))

        Button(
            onClick = {
                val updates = userProfileChangeRequest {
                    displayName = nameField
                }

                user?.updateProfile(updates)?.addOnCompleteListener {
                    Toast.makeText(context, (R.string.saved), Toast.LENGTH_SHORT).show()
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

fun uploadToImgBBAndFirebase(
    uri: Uri,
    context: Context,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser ?: run {
        onError("User not logged in")
        return
    }

    val inputStream = context.contentResolver.openInputStream(uri)
    val bytes = inputStream?.readBytes()
    inputStream?.close()

    if (bytes == null) {
        onError("Image read error")
        return
    }

    val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)

    val body = FormBody.Builder()
        .add("key", BuildConfig.IMGBB_API_KEY)
        .add("image", base64)
        .build()

    val request = Request.Builder()
        .url("https://api.imgbb.com/1/upload")
        .post(body)
        .build()

    val client = OkHttpClient()
    val handler = Handler(Looper.getMainLooper())

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            handler.post { onError(e.message ?: "Upload failed") }
        }

        override fun onResponse(call: Call, response: Response) {
            val jsonText = response.body?.string() ?: ""
            val json = JSONObject(jsonText)
            val url = json.getJSONObject("data").getString("display_url")

            val update = userProfileChangeRequest {
                photoUri = Uri.parse(url)
            }

            user.updateProfile(update).addOnCompleteListener { task ->
                handler.post {
                    if (task.isSuccessful) onSuccess(url)
                    else onError(task.exception?.message ?: "Firebase update error")
                }
            }
        }
    })
}

@Composable
fun LoadingDialog(show: Boolean) {
    if (show) {
        Dialog(onDismissRequest = {  }) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(150.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}