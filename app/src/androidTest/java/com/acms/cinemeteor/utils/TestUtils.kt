package com.acms.cinemeteor.utils

import com.acms.cinemeteor.models.Movie

/**
 * Test utilities for creating mock data and test helpers
 */
object TestUtils {
    
    /**
     * Creates a mock Movie object for testing
     */
    fun createMockMovie(
        id: Int,
        title: String,
        overview: String = "Test overview for $title",
        voteAverage: Double = 7.5,
        voteCount: Int = 1000,
        popularity: Double = 50.0,
        releaseDate: String = "2024-01-01",
        posterPath: String? = "/poster$id.jpg",
        backdropPath: String? = "/backdrop$id.jpg",
        originalLanguage: String = "en",
        originalTitle: String? = null
    ): Movie {
        return Movie(
            id = id,
            title = title,
            overview = overview,
            posterPath = posterPath,
            backdropPath = backdropPath,
            releaseDate = releaseDate,
            voteAverage = voteAverage,
            voteCount = voteCount,
            popularity = popularity,
            originalLanguage = originalLanguage,
            originalTitle = originalTitle ?: title
        )
    }
    
    /**
     * Creates a list of mock movies
     */
    fun createMockMovieList(count: Int, prefix: String = "Movie"): List<Movie> {
        return (1..count).map { index ->
            createMockMovie(
                id = index,
                title = "$prefix $index"
            )
        }
    }
    
    /**
     * Test API key constant
     */
    const val TEST_API_KEY = "test_api_key_12345"
    
    /**
     * Test language constant
     */
    const val TEST_LANGUAGE = "en-US"
}

