package com.acms.cinemeteor.viewmodel

import android.app.Application
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.acms.cinemeteor.models.Movie
import com.acms.cinemeteor.repository.MovieRepository
import com.acms.cinemeteor.utils.LanguageUtils
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Unit tests for MovieViewModel
 * Tests ViewModel logic with mocked repository
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class MovieViewModelTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private lateinit var application: Application
    private lateinit var context: Context
    private lateinit var mockRepository: MovieRepository
    private val testDispatcher = StandardTestDispatcher()
    private val testApiKey = "test_api_key_12345"
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        application = ApplicationProvider.getApplicationContext()
        context = application
        
        // Mock LanguageUtils
        mockkObject(LanguageUtils)
        every { LanguageUtils.getLanguageCode(any()) } returns "en-US"
        
        // Create mock repository (not relaxed to avoid proxy issues)
        mockRepository = mockk(relaxUnitFun = true)
        
        // Mock both methods that are called in ViewModel's init block
        coEvery { mockRepository.getTrendingMovies(any(), any(), any()) } returns
                Result.success(emptyList())
        coEvery { mockRepository.getPopularMovies(any(), any(), any()) } returns
                Result.success(emptyList())
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }
    
    @Test
    fun testLoadTrendingMovies_Success() = runTest(testDispatcher) {
        // Arrange
        val mockMovies = listOf(
            createMockMovie(1, "Trending Movie 1"),
            createMockMovie(2, "Trending Movie 2")
        )
        
        // Mock both methods (init block calls both)
        coEvery { mockRepository.getTrendingMovies(any(), any(), any()) } returns
                Result.success(mockMovies)
        coEvery { mockRepository.getPopularMovies(any(), any(), any()) } returns
                Result.success(emptyList())
        
        // Create ViewModel with mocked repository
        val viewModel = createViewModelWithMockRepository()
        advanceUntilIdle() // Wait for init block to complete
        
        // Act
        viewModel.loadTrendingMovies()
        advanceUntilIdle()
        
        // Assert
        val uiState = viewModel.uiState.first()
        assertFalse(uiState.isLoadingTrending)
        assertEquals(2, uiState.trendingMovies.size)
        assertEquals("Trending Movie 1", uiState.trendingMovies[0].title)
        assertNull(uiState.error)
    }
    
    @Test
    fun testLoadTrendingMovies_Error() = runTest(testDispatcher) {
        // Arrange
        val errorMessage = "Network error"
        // Mock both methods (init block calls both)
        coEvery { mockRepository.getTrendingMovies(any(), any(), any()) } returns
                Result.failure(Exception(errorMessage))
        coEvery { mockRepository.getPopularMovies(any(), any(), any()) } returns
                Result.success(emptyList())
        
        val viewModel = createViewModelWithMockRepository()
        advanceUntilIdle() // Wait for init block to complete
        
        // Act
        viewModel.loadTrendingMovies()
        advanceUntilIdle()
        
        // Assert
        val uiState = viewModel.uiState.first()
        assertFalse(uiState.isLoadingTrending)
        assertTrue(uiState.trendingMovies.isEmpty())
        assertNotNull(uiState.error)
        assertTrue(uiState.error!!.contains(errorMessage))
    }
    
    @Test
    fun testLoadPopularMovies_Success() = runTest(testDispatcher) {
        // Arrange
        val mockMovies = listOf(
            createMockMovie(3, "Popular Movie 1"),
            createMockMovie(4, "Popular Movie 2")
        )
        
        // Mock both methods (init block calls both)
        coEvery { mockRepository.getPopularMovies(any(), any(), any()) } returns
                Result.success(mockMovies)
        coEvery { mockRepository.getTrendingMovies(any(), any(), any()) } returns
                Result.success(emptyList())
        
        val viewModel = createViewModelWithMockRepository()
        advanceUntilIdle() // Wait for init block to complete
        
        // Act
        viewModel.loadPopularMovies()
        advanceUntilIdle()
        
        // Assert
        val uiState = viewModel.uiState.first()
        assertFalse(uiState.isLoadingPopular)
        assertEquals(2, uiState.popularMovies.size)
        assertEquals("Popular Movie 1", uiState.popularMovies[0].title)
        assertNull(uiState.error)
    }
    
    @Test
    fun testLoadPopularMovies_Error() = runTest(testDispatcher) {
        // Arrange
        val errorMessage = "API Error: 401"
        // Mock both methods (init block calls both)
        coEvery { mockRepository.getPopularMovies(any(), any(), any()) } returns
                Result.failure(Exception(errorMessage))
        coEvery { mockRepository.getTrendingMovies(any(), any(), any()) } returns
                Result.success(emptyList())
        
        val viewModel = createViewModelWithMockRepository()
        advanceUntilIdle() // Wait for init block to complete
        
        // Act
        viewModel.loadPopularMovies()
        advanceUntilIdle()
        
        // Assert
        val uiState = viewModel.uiState.first()
        assertFalse(uiState.isLoadingPopular)
        assertTrue(uiState.popularMovies.isEmpty())
        assertNotNull(uiState.error)
    }
    
    @Test
    fun testSearchMovies_Success() = runTest(testDispatcher) {
        // Arrange
        val query = "Avengers"
        val mockMovies = listOf(
            createMockMovie(5, "Avengers: Endgame"),
            createMockMovie(6, "Avengers: Infinity War")
        )
        
        // Mock all methods (init block calls both trending and popular)
        coEvery { mockRepository.searchMovies(any(), any(), any(), any()) } returns
                Result.success(mockMovies)
        coEvery { mockRepository.getTrendingMovies(any(), any(), any()) } returns
                Result.success(emptyList())
        coEvery { mockRepository.getPopularMovies(any(), any(), any()) } returns
                Result.success(emptyList())
        
        val viewModel = createViewModelWithMockRepository()
        advanceUntilIdle() // Wait for init block to complete
        
        // Act
        viewModel.searchMovies(query)
        advanceUntilIdle()
        
        // Assert
        val uiState = viewModel.uiState.first()
        assertFalse(uiState.isLoadingSearch)
        assertEquals(2, uiState.searchResults.size)
        assertEquals(query, uiState.searchQuery)
        assertTrue(uiState.searchResults[0].title.contains("Avengers"))
        assertNull(uiState.error)
    }
    
    @Test
    fun testSearchMovies_BlankQuery() = runTest(testDispatcher) {
        // Arrange
        val viewModel = createViewModelWithMockRepository()
        
        // Act
        viewModel.searchMovies("   ")
        advanceUntilIdle()
        
        // Assert
        val uiState = viewModel.uiState.first()
        assertFalse(uiState.isLoadingSearch)
        assertTrue(uiState.searchResults.isEmpty())
        assertEquals("   ", uiState.searchQuery)
    }
    
    @Test
    fun testSearchMovies_EmptyQuery() = runTest(testDispatcher) {
        // Arrange
        val viewModel = createViewModelWithMockRepository()
        
        // Act
        viewModel.searchMovies("")
        advanceUntilIdle()
        
        // Assert
        val uiState = viewModel.uiState.first()
        assertFalse(uiState.isLoadingSearch)
        assertTrue(uiState.searchResults.isEmpty())
        assertEquals("", uiState.searchQuery)
    }
    
    @Test
    fun testSearchMovies_Error() = runTest(testDispatcher) {
        // Arrange
        val query = "Test"
        val errorMessage = "Search failed"
        // Mock all methods (init block calls both trending and popular)
        coEvery { mockRepository.searchMovies(any(), any(), any(), any()) } returns
                Result.failure(Exception(errorMessage))
        coEvery { mockRepository.getTrendingMovies(any(), any(), any()) } returns
                Result.success(emptyList())
        coEvery { mockRepository.getPopularMovies(any(), any(), any()) } returns
                Result.success(emptyList())
        
        val viewModel = createViewModelWithMockRepository()
        advanceUntilIdle() // Wait for init block to complete
        
        // Act
        viewModel.searchMovies(query)
        advanceUntilIdle()
        
        // Assert
        val uiState = viewModel.uiState.first()
        assertFalse(uiState.isLoadingSearch)
        assertTrue(uiState.searchResults.isEmpty())
        assertNotNull(uiState.error)
    }
    
    @Test
    fun testClearError() = runTest(testDispatcher) {
        // Arrange - Set up immediate mocks for init block
        coEvery { mockRepository.getTrendingMovies(any(), any(), any()) } returns
                Result.success(emptyList())
        coEvery { mockRepository.getPopularMovies(any(), any(), any()) } returns
                Result.success(emptyList())
        
        val viewModel = createViewModelWithMockRepository()
        advanceUntilIdle() // Wait for init block to complete
        
        // Set up error mock and trigger an error
        coEvery { mockRepository.getTrendingMovies(any(), any(), any()) } returns
                Result.failure(Exception("Test error"))
        viewModel.loadTrendingMovies()
        advanceUntilIdle()
        
        // Verify error is set
        var uiState = viewModel.uiState.first()
        assertNotNull(uiState.error) { "Error should be set before clearing" }
        
        // Act - clearError is synchronous, no need for advanceUntilIdle
        viewModel.clearError()
        
        // Assert
        uiState = viewModel.uiState.first()
        assertNull(uiState.error) { "Error should be cleared after clearError()" }
    }
    
    @Test
    fun testReloadMovies() = runTest(testDispatcher) {
        // Arrange
        val trendingMovies = listOf(createMockMovie(1, "Trending"))
        val popularMovies = listOf(createMockMovie(2, "Popular"))
        
        // Mock all methods
        coEvery { mockRepository.getTrendingMovies(any(), any(), any()) } returns
                Result.success(trendingMovies)
        coEvery { mockRepository.getPopularMovies(any(), any(), any()) } returns
                Result.success(popularMovies)
        coEvery { mockRepository.searchMovies(any(), any(), any(), any()) } returns
                Result.success(emptyList())
        
        val viewModel = createViewModelWithMockRepository()
        advanceUntilIdle() // Wait for init block to complete
        
        // Set search query first
        viewModel.searchMovies("test")
        advanceUntilIdle()
        
        // Act
        viewModel.reloadMovies()
        advanceUntilIdle()
        
        // Assert
        val uiState = viewModel.uiState.first()
        assertEquals("", uiState.searchQuery) // Should be cleared
        assertTrue(uiState.searchResults.isEmpty()) // Should be cleared
        assertEquals(1, uiState.trendingMovies.size)
        assertEquals(1, uiState.popularMovies.size)
    }
    
    @Test
    fun testIsLoadingState() = runTest(testDispatcher) {
        // Arrange - Set up immediate mocks for init block
        coEvery { mockRepository.getTrendingMovies(any(), any(), any()) } returns
                Result.success(emptyList())
        coEvery { mockRepository.getPopularMovies(any(), any(), any()) } returns
                Result.success(emptyList())
        
        val viewModel = createViewModelWithMockRepository()
        advanceUntilIdle() // Wait for init block to complete
        
        // Test initial state (after init completes)
        var uiState = viewModel.uiState.first()
        assertFalse(uiState.isLoading)
        
        // Start loading trending (override with delayed mock)
        coEvery { mockRepository.getTrendingMovies(any(), any(), any()) } coAnswers {
            kotlinx.coroutines.delay(100)
            Result.success(emptyList())
        }
        
        viewModel.loadTrendingMovies()
        
        // Give a small delay to allow state to update
        kotlinx.coroutines.delay(10)
        
        // Check loading state
        uiState = viewModel.uiState.first()
        assertTrue(uiState.isLoadingTrending)
        assertTrue(uiState.isLoading)
        
        advanceUntilIdle()
        
        // Check after loading
        uiState = viewModel.uiState.first()
        assertFalse(uiState.isLoadingTrending)
        assertFalse(uiState.isLoading)
    }
    
    @Test
    fun testMultipleLoadingStates() = runTest(testDispatcher) {
        // Arrange - Set up immediate mocks for init block first
        coEvery { mockRepository.getTrendingMovies(any(), any(), any()) } returns
                Result.success(emptyList())
        coEvery { mockRepository.getPopularMovies(any(), any(), any()) } returns
                Result.success(emptyList())
        
        val viewModel = createViewModelWithMockRepository()
        advanceUntilIdle() // Wait for init block to complete
        
        // Override with delayed responses for the test
        coEvery { mockRepository.getTrendingMovies(any(), any(), any()) } coAnswers {
            kotlinx.coroutines.delay(50)
            Result.success(emptyList())
        }
        coEvery { mockRepository.getPopularMovies(any(), any(), any()) } coAnswers {
            kotlinx.coroutines.delay(50)
            Result.success(emptyList())
        }
        
        // Act - load both
        viewModel.loadTrendingMovies()
        viewModel.loadPopularMovies()
        
        // Give a small delay to allow state to update
        kotlinx.coroutines.delay(10)
        
        // Assert - both should be loading
        var uiState = viewModel.uiState.first()
        assertTrue(uiState.isLoadingTrending)
        assertTrue(uiState.isLoadingPopular)
        assertTrue(uiState.isLoading)
        
        advanceUntilIdle()
        
        // Both should be done
        uiState = viewModel.uiState.first()
        assertFalse(uiState.isLoadingTrending)
        assertFalse(uiState.isLoadingPopular)
        assertFalse(uiState.isLoading)
    }
    
    // Helper function to create ViewModel with mocked repository
    private fun createViewModelWithMockRepository(): MovieViewModel {
        // Create ViewModel
        val viewModel = MovieViewModel(application)
        
        // Use reflection to inject mock repository
        try {
            val repositoryField = MovieViewModel::class.java.getDeclaredField("repository")
            repositoryField.isAccessible = true
            repositoryField.set(viewModel, mockRepository)
        } catch (e: Exception) {
            // If reflection fails, the test will fail naturally
            throw AssertionError("Failed to inject mock repository: ${e.message}", e)
        }
        
        return viewModel
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

