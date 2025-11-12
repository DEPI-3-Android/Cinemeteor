package com.acms.cinemeteor

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.acms.cinemeteor.models.Movie
import com.acms.cinemeteor.utils.ImageUtils
import com.acms.cinemeteor.viewmodel.MovieViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviesHomeScreen(
    viewModel: MovieViewModel = viewModel()
) {
    val background = R.drawable.background
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Debounced search
    var searchText by remember { mutableStateOf("") }
    LaunchedEffect(searchText) {
        delay(500) // Wait 500ms after user stops typing
        viewModel.searchMovies(searchText)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(id = background),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
        )

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    navigationIcon = {
                        IconButton(onClick = {
                            val intent = Intent(context, SettingsActivity::class.java)
                            context.startActivity(intent)
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.settings),
                                contentDescription = stringResource(R.string.settings),
                                tint = Color.White
                            )
                        }
                    },
                    title = {
                        Text(
                            text = stringResource(R.string.app_name),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(onClick = { /* TODO: Notification */ }) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = stringResource(R.string.notifications),
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            // Show initial loading state only when everything is empty and loading
            if (uiState.isLoading && 
                uiState.trendingMovies.isEmpty() && 
                uiState.popularMovies.isEmpty() && 
                uiState.searchResults.isEmpty() &&
                uiState.searchQuery.isBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color.White)
                        if (uiState.error != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = uiState.error ?: "Error",
                                color = Color.Red,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Show error banner if there's an error
                    if (uiState.error != null) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.Red.copy(alpha = 0.8f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = uiState.error ?: "Error",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextButton(onClick = { viewModel.clearError() }) {
                                        Text("Dismiss", color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                    
                    item {
                        OutlinedTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            placeholder = { 
                                Text(
                                    stringResource(R.string.search_movies), 
                                    color = Color.Gray
                                ) 
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color.Black.copy(alpha = 0.3f), 
                                    RoundedCornerShape(12.dp)
                                ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.Transparent,
                                cursorColor = Color.White,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // Show search results if searching
                    if (searchText.isNotBlank()) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Search Results",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                                if (uiState.isLoadingSearch) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        
                        if (uiState.searchResults.isEmpty() && !uiState.isLoadingSearch) {
                            item {
                                Text(
                                    text = "No movies found for '$searchText'",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )
                            }
                        } else {
                            items(uiState.searchResults) { movie ->
                                MovieGridItem(
                                    movie = movie,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    } else {
                        // Trending Movies Section - Always show this section
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.trending_now),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                                if (uiState.isLoadingTrending && uiState.trendingMovies.isEmpty()) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        
                        item {
                            if (uiState.trendingMovies.isNotEmpty()) {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    items(uiState.trendingMovies) { movie ->
                                        MoviePosterItem(movie = movie)
                                    }
                                }
                            } else if (uiState.isLoadingTrending) {
                                // Show placeholder while loading
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    items(5) {
                                        Box(
                                            modifier = Modifier
                                                .width(140.dp)
                                                .height(200.dp)
                                                .background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                        )
                                    }
                                }
                            } else {
                                // Empty state for trending
                                Text(
                                    text = "No trending movies available",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(25.dp))
                        }

                        // Popular Movies Section - Always show this section
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Popular Movies",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                                if (uiState.isLoadingPopular && uiState.popularMovies.isEmpty()) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        
                        if (uiState.popularMovies.isNotEmpty()) {
                            items(uiState.popularMovies) { movie ->
                                MovieGridItem(
                                    movie = movie,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        } else if (uiState.isLoadingPopular) {
                            // Show placeholders while loading
                            items(5) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                        .padding(bottom = 4.dp)
                                )
                            }
                        } else {
                            // Empty state for popular
                            item {
                                Text(
                                    text = "No popular movies available",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(vertical = 16.dp)
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
fun MoviePosterItem(movie: Movie) {
    val posterUrl = ImageUtils.getPosterUrl(movie.posterPath)
    val context = LocalContext.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable {
            val intent = Intent(context, FilmActivity::class.java)
            intent.putExtra("movie", movie)
            context.startActivity(intent)
        }
    ) {
        AsyncImage(
            model = posterUrl ?: R.drawable.background,
            contentDescription = movie.title,
            modifier = Modifier
                .width(140.dp)
                .height(200.dp)
                .background(Color.Gray, RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.background),
            error = painterResource(id = R.drawable.background)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = movie.title,
            color = Color.White,
            fontSize = 14.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun MovieGridItem(
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
            model = posterUrl ?: R.drawable.background,
            contentDescription = movie.title,
            modifier = Modifier
                .height(160.dp)
                .fillMaxWidth()
                .background(Color.Gray, RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.background),
            error = painterResource(id = R.drawable.background)
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = movie.title,
            color = Color.White,
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
                withStyle(style = SpanStyle(color = Color.White)) {
                    append(String.format("%.1f", movie.voteAverage))
                }
            },
            fontSize = 11.sp
        )
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewMoviesHomeScreen() {
    MoviesHomeScreen()
}
