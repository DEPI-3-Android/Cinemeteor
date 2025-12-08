package com.acms.cinemeteor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import coil.compose.AsyncImage
import com.acms.cinemeteor.models.Movie
import com.acms.cinemeteor.repository.MovieRepository
import com.acms.cinemeteor.ui.components.LoadingScreen
import com.acms.cinemeteor.ui.theme.CinemeteorTheme
import com.acms.cinemeteor.utils.ImageUtils
import com.acms.cinemeteor.utils.LanguageUtils
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class FavouriteActivity : AppCompatActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CinemeteorTheme {
                Scaffold(topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = "Favorite Films",
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        },
                        colors = TopAppBarDefaults.largeTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
                ) { paddingValues ->
                    FavoriteActivityDesign(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )
                }
            }
        }
    }
}

object LocalSavedMovies {
    private const val PREFS_NAME = "cinemeteor_prefs"
    private const val KEY_SAVED_MOVIES = "saved_movies"
    private val gson = Gson()
    fun getSavedMovies(context: Context): List<Movie> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_SAVED_MOVIES, null) ?: return emptyList()
        val type = object : TypeToken<List<Movie>>() {}.type
        return gson.fromJson(json, type)
    }

    private fun saveMoviesList(context: Context, movies: List<Movie>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SAVED_MOVIES, gson.toJson(movies)).apply()
    }

    fun toggleMovie(context: Context, movie: Movie) {
        val current = getSavedMovies(context).toMutableList()
        if (current.any { it.id == movie.id }) {
            current.removeAll { it.id == movie.id }
        } else {
            current.add(movie)
        }
        saveMoviesList(context, current)
    }

    fun isMovieSaved(context: Context, movieId: Int): Boolean {
        return getSavedMovies(context).any { it.id == movieId }
    }


    fun removeMovie(context: Context, movieId: Int) {
        val current = getSavedMovies(context).toMutableList()
        current.removeAll { it.id == movieId }
        saveMoviesList(context, current)
    }


    fun clearAll(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_SAVED_MOVIES).apply()
    }

    /**
     * Refreshes all saved movies with the current language from SharedPreferences
     * Fetches updated movie details for each saved movie and updates them concurrently
     */
    suspend fun refreshMoviesWithLanguage(
        context: Context,
        apiKey: String,
        language: String
    ): Result<Unit> {
        return try {
            val savedMovies = getSavedMovies(context)
            if (savedMovies.isEmpty()) {
                Log.d("LocalSavedMovies", "No saved movies to refresh")
                return Result.success(Unit)
            }
            Log.d(
                "LocalSavedMovies",
                "Refreshing ${savedMovies.size} saved movies with language: $language"
            )
            val repository = MovieRepository()
            // Fetch updated details for each movie concurrently using async
            val updatedMovies = coroutineScope {
                savedMovies.map { movie ->
                    async {
                        // Use getMovieDetailsWithFallback to ensure title/overview are filled
                        val result =
                            repository.getMovieDetailsWithFallback(apiKey, movie.id, language)
                        result.fold(
                            onSuccess = { updatedMovie: Movie ->
                                Log.d(
                                    "LocalSavedMovies",
                                    "Updated movie ${movie.id}: ${updatedMovie.title}"
                                )
                                updatedMovie
                            },
                            onFailure = { exception: Throwable ->
                                Log.e(
                                    "LocalSavedMovies",
                                    "Failed to update movie ${movie.id}: ${exception.message}"
                                )
                                // Keep original movie if update fails
                                movie
                            }
                        )
                    }
                }.awaitAll()
            }
            // Save updated movies
            if (updatedMovies.isNotEmpty()) {
                saveMoviesList(context, updatedMovies)
                Log.d(
                    "LocalSavedMovies",
                    "Successfully refreshed ${updatedMovies.size} movies with language: $language"
                )
            }
            // Small delay to ensure data is persisted
            kotlinx.coroutines.delay(100)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("LocalSavedMovies", "Exception refreshing movies", e)
            Result.failure(e)
        }
    }

    /**
     * Refreshes all saved movies using the current language from SharedPreferences
     */
    suspend fun refreshMoviesWithCurrentLanguage(
        context: Context,
        apiKey: String
    ): Result<Unit> {
        val language = LanguageUtils.getLanguageCode(context)
        return refreshMoviesWithLanguage(context, apiKey, language)
    }
}
@Composable
fun FavoriteActivityDesign(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var savedMovies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    fun refreshData() {
        isLoading = true
        FirestoreHelper.getFavoriteMovies(onSuccess = { movies ->
            Log.d("FavoriteActivity", "Firestore returned: ${movies.size} movies")
            savedMovies = movies
            isLoading = false
            Toast.makeText(
                context,
                "${context.getString(R.string.synced_movies)} ${movies.size}",
                Toast.LENGTH_SHORT
            ).show()
        }, onError = { error ->
            Log.d("FavoriteActivity", "Error fetching: $error")
            isLoading = false
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        })
    }
    
    LaunchedEffect(Unit) { 
        refreshData() 
    }
    
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isLoading)
    
    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = { refreshData() },
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = modifier.fillMaxSize()) {
            if (isLoading) {
                LoadingScreen(
                    isLoading = true,
                    message = null,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (savedMovies.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.cloud_saved_empty))
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(savedMovies) { movie ->
                        MovieFavItem(
                            movie = movie,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    FirestoreHelper.toggleFavrite(
                                        movie = movie,
                                        onResult = { newState ->
                                            refreshData() // Refresh the list after toggle
                                            val message =
                                                if (newState) R.string.added_to_cloud else R.string.removed_from_cloud
                                            Toast.makeText(context, message, Toast.LENGTH_SHORT)
                                                .show()
                                        },
                                        onError = { errorMessage ->
                                            Toast.makeText(
                                                context,
                                                errorMessage,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MovieFavItem(
    movie: Movie,
    modifier: Modifier = Modifier
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
            }
    ) {
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
            },
            fontSize = 11.sp
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun FavouriteActivityPreview() {
    CinemeteorTheme {
        FavoriteActivityDesign()
        MovieFavItem(
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