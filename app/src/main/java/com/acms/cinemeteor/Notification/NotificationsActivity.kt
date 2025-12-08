package com.acms.cinemeteor.Notification

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await


class NotificationsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                NotificationsScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen() {

    var isLoading by remember { mutableStateOf(true) }
    var notificationsFromFCM by remember { mutableStateOf(listOf<FcmNotification>()) }

    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    LaunchedEffect(userId) {
        if (userId != null) {
            try {
                val db = FirebaseFirestore.getInstance()
                val snapshot = db.collection("users")
                    .document(userId)
                    .collection("Notifications")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val loadedNotifications = snapshot.documents.map { doc ->
                    FcmNotification(
                        title = doc.getString("title") ?: "No Title",
                        message = doc.getString("message") ?: "No Message"
                    )
                }

                notificationsFromFCM = loadedNotifications
                Log.d("NotificationsScreen", "Loaded ${loadedNotifications.size} notifications.")

            } catch (e: Exception) {
                Log.e("NotificationsScreen", "Error fetching notifications: $e")
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
            Log.e("NotificationsScreen", "User not logged in, cannot fetch notifications.")
        }
    }

    Scaffold(

        containerColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            TopAppBar(
                title = {

                    Text("Notifications", color = MaterialTheme.colorScheme.onPrimary)
                },
                colors = TopAppBarDefaults.topAppBarColors(

                    containerColor = MaterialTheme.colorScheme.onBackground,

                )
            )
        },
        content = { padding ->
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {

                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
                notificationsFromFCM.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {

                        Text("No notifications yet", color = MaterialTheme.colorScheme.secondary)
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(notificationsFromFCM) { item ->
                            NotificationCard(item)
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun NotificationCard(item: FcmNotification) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),

        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(text = item.title, style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurface))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = item.message, style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
        }
    }
}


data class FcmNotification(
    val title: String,
    val message: String
)