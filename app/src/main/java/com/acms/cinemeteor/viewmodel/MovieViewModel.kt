package com.acms.cinemeteor.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.acms.cinemeteor.BuildConfig
import com.acms.cinemeteor.models.Movie
import com.acms.cinemeteor.repository.MovieRepository
import com.acms.cinemeteor.utils.LanguageUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MovieUiState(
    val trendingMovies: List<Movie> = emptyList(),
    val popularMovies: List<Movie> = emptyList(),
    val upcomingMovies: List<Movie> = emptyList(),
    val searchResults: List<Movie> = emptyList(),
    val isLoadingTrending: Boolean = false,
    val isLoadingPopular: Boolean = false,
    val isLoadingUpcoming: Boolean = false,
    val isLoadingSearch: Boolean = false,
    val error: String? = null,
    val searchQuery: String = ""
) {
    val isLoading: Boolean
        get() = isLoadingTrending || isLoadingPopular || isLoadingUpcoming || isLoadingSearch
}

class MovieViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MovieRepository()
    private val apiKey = BuildConfig.TMDB_API_KEY.trim()
    
    private val _uiState = MutableStateFlow(MovieUiState())
    val uiState: StateFlow<MovieUiState> = _uiState.asStateFlow()
    
    private fun getLanguage(): String {
        return LanguageUtils.getLanguageCode(getApplication())
    }
    
    init {
        Log.d("MovieViewModel", "Initializing ViewModel")
        Log.d("MovieViewModel", "API Key loaded: ${if (apiKey.isNotEmpty()) "Yes (length: ${apiKey.length})" else "No"}")
        if (apiKey.isNotEmpty() && apiKey != "\"\"" && apiKey.isNotBlank()) {
            Log.d("MovieViewModel", "Loading trending, popular, and upcoming movies...")
            // Load all concurrently
            viewModelScope.launch {
                loadTrendingMovies()
            }
            viewModelScope.launch {
                loadPopularMovies()
            }
            viewModelScope.launch {
                loadUpcomingMovies()
            }
        } else {
            Log.e("MovieViewModel", "API key is empty or invalid! Key: '$apiKey'")
            _uiState.value = _uiState.value.copy(
                error = "TMDB API key not configured. Please add TMDB_API_KEY=your_key to local.properties file, sync Gradle, and rebuild the project."
            )
        }
    }
    
    fun loadTrendingMovies() {
        if (apiKey.isEmpty() || apiKey == "\"\"") {
            Log.e("MovieViewModel", "Cannot load trending movies: API key is empty")
            return
        }
        
        val language = getLanguage()
        viewModelScope.launch {
            try {
                Log.d("MovieViewModel", "Loading trending movies with language: $language...")
                _uiState.value = _uiState.value.copy(isLoadingTrending = true, error = null)
                repository.getTrendingMovies(apiKey, language)
                    .onSuccess { movies ->
                        Log.d("MovieViewModel", "Trending movies loaded successfully: ${movies.size} movies")
                        _uiState.value = _uiState.value.copy(
                            trendingMovies = movies,
                            isLoadingTrending = false
                        )
                    }
                    .onFailure { exception ->
                        Log.e("MovieViewModel", "Failed to load trending movies", exception)
                        _uiState.value = _uiState.value.copy(
                            isLoadingTrending = false,
                            error = exception.message ?: "Failed to load trending movies"
                        )
                    }
            } catch (e: Exception) {
                Log.e("MovieViewModel", "Exception in loadTrendingMovies", e)
                _uiState.value = _uiState.value.copy(
                    isLoadingTrending = false,
                    error = "Error: ${e.message}"
                )
            }
        }
    }
    
    fun loadPopularMovies() {
        if (apiKey.isEmpty() || apiKey == "\"\"") {
            Log.e("MovieViewModel", "Cannot load popular movies: API key is empty")
            return
        }
        
        val language = getLanguage()
        viewModelScope.launch {
            try {
                Log.d("MovieViewModel", "Loading popular movies with language: $language...")
                _uiState.value = _uiState.value.copy(isLoadingPopular = true, error = null)
                repository.getPopularMovies(apiKey, language)
                    .onSuccess { movies ->
                        Log.d("MovieViewModel", "Popular movies loaded successfully: ${movies.size} movies")
                        _uiState.value = _uiState.value.copy(
                            popularMovies = movies,
                            isLoadingPopular = false
                        )
                    }
                    .onFailure { exception ->
                        Log.e("MovieViewModel", "Failed to load popular movies", exception)
                        _uiState.value = _uiState.value.copy(
                            isLoadingPopular = false,
                            error = exception.message ?: "Failed to load popular movies"
                        )
                    }
            } catch (e: Exception) {
                Log.e("MovieViewModel", "Exception in loadPopularMovies", e)
                _uiState.value = _uiState.value.copy(
                    isLoadingPopular = false,
                    error = "Error: ${e.message}"
                )
            }
        }
    }
    
    fun loadUpcomingMovies() {
        if (apiKey.isEmpty() || apiKey == "\"\"") {
            Log.e("MovieViewModel", "Cannot load upcoming movies: API key is empty")
            return
        }
        
        val language = getLanguage()
        viewModelScope.launch {
            try {
                Log.d("MovieViewModel", "Loading upcoming movies with language: $language...")
                _uiState.value = _uiState.value.copy(isLoadingUpcoming = true, error = null)
                repository.getUpcomingMovies(apiKey, language)
                    .onSuccess { movies ->
                        Log.d("MovieViewModel", "Upcoming movies loaded successfully: ${movies.size} movies")
                        _uiState.value = _uiState.value.copy(
                            upcomingMovies = movies,
                            isLoadingUpcoming = false
                        )
                    }
                    .onFailure { exception ->
                        Log.e("MovieViewModel", "Failed to load upcoming movies", exception)
                        _uiState.value = _uiState.value.copy(
                            isLoadingUpcoming = false,
                            error = exception.message ?: "Failed to load upcoming movies"
                        )
                    }
            } catch (e: Exception) {
                Log.e("MovieViewModel", "Exception in loadUpcomingMovies", e)
                _uiState.value = _uiState.value.copy(
                    isLoadingUpcoming = false,
                    error = "Error: ${e.message}"
                )
            }
        }
    }
    
    fun searchMovies(query: String) {
        if (apiKey.isEmpty() || apiKey == "\"\"") {
            Log.e("MovieViewModel", "Cannot search movies: API key is empty")
            return
        }
        
        _uiState.value = _uiState.value.copy(searchQuery = query)
        
        if (query.isBlank()) {
            Log.d("MovieViewModel", "Search query is blank, clearing results")
            _uiState.value = _uiState.value.copy(searchResults = emptyList(), isLoadingSearch = false)
            return
        }
        
        val language = getLanguage()
        viewModelScope.launch {
            try {
                Log.d("MovieViewModel", "Searching movies with query: '$query' and language: $language")
                _uiState.value = _uiState.value.copy(isLoadingSearch = true, error = null)
                repository.searchMovies(apiKey, query, language)
                    .onSuccess { movies ->
                        Log.d("MovieViewModel", "Search completed: ${movies.size} movies found")
                        _uiState.value = _uiState.value.copy(
                            searchResults = movies,
                            isLoadingSearch = false
                        )
                    }
                    .onFailure { exception ->
                        Log.e("MovieViewModel", "Failed to search movies", exception)
                        _uiState.value = _uiState.value.copy(
                            isLoadingSearch = false,
                            error = exception.message ?: "Failed to search movies"
                        )
                    }
            } catch (e: Exception) {
                Log.e("MovieViewModel", "Exception in searchMovies", e)
                _uiState.value = _uiState.value.copy(
                    isLoadingSearch = false,
                    error = "Search error: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * Reloads all movies (trending, popular, and upcoming) with current language
     * Clears search query and results to ensure fresh data
     */
    fun reloadMovies() {
        Log.d("MovieViewModel", "Reloading all movies with language: ${getLanguage()}")
        // Clear search query and results first
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            searchResults = emptyList(),
            error = null // Clear any previous errors
        )
        // Reload trending, popular, and upcoming movies with current language
        loadTrendingMovies()
        loadPopularMovies()
        loadUpcomingMovies()
    }
}

