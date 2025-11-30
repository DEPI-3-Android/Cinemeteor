package com.acms.cinemeteor.repository

import android.util.Log
import com.acms.cinemeteor.api.TMDBApiService
import com.acms.cinemeteor.models.Movie
import com.acms.cinemeteor.models.MovieResponse
import com.acms.cinemeteor.models.MovieReviewsResponse
import com.acms.cinemeteor.models.MovieVideosResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response

/**
 * Unit tests for MovieRepository
 * Tests repository logic with mocked API service
 */
@RunWith(AndroidJUnit4::class)
class MovieRepositoryTest {
    
    private lateinit var repository: MovieRepository
    private lateinit var mockApiService: TMDBApiService
    private val testApiKey = "TMDB_API_KEY"
    private val testLanguage = "en-US"
    
    @Before
    fun setup() {
        mockkStatic(Log::class)
        mockApiService = mockk(relaxed = true)
        
        // Use reflection to inject mock API service
        repository = MovieRepository()
        val apiServiceField = MovieRepository::class.java.getDeclaredField("apiService")
        apiServiceField.isAccessible = true
        apiServiceField.set(repository, mockApiService)
    }
    
    @After
    fun tearDown() {
        // Cleanup if needed
    }
    
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
        // MockK may wrap exceptions, so check the cause or message
        val actualException = exception as? java.net.UnknownHostException 
            ?: (exception as? java.lang.reflect.UndeclaredThrowableException)?.cause as? java.net.UnknownHostException
        assertNotNull(actualException)
        assertTrue(actualException!!.message?.contains("No internet connection") == true || 
                   actualException.message?.contains("UnknownHostException") == true)
    }
    
    @Test
    fun testGetTrendingMovies_Success() = runTest {
        // Arrange
        val mockMovies = listOf(createMockMovie(3, "Trending Movie"))
        val mockResponse = MovieResponse(
            page = 1,
            results = mockMovies,
            totalPages = 5,
            totalResults = 100
        )
        
        coEvery { mockApiService.getTrendingMovies(any(), any(), any()) } returns
                Response.success(mockResponse)
        
        // Act
        val result = repository.getTrendingMovies(testApiKey, testLanguage)
        
        // Assert
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
        coVerify { mockApiService.getTrendingMovies(testApiKey, 1, testLanguage) }
    }
    
    @Test
    fun testSearchMovies_Success() = runTest {
        // Arrange
        val query = "Avengers"
        val mockMovies = listOf(createMockMovie(5, "Avengers: Endgame"))
        val mockResponse = MovieResponse(
            page = 1,
            results = mockMovies,
            totalPages = 1,
            totalResults = 1
        )
        
        coEvery { mockApiService.searchMovies(any(), any(), any(), any()) } returns
                Response.success(mockResponse)
        
        // Act
        val result = repository.searchMovies(testApiKey, query, testLanguage)
        
        // Assert
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
        coVerify { mockApiService.searchMovies(testApiKey, query, 1, testLanguage) }
    }
    
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
    
    @Test
    fun testSearchMovies_EmptyQuery() = runTest {
        // Arrange
        val emptyQuery = ""
        
        // Act
        val result = repository.searchMovies(testApiKey, emptyQuery, testLanguage)
        
        // Assert
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }
    
    @Test
    fun testGetMovieDetails_Success() = runTest {
        // Arrange
        val movieId = 123
        val mockMovie = createMockMovie(movieId, "Test Movie", "Test overview")
        
        coEvery { mockApiService.getMovieDetails(any(), any(), any()) } returns
                Response.success(mockMovie)
        
        // Act
        val result = repository.getMovieDetails(testApiKey, movieId, testLanguage)
        
        // Assert
        assertTrue(result.isSuccess)
        val movie = result.getOrNull()
        assertNotNull(movie)
        assertEquals(movieId, movie!!.id)
        assertEquals("Test Movie", movie.title)
        coVerify { mockApiService.getMovieDetails(movieId, testApiKey, testLanguage) }
    }
    
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
    
    @Test
    fun testGetMovieDetailsWithFallback_Success() = runTest {
        // Arrange
        val movieId = 456
        val mockMovie = createMockMovie(movieId, "Movie with Title", "Overview")
        
        coEvery { mockApiService.getMovieDetails(any(), any(), any()) } returns
                Response.success(mockMovie)
        
        // Act
        val result = repository.getMovieDetailsWithFallback(testApiKey, movieId, testLanguage)
        
        // Assert
        assertTrue(result.isSuccess)
        assertEquals(movieId, result.getOrNull()!!.id)
    }
    
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
    
    @Test
    fun testGetMovieVideos_Success() = runTest {
        // Arrange
        val movieId = 101
        val mockVideosResponse = MovieVideosResponse(
            id = movieId,
            results = listOf(
                com.acms.cinemeteor.models.Video(
                    id = "video1",
                    iso31661 = "US",
                    iso6391 = "en",
                    key = "abc123",
                    name = "Trailer",
                    site = "YouTube",
                    type = "Trailer"
                )
            )
        )
        
        coEvery { mockApiService.getMovieVideos(any(), any(), any()) } returns
                Response.success(mockVideosResponse)
        
        // Act
        val result = repository.getMovieVideos(testApiKey, movieId, testLanguage)
        
        // Assert
        assertTrue(result.isSuccess)
        val videos = result.getOrNull()
        assertNotNull(videos)
        assertEquals(1, videos!!.results.size)
        assertEquals("Trailer", videos.results[0].name)
        coVerify { mockApiService.getMovieVideos(movieId, testApiKey, testLanguage) }
    }
    
    @Test
    fun testGetSimilarMovies_Success() = runTest {
        // Arrange
        val movieId = 202
        val mockMovies = listOf(createMockMovie(203, "Similar Movie"))
        val mockResponse = MovieResponse(
            page = 1,
            results = mockMovies,
            totalPages = 1,
            totalResults = 1
        )
        
        coEvery { mockApiService.getSimilarMovies(any(), any(), any(), any()) } returns
                Response.success(mockResponse)
        
        // Act
        val result = repository.getSimilarMovies(testApiKey, movieId, testLanguage)
        
        // Assert
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
        coVerify { mockApiService.getSimilarMovies(movieId, testApiKey, 1, testLanguage) }
    }
    
    @Test
    fun testGetMovieReviews_Success() = runTest {
        // Arrange
        val movieId = 303
        val mockReviewsResponse = MovieReviewsResponse(
            id = movieId,
            page = 1,
            results = listOf(
                com.acms.cinemeteor.models.Review(
                    id = "review1",
                    author = "Test Reviewer",
                    content = "Great movie!",
                    createdAt = "2024-01-01T00:00:00.000Z",
                    authorDetails = null
                )
            ),
            totalPages = 1,
            totalResults = 1
        )
        
        coEvery { mockApiService.getMovieReviews(any(), any(), any(), any()) } returns
                Response.success(mockReviewsResponse)
        
        // Act
        val result = repository.getMovieReviews(testApiKey, movieId, testLanguage)
        
        // Assert
        assertTrue(result.isSuccess)
        val reviews = result.getOrNull()
        assertNotNull(reviews)
        assertEquals(1, reviews!!.results.size)
        assertEquals("Test Reviewer", reviews.results[0].author)
        coVerify { mockApiService.getMovieReviews(movieId, testApiKey, 1, testLanguage) }
    }
    
    @Test
    fun testGetNowPlayingMovies_Success() = runTest {
        // Arrange
        val mockMovies = listOf(createMockMovie(404, "Now Playing Movie"))
        val mockResponse = MovieResponse(
            page = 1,
            results = mockMovies,
            totalPages = 5,
            totalResults = 50
        )
        
        coEvery { mockApiService.getNowPlayingMovies(any(), any(), any()) } returns
                Response.success(mockResponse)
        
        // Act
        val result = repository.getNowPlayingMovies(testApiKey, testLanguage)
        
        // Assert
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
    }
    
    @Test
    fun testGetTopRatedMovies_Success() = runTest {
        // Arrange
        val mockMovies = listOf(createMockMovie(505, "Top Rated Movie", voteAverage = 9.5))
        val mockResponse = MovieResponse(
            page = 1,
            results = mockMovies,
            totalPages = 10,
            totalResults = 200
        )
        
        coEvery { mockApiService.getTopRatedMovies(any(), any(), any()) } returns
                Response.success(mockResponse)
        
        // Act
        val result = repository.getTopRatedMovies(testApiKey, testLanguage)
        
        // Assert
        assertTrue(result.isSuccess)
        assertEquals(9.5, result.getOrNull()!![0].voteAverage, 0.01)
    }
    
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
    
    // Helper function to create mock movie
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
}

