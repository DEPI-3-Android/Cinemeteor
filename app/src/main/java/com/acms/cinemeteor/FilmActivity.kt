package com.acms.cinemeteor

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Layout
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.acms.cinemeteor.BuildConfig
import com.acms.cinemeteor.models.Movie
import com.acms.cinemeteor.models.Review
import com.acms.cinemeteor.repository.MovieRepository
import com.acms.cinemeteor.ui.components.LoadingScreen
import com.acms.cinemeteor.ui.theme.CinemeteorTheme
import com.acms.cinemeteor.utils.ImageUtils
import com.acms.cinemeteor.utils.LanguageUtils
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class FilmActivity : AppCompatActivity() {
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
                                Text(
                                    "No movie data found",
                                    color = MaterialTheme.colorScheme.onBackground
                                )
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
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val repository = remember { MovieRepository() }
    val apiKey = BuildConfig.TMDB_API_KEY.trim()

    // State for the current movie (can be refreshed with new language)
    var currentMovie by remember { mutableStateOf(movie) }
    val posterUrl = currentMovie.posterPath?.let { ImageUtils.getPosterUrl(it) }
    var expanded by remember { mutableStateOf(false) }
    var isSaved by remember {
        mutableStateOf(LocalSavedMovies.isMovieSaved(context, currentMovie.id))
    }
    var isLoadingMovieDetails by remember { mutableStateOf(false) }
    var similarMovies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var isLoadingSimilarMovies by remember { mutableStateOf(false) }
    var isLoadingReviews by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var isInitialLoad by remember { mutableStateOf(true) }

    // Swipe refresh state
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)

    // Function to refresh all movie data
    suspend fun refreshMovieData(isManualRefresh: Boolean = false) {
        if (apiKey.isNotEmpty() && apiKey != "\"\"") {
            if (isManualRefresh) {
                isRefreshing = true
            }
            val languageCode = LanguageUtils.getLanguageCode(context)

            try {
                // Refresh movie details
                isLoadingMovieDetails = true
                val refreshResult =
                    repository.getMovieDetailsWithFallback(apiKey, movie.id, languageCode)
                refreshResult.onSuccess { updatedMovie ->
                    currentMovie = updatedMovie
                    isSaved = LocalSavedMovies.isMovieSaved(context, updatedMovie.id)
                    isLoadingMovieDetails = false
                    Log.d("FilmActivity", "Movie details refreshed: title='${updatedMovie.title}'")
                }.onFailure {
                    isLoadingMovieDetails = false
                }

                // Refresh similar movies
                isLoadingSimilarMovies = true
                val similarResult = repository.getSimilarMovies(apiKey, movie.id, languageCode, 1)
                similarResult.onSuccess { movies ->
                    similarMovies = movies.take(10)
                    isLoadingSimilarMovies = false
                    Log.d("FilmActivity", "Loaded ${similarMovies.size} similar movies")
                }.onFailure {
                    isLoadingSimilarMovies = false
                }

                // Refresh reviews
                isLoadingReviews = true
                val reviewsResult = repository.getMovieReviews(apiKey, movie.id, languageCode, 1)
                reviewsResult.onSuccess { reviewsResponse ->
                    reviews = reviewsResponse.results.take(5)
                    isLoadingReviews = false
                    Log.d("FilmActivity", "Loaded ${reviews.size} reviews")
                }.onFailure {
                    isLoadingReviews = false
                }
            } catch (e: Exception) {
                Log.e("FilmActivity", "Exception refreshing movie data", e)
                isLoadingMovieDetails = false
                isLoadingSimilarMovies = false
                isLoadingReviews = false
            } finally {
                if (isManualRefresh) {
                    isRefreshing = false
                }
                if (isInitialLoad) {
                    isInitialLoad = false
                }
            }
        }
    }

    // Refresh movie details with current language when screen loads (with English fallback)
    LaunchedEffect(Unit) {
        val languageCode = LanguageUtils.getLanguageCode(context)
        Log.d("FilmActivity", "Loading movie ${movie.id} with language: $languageCode")
        refreshMovieData(isManualRefresh = false)
    }




    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Loading screen overlay while fetching movie details (initial load only)
        LoadingScreen(
            isLoading = isLoadingMovieDetails && isInitialLoad,
            message = null,
            modifier = Modifier.fillMaxSize()
        )

        AsyncImage(
            model = posterUrl ?: R.drawable.background_screen,
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.3f
        )


        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
        )


        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = {
                lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    refreshMovieData(isManualRefresh = true)
                }
            },
            indicator = { state, trigger ->
                SwipeRefreshIndicator(
                    state = state,
                    refreshTriggerDistance = trigger,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            },
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Row {
                    AsyncImage(
                        model = posterUrl ?: R.drawable.background_screen,
                        contentDescription = currentMovie.title,
                        modifier = Modifier
                            .width(140.dp)
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.background_screen),
                        error = painterResource(id = R.drawable.background_screen)
                    )

                    Spacer(
                        modifier = Modifier
                            .height(12.dp)
                            .width(12.dp)
                    )


                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {

                        Text(
                            text = currentMovie.title.ifEmpty { "No Title" },
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            ),
                            modifier = Modifier
                                .padding(8.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "⭐ ${currentMovie.voteAverage}",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Row {
                            IconButton(onClick = {
                                LocalSavedMovies.toggleMovie(context, currentMovie)

                                isSaved = LocalSavedMovies.isMovieSaved(context, currentMovie.id)
                            }) {
                                Icon(
                                    painter = painterResource(
                                        id = if (isSaved) R.drawable.ic_saved else R.drawable.ic_save
                                    ),
                                    contentDescription = "Save",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            IconButton(onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Check out this movie: ${currentMovie.title}\n\n${currentMovie.overview}"
                                    )
                                }
                                context.startActivity(
                                    Intent.createChooser(shareIntent, "Share via")
                                )
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_share),
                                    contentDescription = "Share",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))


                Button(
                    onClick = {
                        // Fetch and play trailer
                        if (apiKey.isNotEmpty() && apiKey != "\"\"") {
                            lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                                try {
                                    val languageCode = LanguageUtils.getLanguageCode(context)
                                    val rawLanguageCode =
                                        LanguageUtils.getLanguageRaw(context) // "ar" or "en"
                                    Log.d(
                                        "FilmActivity",
                                        "Fetching trailers for movie ${currentMovie.id} with language: $rawLanguageCode (API: $languageCode)"
                                    )

                                    val videosResult = repository.getMovieVideos(
                                        apiKey,
                                        currentMovie.id,
                                        languageCode
                                    )
                                    videosResult.onSuccess { videosResponse ->
                                        // Filter all YouTube trailers
                                        val allTrailers = videosResponse.results.filter { video ->
                                            video.type == "Trailer" && video.site == "YouTube"
                                        }

                                        Log.d(
                                            "FilmActivity",
                                            "Found ${allTrailers.size} YouTube trailers"
                                        )

                                        // First, try to find a trailer matching the selected language
                                        var trailer = allTrailers.firstOrNull { video ->
                                            val videoLang = video.iso6391?.lowercase()
                                                ?.take(2) // Take first 2 chars (ar, en)
                                            videoLang == rawLanguageCode.lowercase()
                                        }

                                        // If found, log it
                                        if (trailer != null) {
                                            Log.d(
                                                "FilmActivity",
                                                "Found trailer in $rawLanguageCode: ${trailer.name} (lang: ${trailer.iso6391})"
                                            )
                                        } else {
                                            // Fallback: Try to find any trailer in the API language
                                            trailer = allTrailers.firstOrNull { video ->
                                                val apiLangCode = languageCode.take(2)
                                                    .lowercase() // "en" from "en-US" or "ar" from "ar"
                                                video.iso6391?.lowercase()?.take(2) == apiLangCode
                                            }

                                            if (trailer != null) {
                                                Log.d(
                                                    "FilmActivity",
                                                    "Using trailer in API language: ${trailer.name} (lang: ${trailer.iso6391})"
                                                )
                                            } else {
                                                // Final fallback: use any trailer
                                                trailer = allTrailers.firstOrNull()
                                                if (trailer != null) {
                                                    Log.d(
                                                        "FilmActivity",
                                                        "No $rawLanguageCode trailer found, using any trailer: ${trailer.name} (lang: ${trailer.iso6391})"
                                                    )
                                                }
                                            }
                                        }

                                        if (trailer != null) {
                                            val youtubeUrl =
                                                "https://www.youtube.com/watch?v=${trailer.key}"
                                            // Switch to main thread to start activity
                                            launch(Dispatchers.Main) {
                                                val intent = Intent(
                                                    Intent.ACTION_VIEW,
                                                    Uri.parse(youtubeUrl)
                                                )
                                                intent.setPackage("com.google.android.youtube")
                                                // Try YouTube app first, fallback to browser
                                                if (intent.resolveActivity(context.packageManager) != null) {
                                                    context.startActivity(intent)
                                                } else {
                                                    // Fallback to web browser
                                                    val webIntent = Intent(
                                                        Intent.ACTION_VIEW,
                                                        Uri.parse(youtubeUrl)
                                                    )
                                                    context.startActivity(webIntent)
                                                }
                                            }
                                        } else {
                                            launch(Dispatchers.Main) {
                                                Toast.makeText(
                                                    context,
                                                    "No trailer available for this movie",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }.onFailure { exception ->
                                        Log.e("FilmActivity", "Failed to fetch trailer", exception)
                                        launch(Dispatchers.Main) {
                                            Toast.makeText(
                                                context,
                                                "Failed to load trailer: ${exception.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e("FilmActivity", "Exception fetching trailer", e)
                                    launch(Dispatchers.Main) {
                                        Toast.makeText(
                                            context,
                                            "Error loading trailer",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "API key not configured",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                        .height(50.dp)
                ) {
                    Text(
                        text = stringResource(R.string.play),
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))


                val overview = currentMovie.overview.ifEmpty { "No description available." }
                val shortText = if (overview.length > 120 && !expanded) {
                    overview.take(120) + "..."
                } else overview

                Text(
                    text = shortText,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Justify,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                if (overview.length > 120) {
                    TextButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            if (expanded) stringResource(R.string.readlees) else stringResource(R.string.readmore),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Similar Movies Section
                if (similarMovies.isNotEmpty() || isLoadingSimilarMovies) {
                    Text(
                        text = stringResource(R.string.similar),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 8.dp)
                    )

                    if (isLoadingSimilarMovies) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "...",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    } else if (similarMovies.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            items(similarMovies) { similarMovie ->
                                SimilarMovieItem(movie = similarMovie, context = context)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Reviews Section
                if (reviews.isNotEmpty() || isLoadingReviews) {
                    Text(
                        text = "Reviews",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 8.dp)
                    )

                    if (isLoadingReviews) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "...",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    } else if (reviews.isNotEmpty()) {
                        reviews.forEach { review ->
                            ReviewItem(review = review)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun SimilarMovieItem(movie: Movie, context: Context) {
    val posterUrl = movie.posterPath?.let { ImageUtils.getPosterUrl(it) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(120.dp)
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
                .width(150.dp)
                .height(200.dp)
                .clip(shape = RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.background_screen),
            error = painterResource(id = R.drawable.background_screen)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = movie.title,
            fontSize = 12.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.width(120.dp)
        )
    }
}

@Composable
fun ReviewItem(review: Review) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = review.author,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                review.authorDetails?.rating?.let { rating ->
                    Text(
                        text = "⭐ ${String.format("%.1f", rating)}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (review.createdAt != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = review.createdAt.take(10), // Show just the date part
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = review.content,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                lineHeight = 20.sp
            )
        }
    }
}
