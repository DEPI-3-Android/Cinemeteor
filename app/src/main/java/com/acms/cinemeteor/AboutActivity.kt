package com.acms.cinemeteor

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.acms.cinemeteor.ui.theme.CinemeteorTheme

class AboutActivity : AppCompatActivity() {
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
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                ) { innerPadding ->
                    AboutDesign(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun AboutDesign(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activity = context as? Activity

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .padding(horizontal = 12.dp)
            .padding(top = 24.dp)
            .fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.about),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .weight(1f, true)
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
                        role = R.string.android_dev_ui,
                        linkedInLink = "https://www.linkedin.com/in/ahmedmmesmail/",
                        gitHubLink = "https://github.com/ahmedmmesmail"
                    )
                }
                item {
                    DeveloperCard(
                        img = R.drawable.person,
                        name = "Marwan Amr",
                        role = R.string.android_dev,
                        linkedInLink = "linkedin.com/in/marwan-amr-b240b9314",
                        gitHubLink = "https://github.com/marwanesawy5"
                    )
                }
                item {
                    DeveloperCard(
                        img = R.drawable.ahmedmo,
                        name = "Ahmed Mostafa",
                        role = R.string.android_dev_api,
                        linkedInLink = "https://www.linkedin.com/in/ahmedmostafa-swe",
                        gitHubLink = "https://github.com/ahmedmo-27"

                    )
                }
                item {
                    DeveloperCard(
                        img = R.drawable.person,
                        name = "Ahmed Reda",
                        role = R.string.android_dev,
                        linkedInLink = "",
                        gitHubLink = "https://github.com/ahmedReda-andriod"

                    )
                }
                item {
                    DeveloperCard(
                        img = R.drawable.person,
                        name = "Sara Amged",
                        role = R.string.android_dev,
                        linkedInLink = "",
                        gitHubLink = "https://github.com/saraamged076"

                    )
                }
                item {
                    DeveloperCard(
                        img = R.drawable.person,
                        name = "Christin Medhat",
                        role = R.string.android_dev,
                        linkedInLink = "",
                        gitHubLink = "https://github.com/christinmedhat"

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
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .padding(start = 6.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.linkedin),
                    contentDescription = "$name LinkedIn",
                    modifier = Modifier
                        .clickable(onClick = { openUrl(context, linkedInLink) })
                        .size(32.dp)
                        .padding(4.dp),
                    colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.tertiary)
                )

                Image(
                    painter = painterResource(R.drawable.github),
                    contentDescription = "$name GitHub",
                    colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.tertiary),
                    modifier = Modifier
                        .clickable(onClick = { openUrl(context, gitHubLink) })
                        .size(32.dp)
                        .padding(4.dp)
                )
            }
        }
    }
}

//}

@Preview(showBackground = true , showSystemUi = true)
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
