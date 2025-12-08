# API Implementation, Swipe to Refresh, Loading Screen, and Testing Documentation

## Table of Contents
1. [API Usage and Implementation](#api-usage-and-implementation)
2. [Swipe to Refresh Implementation](#swipe-to-refresh-implementation)
3. [Loading Screen Implementation](#loading-screen-implementation)
4. [Testing](#testing)

---

## API Usage and Implementation

### Overview
The application uses The Movie Database (TMDB) API to fetch movie data. The implementation follows a clean architecture pattern with clear separation of concerns:
- **API Service Layer**: Defines API endpoints
- **Repository Layer**: Handles data fetching and error handling
- **ViewModel Layer**: Manages UI state and business logic

### 1. Retrofit Client Setup

**Location**: `app/src/main/java/com/acms/cinemeteor/api/RetrofitClient.kt`

The Retrofit client is configured as a singleton object that provides a centralized HTTP client for all API calls.

#### Key Features:
- **Base URL**: `https://api.themoviedb.org/3/`
- **HTTP Logging**: Full request/response logging for debugging
- **Timeout Configuration**: 
  - Connect timeout: 30 seconds
  - Read timeout: 30 seconds
  - Write timeout: 30 seconds
- **Gson Converter**: Automatic JSON serialization/deserialization

#### Implementation Details:
```kotlin
object RetrofitClient {
    private const val BASE_URL = "https://api.themoviedb.org/3/"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val tmdbApiService: TMDBApiService = retrofit.create(TMDBApiService::class.java)
}
```

### 2. TMDB API Service Interface

**Location**: `app/src/main/java/com/acms/cinemeteor/api/TMDBApiService.kt`

Defines all API endpoints using Retrofit annotations. All methods are suspend functions for coroutine support.

#### Available Endpoints:

1. **Get Popular Movies**
   - Endpoint: `GET /movie/popular`
   - Parameters: `api_key`, `page` (default: 1), `language` (default: "en-US")
   - Returns: `Response<MovieResponse>`

2. **Get Now Playing Movies**
   - Endpoint: `GET /movie/now_playing`
   - Parameters: `api_key`, `page` (default: 1), `language` (default: "en-US")
   - Returns: `Response<MovieResponse>`

3. **Get Trending Movies**
   - Endpoint: `GET /trending/movie/day`
   - Parameters: `api_key`, `page` (default: 1), `language` (default: "en-US")
   - Returns: `Response<MovieResponse>`

4. **Search Movies**
   - Endpoint: `GET /search/movie`
   - Parameters: `api_key`, `query`, `page` (default: 1), `language` (default: "en-US")
   - Returns: `Response<MovieResponse>`

5. **Get Top Rated Movies**
   - Endpoint: `GET /movie/top_rated`
   - Parameters: `api_key`, `page` (default: 1), `language` (default: "en-US")
   - Returns: `Response<MovieResponse>`

6. **Get Upcoming Movies**
   - Endpoint: `GET /movie/upcoming`
   - Parameters: `api_key`, `page` (default: 1), `language` (default: "en-US")
   - Returns: `Response<MovieResponse>`

7. **Get Movie Details**
   - Endpoint: `GET /movie/{movie_id}`
   - Parameters: `movie_id` (path), `api_key`, `language` (default: "en-US")
   - Returns: `Response<Movie>`

8. **Get Movie Videos**
   - Endpoint: `GET /movie/{movie_id}/videos`
   - Parameters: `movie_id` (path), `api_key`, `language` (default: "en-US")
   - Returns: `Response<MovieVideosResponse>`

9. **Get Similar Movies**
   - Endpoint: `GET /movie/{movie_id}/similar`
   - Parameters: `movie_id` (path), `api_key`, `page` (default: 1), `language` (default: "en-US")
   - Returns: `Response<MovieResponse>`

10. **Get Movie Reviews**
    - Endpoint: `GET /movie/{movie_id}/reviews`
    - Parameters: `movie_id` (path), `api_key`, `page` (default: 1), `language` (default: "en-US")
    - Returns: `Response<MovieReviewsResponse>`

### 3. Movie Repository

**Location**: `app/src/main/java/com/acms/cinemeteor/repository/MovieRepository.kt`

The repository layer acts as a data access abstraction, handling API calls and error management.

#### Architecture Pattern:
- **Single Responsibility**: Each method handles one specific API call
- **Error Handling**: All methods return `Result<T>` for type-safe error handling
- **Logging**: Comprehensive logging for debugging and monitoring
- **Language Support**: All methods accept language parameter for internationalization

#### Key Methods:

##### 3.1. List Operations (Popular, Trending, Now Playing, Top Rated, Upcoming)
All list operations follow the same pattern:
- Accept `apiKey`, `language`, and optional `page` parameters
- Return `Result<List<Movie>>`
- Handle success, API errors, and network exceptions
- Log all operations for debugging

**Example - Get Popular Movies:**
```kotlin
suspend fun getPopularMovies(apiKey: String, language: String = "en-US", page: Int = 1): Result<List<Movie>> {
    return try {
        Log.d("MovieRepository", "Fetching popular movies...")
        val response = apiService.getPopularMovies(apiKey, page, language)
        Log.d("MovieRepository", "Popular movies response: code=${response.code()}, isSuccess=${response.isSuccessful}")
        if (response.isSuccessful) {
            val movies = response.body()?.results ?: emptyList()
            Log.d("MovieRepository", "Popular movies count: ${movies.size}")
            Result.success(movies)
        } else {
            val errorMsg = "API Error: ${response.code()} - ${response.message()}"
            Log.e("MovieRepository", errorMsg)
            Result.failure(Exception(errorMsg))
        }
    } catch (e: Exception) {
        Log.e("MovieRepository", "Exception loading popular movies", e)
        Result.failure(e)
    }
}
```

##### 3.2. Search Movies
Special handling for blank/empty queries:
- Returns empty list immediately if query is blank
- Prevents unnecessary API calls
- Logs search queries for debugging

##### 3.3. Get Movie Details
Two variants:
- **`getMovieDetails`**: Basic movie details fetch
- **`getMovieDetailsWithFallback`**: Enhanced version with English fallback

**Fallback Mechanism:**
The `getMovieDetailsWithFallback` method implements intelligent language fallback:
1. First attempts to fetch in the requested language
2. If title or overview is empty, automatically fetches English version
3. Merges data: uses primary language when available, falls back to English for missing fields
4. If primary language fetch fails, attempts English as fallback

**Implementation:**
```kotlin
suspend fun getMovieDetailsWithFallback(apiKey: String, movieId: Int, language: String = "en-US"): Result<Movie> {
    return try {
        // First try the requested language
        val primaryResult = getMovieDetails(apiKey, movieId, language)
        
        return primaryResult.fold(
            onSuccess = { movie ->
                // Check if title or overview is missing/empty
                val needsFallback = movie.title.isBlank() || movie.overview.isBlank()
                
                if (needsFallback && language != "en-US") {
                    // Fetch English version as fallback
                    val englishResult = getMovieDetails(apiKey, movieId, "en-US")
                    // Merge and return
                } else {
                    Result.success(movie)
                }
            },
            onFailure = { exception ->
                // If primary language fetch fails, try English
                if (language != "en-US") {
                    getMovieDetails(apiKey, movieId, "en-US")
                } else {
                    Result.failure(exception)
                }
            }
        )
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

##### 3.4. Get Movie Videos
- Fetches all videos (trailers, teasers, etc.) for a movie
- Returns `Result<MovieVideosResponse>`
- Used for playing trailers in the app

##### 3.5. Get Similar Movies
- Fetches movies similar to the current movie
- Supports pagination
- Returns `Result<List<Movie>>`

##### 3.6. Get Movie Reviews
- Fetches user reviews for a movie
- Supports pagination
- Returns `Result<MovieReviewsResponse>`

### 4. Error Handling Strategy

#### Result Type Pattern
All repository methods use Kotlin's `Result<T>` type for type-safe error handling:
- **Success**: `Result.success(data)`
- **Failure**: `Result.failure(exception)`

#### Error Types Handled:
1. **API Errors**: HTTP error codes (401, 404, 500, etc.)
   - Logged with response code and message
   - Wrapped in Exception with descriptive message

2. **Network Exceptions**: Connection timeouts, no internet, etc.
   - Caught and logged
   - Returned as `Result.failure(exception)`

3. **Null Responses**: Empty or null response bodies
   - Handled gracefully with empty lists or appropriate error messages

4. **Empty Data**: Missing title/overview fields
   - Handled by fallback mechanism in `getMovieDetailsWithFallback`

### 5. Language Support

#### Language Parameter Flow:
1. **UI Layer**: Gets current language from `LanguageUtils.getLanguageCode(context)`
2. **ViewModel**: Passes language to repository methods
3. **Repository**: Includes language in API calls
4. **API Service**: Sends language as query parameter to TMDB API

#### Supported Languages:
- **English**: `en-US`
- **Arabic**: `ar`
- **Other languages**: Supported via TMDB API

#### Language Fallback:
- If requested language data is incomplete, automatically falls back to English
- Ensures users always see complete movie information

### 6. Logging Strategy

Comprehensive logging at all levels:
- **Repository Level**: Logs all API calls, responses, and errors
- **ViewModel Level**: Logs state changes and data loading
- **UI Level**: Logs user interactions and navigation

**Log Tags Used:**
- `MovieRepository`: Repository operations
- `MovieViewModel`: ViewModel state management
- `FilmActivity`: Movie details screen
- `HomeScreen`: Home screen operations

---

## Swipe to Refresh Implementation

### Overview
Swipe-to-refresh functionality is implemented using the Accompanist SwipeRefresh library, providing a native Android pull-to-refresh experience.

### Library Used
```kotlin
implementation("com.google.accompanist:accompanist-swiperefresh:0.x.x")
```

### 1. HomeScreen Implementation

**Location**: `app/src/main/java/com/acms/cinemeteor/HomeScreen.kt`

#### State Management:
```kotlin
// Track manual refresh state (when user swipes to refresh)
var isManualRefresh by remember { mutableStateOf(false) }

// Pull to refresh state
val isRefreshing = uiState.isLoadingTrending || uiState.isLoadingPopular || uiState.isLoadingUpcoming
val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)

// Update manual refresh state when refreshing completes
LaunchedEffect(isRefreshing) {
    if (!isRefreshing && isManualRefresh) {
        isManualRefresh = false
    }
}
```

#### Implementation:
```kotlin
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
    // Content: LazyColumn with movie lists
}
```

#### Features:
- **Automatic State Management**: Refresh state is automatically managed based on loading states
- **Search Clearing**: Automatically clears search when user manually refreshes
- **Concurrent Loading**: All movie lists (trending, popular, upcoming) reload simultaneously
- **Language Awareness**: Refreshes with current app language

#### Loading Screen Integration:
The loading screen is shown during initial load or manual refresh:
```kotlin
val showLoadingScreen = (isRefreshing && isInitialLoad) || 
        (isManualRefresh && isRefreshing && uiState.searchQuery.isBlank())
```

### 2. FavoriteActivity Implementation

**Location**: `app/src/main/java/com/acms/cinemeteor/FavoriteActivity.kt`

#### Implementation:
```kotlin
val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isLoading)

SwipeRefresh(
    state = swipeRefreshState,
    onRefresh = { refreshData() },
    modifier = Modifier.fillMaxSize()
) {
    // Content: Loading screen or movie grid
}
```

#### Refresh Function:
```kotlin
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
```

#### Features:
- **Firestore Integration**: Refreshes favorite movies from Firebase Firestore
- **User Feedback**: Shows toast message with sync status
- **Error Handling**: Displays error messages to user
- **Loading State**: Shows loading screen during refresh

### 3. FilmActivity Implementation

**Location**: `app/src/main/java/com/acms/cinemeteor/FilmActivity.kt`

#### Implementation:
```kotlin
// Swipe refresh state
val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)

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
    // Content: Movie details, similar movies, reviews
}
```

#### Refresh Function:
```kotlin
suspend fun refreshMovieData(isManualRefresh: Boolean = false) {
    if (apiKey.isNotEmpty() && apiKey != "\"\"") {
        if (isManualRefresh) {
            isRefreshing = true
        }
        val languageCode = LanguageUtils.getLanguageCode(context)

        try {
            // Refresh movie details
            isLoadingMovieDetails = true
            val refreshResult = repository.getMovieDetailsWithFallback(apiKey, movie.id, languageCode)
            // ... handle result

            // Refresh similar movies
            isLoadingSimilarMovies = true
            val similarResult = repository.getSimilarMovies(apiKey, movie.id, languageCode, 1)
            // ... handle result

            // Refresh reviews
            isLoadingReviews = true
            val reviewsResult = repository.getMovieReviews(apiKey, movie.id, languageCode, 1)
            // ... handle result
        } finally {
            if (isManualRefresh) {
                isRefreshing = false
            }
        }
    }
}
```

#### Features:
- **Comprehensive Refresh**: Refreshes movie details, similar movies, and reviews
- **Custom Indicator**: Custom-styled refresh indicator matching app theme
- **Language Support**: Refreshes with current app language
- **Concurrent Loading**: All data types refresh simultaneously
- **Error Handling**: Gracefully handles failures for individual data types

### 4. SwipeRefresh State Management

#### State Flow:
1. **User Action**: User pulls down to refresh
2. **State Update**: `isRefreshing` set to `true`
3. **Data Fetch**: API calls initiated
4. **State Update**: Loading flags updated
5. **Completion**: `isRefreshing` set to `false` when all operations complete

#### Best Practices:
- **Single Source of Truth**: Refresh state derived from loading states
- **User Feedback**: Visual indicator shows refresh progress
- **Error Recovery**: Errors don't prevent refresh completion
- **Language Awareness**: Always refreshes with current language

---

## Loading Screen Implementation

### Overview
The loading screen provides a consistent, user-friendly loading experience across the application.

### 1. LoadingScreen Component

**Location**: `app/src/main/java/com/acms/cinemeteor/ui/components/LoadingScreen.kt`

#### Implementation:
```kotlin
@Composable
fun LoadingScreen(
    isLoading: Boolean,
    message: String? = null,
    modifier: Modifier = Modifier
) {
    if (isLoading) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(16.dp)
                )
                if (message != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}
