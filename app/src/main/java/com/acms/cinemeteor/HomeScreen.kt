package com.acms.cinemeteor

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
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
import android.util.Log
import com.acms.cinemeteor.models.Movie
import com.acms.cinemeteor.ui.components.LoadingScreen
import com.acms.cinemeteor.utils.ImageUtils
import com.acms.cinemeteor.utils.LanguageUtils
import com.acms.cinemeteor.viewmodel.MovieViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviesHomeScreen(
    viewModel: MovieViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    
    // Track current language to detect changes and reload movies
    val currentLanguage = LanguageUtils.getLanguageRaw(context)
    
    // Reload movies when language changes (LaunchedEffect key changes when language changes)
    LaunchedEffect(currentLanguage) {
        Log.d("MoviesHomeScreen", "Language detected: $currentLanguage, reloading movies...")
        viewModel.reloadMovies()
    }

    // Debounced search
    var searchText by remember { mutableStateOf("") }
    LaunchedEffect(searchText) {
        delay(500) // Wait 500ms after user stops typing
        viewModel.searchMovies(searchText)
    }
    
    // Track manual refresh state (when user swipes to refresh)
    var isManualRefresh by remember { mutableStateOf(false) }
    
    // Pull to refresh state
    val isRefreshing = uiState.isLoadingTrending || uiState.isLoadingPopular
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)
    
    // Update manual refresh state when refreshing completes
    LaunchedEffect(isRefreshing) {
        if (!isRefreshing && isManualRefresh) {
            isManualRefresh = false
        }
    }

    // Determine if we should show loading screen
    // Show loading if:
    // 1. Initial load: loading and both lists are empty AND not searching
    // 2. Manual refresh: user swiped to refresh (isManualRefresh is true)
    // Don't show loading during search (only show small indicator in search section)
    val isInitialLoad = uiState.trendingMovies.isEmpty() && uiState.popularMovies.isEmpty() &&
            uiState.searchQuery.isBlank() && uiState.searchResults.isEmpty()
    val showLoadingScreen = (isRefreshing && isInitialLoad) || 
            (isManualRefresh && isRefreshing && uiState.searchQuery.isBlank())

    Box(modifier = Modifier.fillMaxSize()) {
        // Loading screen overlay
        LoadingScreen(
            isLoading = showLoadingScreen,
            message = null,
            modifier = Modifier.fillMaxSize()
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.app_name),
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
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = {
                    // Clear search when refreshing manually
                    searchText = ""
                    isManualRefresh = true
                    // Reload all movies (trending, popular, and clear search)
                    viewModel.reloadMovies()
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxSize()
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

                                        fontSize = 12.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextButton(onClick = { viewModel.clearError() }) {
                                        Text("Dismiss")
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
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.tertiary,
                                cursorColor = MaterialTheme.colorScheme.primary,
                                focusedTextColor = MaterialTheme.colorScheme.secondary,
                                unfocusedTextColor = MaterialTheme.colorScheme.secondary,
                                disabledTextColor = MaterialTheme.colorScheme.tertiary,
                                disabledPlaceholderColor = MaterialTheme.colorScheme.tertiary,
                                focusedPlaceholderColor = MaterialTheme.colorScheme.tertiary,
                                unfocusedPlaceholderColor = MaterialTheme.colorScheme.tertiary
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

                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                                if (uiState.isLoadingTrending && uiState.trendingMovies.isEmpty()) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),

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
                                                .clip(RoundedCornerShape(16))
                                        )
                                    }
                                }
                            } else {
                                // Empty state for trending
                                Text(
                                    text = stringResource(R.string.no_trending),
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
                                    text = stringResource(R.string.popular),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                                if (uiState.isLoadingPopular && uiState.popularMovies.isEmpty()) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),

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
                                        .clip(RoundedCornerShape(16.dp))
                                        .padding(bottom = 4.dp)
                                )
                            }
                        } else {
                            // Empty state for popular
                            item {
                                Text(
                                    text = stringResource(R.string.no_popular),
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
            model = posterUrl ?: R.drawable.background_screen,
            contentDescription = movie.title,
            modifier = Modifier
                .width(140.dp)
                .height(200.dp)
                .clip(shape = RoundedCornerShape(16)),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.background_screen),
            error = painterResource(id = R.drawable.background_screen),

            )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = movie.title,

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
            model = posterUrl ?: R.drawable.background_screen,
            contentDescription = movie.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
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


@Preview(showBackground = true)
@Composable
fun PreviewMoviesHomeScreen() {
    MoviesHomeScreen()
}
