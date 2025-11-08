package com.acms.cinemeteor

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.acms.cinemeteor.ui.theme.CinemeteorTheme

class AboutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CinemeteorTheme {
                Scaffold(modifier = Modifier
                    .fillMaxSize()) { innerPadding ->
                    AboutDesign(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun AboutDesign(modifier: Modifier = Modifier) {

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .padding(horizontal = 12.dp)
            .padding(top = 24.dp)
            .fillMaxWidth()
    ) {
        Row (
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            Icon(
//                        painter = painterResource(R.drawable.linkedin),
                Icons.Default.ArrowBack,
                contentDescription = "Back Button",
                tint = MaterialTheme.colorScheme.tertiary
            )
            Text(
                text = "About Us",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(start = 8.dp, top = 8.dp)
            )
        }
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .fillMaxWidth()
        ) {

            LazyColumn(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.fillMaxWidth()
            ) {

                item {
                    DeveloperCard(
                        img = R.drawable.ammelogo,
                        name = "Ahmed Esmail",
                        role = R.string.app_name,
                        linkedInLink = "",
                        gitHubLink = ""
                    )
                }
                item {
                    DeveloperCard(
                        img = R.drawable.person,
                        name = "Marwan",
                        role = R.string.app_name,
                        linkedInLink = "",
                        gitHubLink = ""
                    )
                }
                item {
                    DeveloperCard(
                        img = R.drawable.mode,
                        name = "Ahmed Mostafa",
                        role = R.string.app_name,
                        linkedInLink = "",
                        gitHubLink = ""

                    )
                }
                item {
                    DeveloperCard(
                        img = R.drawable.settings,
                        name = "Ahmed Reda",
                        role = R.string.app_name,
                        linkedInLink = "",
                        gitHubLink = ""

                    )
                }
                item {
                    DeveloperCard(
                        img = R.drawable.info,
                        name = "Sara",
                        role = R.string.app_name,
                        linkedInLink = "",
                        gitHubLink = ""

                    )
                }
                item {
                    DeveloperCard(
                        img = R.drawable.language,
                        name = "Christine",
                        role = R.string.app_name,
                        linkedInLink = "",
                        gitHubLink = ""

                    )
                }
            }
        }
    }
}

@Composable
fun DeveloperCard(
    img: Int, name: String, role: Int,
    gitHubLink: String, linkedInLink: String,
) {
    val context = LocalContext.current


    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .border(1.dp, Color.LightGray, RoundedCornerShape(18.dp))
    ) {
        Image(
            painter = painterResource(img),
            contentDescription = "$name image",
            modifier = Modifier
                .size(100.dp)
                .padding(8.dp)
                .clip(RoundedCornerShape(12.dp))
        )
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Text(
                text = name,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(start = 8.dp)
//                    textAlign = TextAlign.Center
            )


            Text(
                text = stringResource(role),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(start = 8.dp)
//                textAlign = TextAlign.Center
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { openUrl(context, linkedInLink) }
                ) {
                    Icon(
//                        painter = painterResource(R.drawable.linkedin),
                        Icons.Default.Work,
                        contentDescription = "$name LinkedIn",
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
                IconButton(
                    onClick = { openUrl(context, gitHubLink) }
                ) {
                    Icon(
                        Icons.Default.Code,
                        contentDescription = "$name GitHub",
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}
//}

@Preview(showBackground = true)
@Composable
fun AboutPreview() {
    CinemeteorTheme {
        AboutDesign()
    }
}


fun openUrl(context: Context, link: String) {
    if (link.isNotEmpty())
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
}