```

#### Features:
- **Full-Screen Overlay**: Covers entire screen with semi-transparent background
- **Centered Indicator**: Material Design CircularProgressIndicator
- **Optional Message**: Can display custom loading message
- **Theme-Aware**: Uses Material Theme colors
- **Conditional Rendering**: Only renders when `isLoading` is true

### 2. Usage in HomeScreen

**Location**: `app/src/main/java/com/acms/cinemeteor/HomeScreen.kt`

#### Implementation:
```kotlin
// Determine if we should show loading screen
val isInitialLoad = uiState.trendingMovies.isEmpty() && 
                    uiState.popularMovies.isEmpty() &&
                    uiState.upcomingMovies.isEmpty() && 
                    uiState.searchQuery.isBlank() && 
                    uiState.searchResults.isEmpty()
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
        // ... rest of UI
    )
}
```

#### Logic:
- **Initial Load**: Shows loading screen when all lists are empty and not searching
- **Manual Refresh**: Shows loading screen during manual swipe-to-refresh
- **Search**: Does NOT show full loading screen during search (only small indicator)

### 3. Usage in FavoriteActivity

**Location**: `app/src/main/java/com/acms/cinemeteor/FavoriteActivity.kt`

#### Implementation:
```kotlin
Box(modifier = modifier.fillMaxSize()) {
    if (isLoading) {
        LoadingScreen(
            isLoading = true,
            message = null,
            modifier = Modifier.fillMaxSize()
        )
    } else if (savedMovies.isEmpty()) {
        // Empty state
    } else {
        // Movie grid
    }
}
```

#### Features:
- **Simple State**: Shows loading screen when `isLoading` is true
- **Empty State**: Shows empty message when no movies
- **Content**: Shows movie grid when data is loaded

### 4. Usage in FilmActivity

**Location**: `app/src/main/java/com/acms/cinemeteor/FilmActivity.kt`

#### Implementation:
```kotlin
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

    // Background image and content
    // ...
}
```

#### Features:
- **Initial Load Only**: Shows loading screen only during initial load
- **Background Image**: Loading screen overlays background image
- **Progressive Loading**: Similar movies and reviews load separately with their own indicators

### 5. Loading Indicators in Lists

#### Small Indicators:
For individual sections, small `CircularProgressIndicator` components are used:

```kotlin
if (uiState.isLoadingTrending && uiState.trendingMovies.isEmpty()) {
    CircularProgressIndicator(
        modifier = Modifier.size(20.dp),
        strokeWidth = 2.dp
    )
}
```

#### Placeholder Loading:
Skeleton placeholders are shown while loading:

```kotlin
if (uiState.isLoadingTrending) {
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
}
```

### 6. Loading State Management

#### State Flow:
1. **Initial State**: `isLoading = true`
2. **Data Fetch**: API calls initiated
3. **State Update**: `isLoading = false` on completion
4. **UI Update**: Loading screen hidden, content shown

#### Best Practices:
- **Conditional Rendering**: Only show when actually loading
- **User Feedback**: Always provide visual feedback during loading
- **Error Handling**: Hide loading screen even on error
- **Progressive Loading**: Show content as it becomes available

---

## Testing

### Overview
The application includes comprehensive unit tests for the repository layer, ensuring API integration works correctly.

### 1. Test Setup

**Location**: `app/src/androidTest/java/com/acms/cinemeteor/repository/MovieRepositoryTest.kt`

#### Testing Framework:
- **JUnit 4**: Test framework
- **MockK**: Mocking library for Kotlin
- **Kotlin Coroutines Test**: For testing suspend functions
- **AndroidJUnit4**: Android test runner

#### Test Structure:
```kotlin
@RunWith(AndroidJUnit4::class)
class MovieRepositoryTest {
    private lateinit var repository: MovieRepository
    private lateinit var mockApiService: TMDBApiService
    private val testApiKey = "TMDB_API_KEY"
    private val testLanguage = "en-US"
    
