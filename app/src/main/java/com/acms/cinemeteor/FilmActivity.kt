package com.acms.cinemeteor

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.acms.cinemeteor.models.Movie
import com.acms.cinemeteor.ui.theme.CinemeteorTheme
import com.acms.cinemeteor.utils.ImageUtils

class FilmActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        val movie: Movie? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("movie", Movie::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("movie") as? Movie
        }

        setContent {
            CinemeteorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        if (movie != null) {
                            FilmDetailsScreen(movie)
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No movie data found", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun FilmDetailsScreen(movie: Movie) {
    val posterUrl = movie.posterPath?.let { ImageUtils.getPosterUrl(it) }
    val background = R.drawable.background

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = background),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = posterUrl ?: R.drawable.background,
                contentDescription = movie.title,
                modifier = Modifier
                    .width(200.dp)
                    .height(300.dp),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.background),
                error = painterResource(id = R.drawable.background)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = movie.title.ifEmpty { "No Title" },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "⭐ ${movie.voteAverage}   |   ${movie.releaseDate ?: "Unknown"}",
                color = Color.Yellow,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = movie.overview.ifEmpty { "No description available." },
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 22.sp,
                textAlign = TextAlign.Justify
            )
        }
    }
}
