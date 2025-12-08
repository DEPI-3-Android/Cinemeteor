package com.acms.cinemeteor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import coil.compose.AsyncImage
import com.acms.cinemeteor.models.Movie
import com.acms.cinemeteor.ui.theme.CinemeteorTheme
import com.acms.cinemeteor.utils.ImageUtils
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

class CloudSavedActivity : AppCompatActivity() {
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
                    CloudSavedDesign()
                }
            }
        }
    }
}

@Composable
fun CloudSavedDesign(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var favoriteMovies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    fun refreshData() {
        isLoading = true
        FirestoreHelper.getFavoriteMovies(onSuccess = { movies ->
            Log.d("CloudDebug", "Firestore returned: ${movies.size} movies")
            favoriteMovies = movies
            isLoading = false
            Toast.makeText(
                context,
                "${context.getString(R.string.synced_movies)} ${movies.size}",
                Toast.LENGTH_SHORT
            ).show()
        }, onError = { error ->
            Log.d("CloudDebug", "Error fetching: $error")
            isLoading = false
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        })
    }
    LaunchedEffect(Unit) { refreshData() }
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isLoading)
    SwipeRefresh(
        state = swipeRefreshState, onRefresh = { refreshData() }) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (favoriteMovies.isEmpty()) {
                Text(
                    text = stringResource(R.string.cloud_saved_empty),
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(favoriteMovies) { movie ->
                        MovieFavCloudItem(
                            movie = movie, modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    LocalSavedMovies.toggleMovie(context, movie)
                                })
                    }
                }
            }
        }
    }
}

@Composable
fun MovieFavCloudItem(
    movie: Movie, modifier: Modifier = Modifier
) {
    val posterUrl = ImageUtils.getPosterUrl(movie.posterPath)
    val context = LocalContext.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(bottom = 0.dp)
            .clickable {
                val intent = Intent(context, FilmActivity::class.java)
                intent.putExtra("movie", movie)
                context.startActivity(intent)
            }) {
        AsyncImage(
            model = posterUrl ?: R.drawable.background_screen,
            contentDescription = movie.title,
            modifier = Modifier
                .width(140.dp)
                .height(200.dp)
                .clip(shape = RoundedCornerShape(16)),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.background_screen),
            error = painterResource(id = R.drawable.background_screen)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = movie.title,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 0.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.Yellow)) {
                    append(stringResource(R.string.movie_rating))
                }
                withStyle(style = SpanStyle()) {
                    append(String.format("%.1f", movie.voteAverage))
                }
            }, fontSize = 11.sp
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CloudSavedPreview() {
    CinemeteorTheme {
        CloudSavedDesign()
        MovieFavCloudItem(
            Movie(
                id = 1,
                title = "Mockup Movie",
                overview = "A thief who steals corporate secrets through the use of dream-sharing technology is given the inverse task of planting an idea into the mind of a C.E.O.",
                posterPath = "", // You can put a URL here or leave it empty/null
                backdropPath = "",
                releaseDate = "2010-07-16",
                voteAverage = 8.8,
                voteCount = 14000,
                popularity = 95.0,
                originalLanguage = "en",
                originalTitle = "Inception"
            )
        )
    }
}

