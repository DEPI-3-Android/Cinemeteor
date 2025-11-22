package com.acms.cinemeteor

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.acms.cinemeteor.BuildConfig
import com.acms.cinemeteor.models.Movie
import com.acms.cinemeteor.repository.MovieRepository
import com.acms.cinemeteor.ui.components.LoadingScreen
import com.acms.cinemeteor.ui.theme.CinemeteorTheme
import com.acms.cinemeteor.utils.ImageUtils
import com.acms.cinemeteor.utils.LanguageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


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
    
    // Refresh movie details with current language when screen loads (with English fallback)
    LaunchedEffect(Unit) {
        if (apiKey.isNotEmpty() && apiKey != "\"\"") {
            isLoadingMovieDetails = true
            val languageCode = LanguageUtils.getLanguageCode(context)
            Log.d("FilmActivity", "Refreshing movie ${movie.id} with language: $languageCode (with English fallback)")
            try {
                val refreshResult = repository.getMovieDetailsWithFallback(apiKey, movie.id, languageCode)
                refreshResult.onSuccess { updatedMovie ->
                    Log.d("FilmActivity", "Movie details refreshed: title='${updatedMovie.title}', overview length=${updatedMovie.overview.length}")
                    currentMovie = updatedMovie
                    // Update saved status
                    isSaved = LocalSavedMovies.isMovieSaved(context, updatedMovie.id)
                    isLoadingMovieDetails = false
                }.onFailure { exception ->
                    Log.e("FilmActivity", "Failed to refresh movie details: ${exception.message}")
                    // Keep original movie if refresh fails
                    isLoadingMovieDetails = false
                }
            } catch (e: Exception) {
                Log.e("FilmActivity", "Exception refreshing movie details", e)
                isLoadingMovieDetails = false
            }
        }
    }




    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Loading screen overlay while fetching movie details
        LoadingScreen(
            isLoading = isLoadingMovieDetails,
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


        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            AsyncImage(
                model = posterUrl ?: R.drawable.background_screen,
                contentDescription = currentMovie.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .align(Alignment.CenterHorizontally),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.background_screen),
                error = painterResource(id = R.drawable.background_screen)
            )

            Spacer(modifier = Modifier.height(12.dp))


            Text(
                text = currentMovie.title.ifEmpty { "No Title" },
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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

            Spacer(modifier = Modifier.height(20.dp))


            Button(
                onClick = {
                    // Fetch and play trailer
                    if (apiKey.isNotEmpty() && apiKey != "\"\"") {
                        lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                            try {
                                val languageCode = LanguageUtils.getLanguageCode(context)
                                val rawLanguageCode = LanguageUtils.getLanguageRaw(context) // "ar" or "en"
                                Log.d("FilmActivity", "Fetching trailers for movie ${currentMovie.id} with language: $rawLanguageCode (API: $languageCode)")
                                
                                val videosResult = repository.getMovieVideos(apiKey, currentMovie.id, languageCode)
                                videosResult.onSuccess { videosResponse ->
                                    // Filter all YouTube trailers
                                    val allTrailers = videosResponse.results.filter { video ->
                                        video.type == "Trailer" && video.site == "YouTube"
                                    }
                                    
                                    Log.d("FilmActivity", "Found ${allTrailers.size} YouTube trailers")
                                    
                                    // First, try to find a trailer matching the selected language
                                    var trailer = allTrailers.firstOrNull { video ->
                                        val videoLang = video.iso6391?.lowercase()?.take(2) // Take first 2 chars (ar, en)
                                        videoLang == rawLanguageCode.lowercase()
                                    }
                                    
                                    // If found, log it
                                    if (trailer != null) {
                                        Log.d("FilmActivity", "Found trailer in $rawLanguageCode: ${trailer.name} (lang: ${trailer.iso6391})")
                                    } else {
                                        // Fallback: Try to find any trailer in the API language
                                        trailer = allTrailers.firstOrNull { video ->
                                            val apiLangCode = languageCode.take(2).lowercase() // "en" from "en-US" or "ar" from "ar"
                                            video.iso6391?.lowercase()?.take(2) == apiLangCode
                                        }
                                        
                                        if (trailer != null) {
                                            Log.d("FilmActivity", "Using trailer in API language: ${trailer.name} (lang: ${trailer.iso6391})")
                                        } else {
                                            // Final fallback: use any trailer
                                            trailer = allTrailers.firstOrNull()
                                            if (trailer != null) {
                                                Log.d("FilmActivity", "No $rawLanguageCode trailer found, using any trailer: ${trailer.name} (lang: ${trailer.iso6391})")
                                            }
                                        }
                                    }
                                    
                                    if (trailer != null) {
                                        val youtubeUrl = "https://www.youtube.com/watch?v=${trailer.key}"
                                        // Switch to main thread to start activity
                                        launch(Dispatchers.Main) {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(youtubeUrl))
                                            intent.setPackage("com.google.android.youtube")
                                            // Try YouTube app first, fallback to browser
                                            if (intent.resolveActivity(context.packageManager) != null) {
                                                context.startActivity(intent)
                                            } else {
                                                // Fallback to web browser
                                                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(youtubeUrl))
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
                    "Play",
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
                        if (expanded) "Read less" else "Read more",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