    @Before
    fun setup() {
        // Mock Log class
        mockkStatic(Log::class)
        // Create mock API service
        mockApiService = mockk(relaxed = true)
        // Inject mock into repository using reflection
        repository = MovieRepository()
        val apiServiceField = MovieRepository::class.java.getDeclaredField("apiService")
        apiServiceField.isAccessible = true
        apiServiceField.set(repository, mockApiService)
    }
}
```

### 2. Test Categories

#### 2.1. Success Cases
Tests verify that successful API responses are handled correctly:

**Example - Get Popular Movies Success:**
```kotlin
@Test
fun testGetPopularMovies_Success() = runTest {
    // Arrange
    val mockMovies = listOf(
        createMockMovie(1, "Movie 1"),
        createMockMovie(2, "Movie 2")
    )
    val mockResponse = MovieResponse(
        page = 1,
        results = mockMovies,
        totalPages = 10,
        totalResults = 200
    )
    
    coEvery { mockApiService.getPopularMovies(any(), any(), any()) } returns
            Response.success(mockResponse)
    
    // Act
    val result = repository.getPopularMovies(testApiKey, testLanguage)
    
    // Assert
    assertTrue(result.isSuccess)
    val movies = result.getOrNull()
    assertNotNull(movies)
    assertEquals(2, movies!!.size)
    assertEquals("Movie 1", movies[0].title)
    
    coVerify(exactly = 1) { mockApiService.getPopularMovies(testApiKey, 1, testLanguage) }
}
```

#### 2.2. Empty Results Cases
Tests verify handling of empty API responses:

**Example - Get Popular Movies Empty Results:**
```kotlin
@Test
fun testGetPopularMovies_EmptyResults() = runTest {
    // Arrange
    val mockResponse = MovieResponse(
        page = 1,
        results = emptyList(),
        totalPages = 1,
        totalResults = 0
    )
    
    coEvery { mockApiService.getPopularMovies(any(), any(), any()) } returns
            Response.success(mockResponse)
    
    // Act
    val result = repository.getPopularMovies(testApiKey, testLanguage)
    
    // Assert
    assertTrue(result.isSuccess)
    val movies = result.getOrNull()
    assertNotNull(movies)
    assertTrue(movies!!.isEmpty())
}
```

#### 2.3. API Error Cases
Tests verify handling of HTTP error responses:

**Example - Get Popular Movies API Error:**
```kotlin
@Test
fun testGetPopularMovies_ApiError() = runTest {
    // Arrange
    val responseBody = "Unauthorized".toResponseBody("text/plain".toMediaType())
    coEvery { mockApiService.getPopularMovies(any(), any(), any()) } returns
            Response.error(401, responseBody)
    
    // Act
    val result = repository.getPopularMovies(testApiKey, testLanguage)
    
    // Assert
    assertTrue(result.isFailure)
    val exception = result.exceptionOrNull()
    assertNotNull(exception)
    assertTrue(exception!!.message!!.contains("API Error"))
}
```

#### 2.4. Network Exception Cases
Tests verify handling of network failures:

**Example - Get Popular Movies Network Exception:**
```kotlin
@Test
fun testGetPopularMovies_NetworkException() = runTest {
    // Arrange
    val networkException = java.net.UnknownHostException("No internet connection")
    coEvery { mockApiService.getPopularMovies(any(), any(), any()) } throws networkException
    
    // Act
    val result = repository.getPopularMovies(testApiKey, testLanguage)
    
    // Assert
    assertTrue(result.isFailure)
    val exception = result.exceptionOrNull()
    assertNotNull(exception)
}
```

#### 2.5. Special Cases

**Search Movies - Blank Query:**
```kotlin
@Test
fun testSearchMovies_BlankQuery() = runTest {
    // Arrange
    val blankQuery = "   "
    
    // Act
    val result = repository.searchMovies(testApiKey, blankQuery, testLanguage)
    
    // Assert
    assertTrue(result.isSuccess)
    assertTrue(result.getOrNull()!!.isEmpty())
    coVerify(exactly = 0) { mockApiService.searchMovies(any(), any(), any(), any()) }
}
```

**Movie Details - Null Response:**
```kotlin
@Test
fun testGetMovieDetails_NullResponse() = runTest {
    // Arrange
    val movieId = 999
    coEvery { mockApiService.getMovieDetails(any(), any(), any()) } returns
            Response.success(null)
    
    // Act
    val result = repository.getMovieDetails(testApiKey, movieId, testLanguage)
    
    // Assert
    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull()!!.message!!.contains("No movie details found"))
}
```

**Movie Details With Fallback - Empty Title:**
```kotlin
@Test
fun testGetMovieDetailsWithFallback_EmptyTitleFallsBackToEnglish() = runTest {
    // Arrange
    val movieId = 789
    val movieWithEmptyTitle = createMockMovie(movieId, "", "Overview")
    val englishMovie = createMockMovie(movieId, "English Title", "English Overview")
    
    coEvery { mockApiService.getMovieDetails(movieId, testApiKey, "fr-FR") } returns
            Response.success(movieWithEmptyTitle)
    coEvery { mockApiService.getMovieDetails(movieId, testApiKey, "en-US") } returns
            Response.success(englishMovie)
    
    // Act
    val result = repository.getMovieDetailsWithFallback(testApiKey, movieId, "fr-FR")
    
    // Assert
    assertTrue(result.isSuccess)
    val movie = result.getOrNull()
    assertNotNull(movie)
    assertEquals("English Title", movie!!.title) // Should use English fallback
}
```

#### 2.6. Pagination Tests
Tests verify pagination support:

```kotlin
@Test
fun testPagination() = runTest {
    // Arrange
    val page = 2
    val mockResponse = MovieResponse(
        page = page,
        results = listOf(createMockMovie(600, "Page 2 Movie")),
        totalPages = 10,
        totalResults = 200
    )
    
    coEvery { mockApiService.getPopularMovies(any(), any(), any()) } returns
            Response.success(mockResponse)
    
    // Act
    val result = repository.getPopularMovies(testApiKey, testLanguage, page)
    
    // Assert
    assertTrue(result.isSuccess)
    coVerify { mockApiService.getPopularMovies(testApiKey, page, testLanguage) }
}
```

### 3. Test Coverage

#### Covered Endpoints:
- ✅ Get Popular Movies
- ✅ Get Trending Movies
- ✅ Get Now Playing Movies
- ✅ Get Top Rated Movies
- ✅ Get Upcoming Movies
- ✅ Search Movies
- ✅ Get Movie Details
- ✅ Get Movie Details With Fallback
- ✅ Get Movie Videos
- ✅ Get Similar Movies
- ✅ Get Movie Reviews

#### Test Scenarios:
- ✅ Success cases
- ✅ Empty results
- ✅ API errors (401, 404, 500, etc.)
- ✅ Network exceptions
- ✅ Null responses
- ✅ Blank/empty queries
- ✅ Language fallback
- ✅ Pagination

### 4. Mock Helper Functions

#### Create Mock Movie:
```kotlin
private fun createMockMovie(
    id: Int,
    title: String,
    overview: String = "Test overview",
    voteAverage: Double = 7.5
): Movie {
    return Movie(
        id = id,
        title = title,
        overview = overview,
        posterPath = "/poster$id.jpg",
        backdropPath = "/backdrop$id.jpg",
        releaseDate = "2024-01-01",
        voteAverage = voteAverage,
        voteCount = 1000,
        popularity = 50.0,
        originalLanguage = "en",
        originalTitle = title
    )
}
```

### 5. Testing Best Practices

#### 1. Arrange-Act-Assert Pattern
All tests follow the AAA pattern:
- **Arrange**: Set up test data and mocks
- **Act**: Execute the code under test
- **Assert**: Verify the results

#### 2. Isolation
- Each test is independent
- No shared state between tests
- Mocks are reset for each test

#### 3. Comprehensive Coverage
- Test success paths
- Test error paths
- Test edge cases
- Test boundary conditions

#### 4. Verification
- Verify method calls with `coVerify`
- Verify return values
- Verify error messages
- Verify state changes

### 6. Running Tests

#### Command Line:
```bash
./gradlew test
```

#### Android Studio:
1. Right-click on test file
2. Select "Run 'MovieRepositoryTest'"
3. View results in test runner

#### Test Results:
- All tests should pass
- Coverage report available
- Detailed failure messages for debugging

---

## Summary

### API Implementation
- ✅ Clean architecture with separation of concerns
- ✅ Comprehensive error handling
- ✅ Language support with intelligent fallback
- ✅ Extensive logging for debugging
- ✅ Type-safe Result pattern

### Swipe to Refresh
- ✅ Native Android experience
- ✅ State management with loading indicators
- ✅ Language-aware refresh
- ✅ User feedback with toasts
- ✅ Error handling

### Loading Screen
- ✅ Consistent UI component
- ✅ Theme-aware design
- ✅ Conditional rendering
- ✅ Progressive loading support
- ✅ Multiple loading states

### Testing
- ✅ Comprehensive unit tests
- ✅ Mock-based testing
- ✅ All endpoints covered
- ✅ Success and error cases
- ✅ Edge cases handled

---

## Additional Notes

### API Key Configuration
The API key is stored in `BuildConfig.TMDB_API_KEY` and should be configured in `local.properties`:
```
TMDB_API_KEY=your_api_key_here
```

### Dependencies
Key dependencies used:
- `retrofit2`: HTTP client
- `okhttp3`: HTTP client implementation
- `gson`: JSON serialization
- `accompanist-swiperefresh`: Swipe refresh component
- `mockk`: Testing mocks
- `kotlinx-coroutines-test`: Coroutine testing

### Performance Considerations
- Concurrent API calls for better performance
- Timeout configuration prevents hanging requests
- Caching could be added for offline support
- Pagination support for large datasets

### Future Enhancements
- Add caching layer
- Implement offline support
- Add more comprehensive error messages
- Add retry mechanism for failed requests
- Implement request cancellation

