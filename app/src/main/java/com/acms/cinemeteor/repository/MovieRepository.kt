package com.acms.cinemeteor.repository

import android.util.Log
import com.acms.cinemeteor.api.RetrofitClient
import com.acms.cinemeteor.models.Movie
import com.acms.cinemeteor.models.MovieResponse
import com.acms.cinemeteor.models.MovieVideosResponse
import com.acms.cinemeteor.models.MovieReviewsResponse
import retrofit2.Response

class MovieRepository {
    private val apiService = RetrofitClient.tmdbApiService
    
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
    
    suspend fun getTrendingMovies(apiKey: String, language: String = "en-US", page: Int = 1): Result<List<Movie>> {
        return try {
            Log.d("MovieRepository", "Fetching trending movies...")
            val response = apiService.getTrendingMovies(apiKey, page, language)
            Log.d("MovieRepository", "Trending movies response: code=${response.code()}, isSuccess=${response.isSuccessful}")
            if (response.isSuccessful) {
                val movies = response.body()?.results ?: emptyList()
                Log.d("MovieRepository", "Trending movies count: ${movies.size}")
                Result.success(movies)
            } else {
                val errorMsg = "API Error: ${response.code()} - ${response.message()}"
                Log.e("MovieRepository", errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("MovieRepository", "Exception loading trending movies", e)
            Result.failure(e)
        }
    }
    
    suspend fun getNowPlayingMovies(apiKey: String, language: String = "en-US", page: Int = 1): Result<List<Movie>> {
        return try {
            val response = apiService.getNowPlayingMovies(apiKey, page, language)
            if (response.isSuccessful) {
                Result.success(response.body()?.results ?: emptyList())
            } else {
                Result.failure(Exception("API Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun searchMovies(apiKey: String, query: String, language: String = "en-US", page: Int = 1): Result<List<Movie>> {
        return try {
            if (query.isBlank()) {
                Log.d("MovieRepository", "Search query is blank")
                return Result.success(emptyList())
            }
            Log.d("MovieRepository", "Searching movies with query: '$query'")
            val response = apiService.searchMovies(apiKey, query, page, language)
            Log.d("MovieRepository", "Search response: code=${response.code()}, isSuccess=${response.isSuccessful}")
            if (response.isSuccessful) {
                val movies = response.body()?.results ?: emptyList()
                Log.d("MovieRepository", "Search results count: ${movies.size}")
                Result.success(movies)
            } else {
                val errorMsg = "API Error: ${response.code()} - ${response.message()}"
                Log.e("MovieRepository", errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("MovieRepository", "Exception searching movies", e)
            Result.failure(e)
        }
    }
    
    suspend fun getTopRatedMovies(apiKey: String, language: String = "en-US", page: Int = 1): Result<List<Movie>> {
        return try {
            val response = apiService.getTopRatedMovies(apiKey, page, language)
            if (response.isSuccessful) {
                Result.success(response.body()?.results ?: emptyList())
            } else {
                Result.failure(Exception("API Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUpcomingMovies(apiKey: String, language: String = "en-US", page: Int = 1): Result<List<Movie>> {
        return try {
            Log.d("MovieRepository", "Fetching upcoming movies...")
            val response = apiService.getUpcomingMovies(apiKey, page, language)
            Log.d("MovieRepository", "Upcoming movies response: code=${response.code()}, isSuccess=${response.isSuccessful}")
            if (response.isSuccessful) {
                val movies = response.body()?.results ?: emptyList()
                Log.d("MovieRepository", "Upcoming movies count: ${movies.size}")
                Result.success(movies)
            } else {
                val errorMsg = "API Error: ${response.code()} - ${response.message()}"
                Log.e("MovieRepository", errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("MovieRepository", "Exception loading upcoming movies", e)
            Result.failure(e)
        }
    }
    
    suspend fun getMovieDetails(apiKey: String, movieId: Int, language: String = "en-US"): Result<Movie> {
        return try {
            Log.d("MovieRepository", "Fetching movie details for ID: $movieId with language: $language")
            val response = apiService.getMovieDetails(movieId, apiKey, language)
            Log.d("MovieRepository", "Movie details response: code=${response.code()}, isSuccess=${response.isSuccessful}")
            if (response.isSuccessful) {
                val movie = response.body()
                if (movie != null) {
                    Log.d("MovieRepository", "Movie details loaded successfully: ${movie.title}")
                    Result.success(movie)
                } else {
                    Log.e("MovieRepository", "Movie details response body is null")
                    Result.failure(Exception("No movie details found"))
                }
            } else {
                val errorMsg = "API Error: ${response.code()} - ${response.message()}"
                Log.e("MovieRepository", errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("MovieRepository", "Exception loading movie details", e)
            Result.failure(e)
        }
    }
    
    /**
     * Gets movie details with fallback to English if title or overview is empty
     */
    suspend fun getMovieDetailsWithFallback(apiKey: String, movieId: Int, language: String = "en-US"): Result<Movie> {
        return try {
            // First try the requested language
            val primaryResult = getMovieDetails(apiKey, movieId, language)
            
            return primaryResult.fold(
                onSuccess = { movie ->
                    // Check if title or overview is missing/empty
                    val needsFallback = movie.title.isBlank() || movie.overview.isBlank()
                    
                    if (needsFallback && language != "en-US") {
                        Log.d("MovieRepository", "Title or overview empty in $language, fetching English fallback")
                        // Fetch English version as fallback
                        val englishResult = getMovieDetails(apiKey, movieId, "en-US")
                        englishResult.fold(
                            onSuccess = { englishMovie ->
                                // Merge: use primary language when available, fallback to English
                                val mergedMovie = movie.copy(
                                    title = if (movie.title.isBlank()) englishMovie.title else movie.title,
                                    overview = if (movie.overview.isBlank()) englishMovie.overview else movie.overview
                                )
                                Log.d("MovieRepository", "Merged movie with English fallback: ${mergedMovie.title}")
                                Result.success(mergedMovie)
                            },
                            onFailure = {
                                // If English fetch fails, return the primary language version
                                Log.w("MovieRepository", "Failed to fetch English fallback, using primary language")
                                Result.success(movie)
                            }
                        )
                    } else {
                        // Primary language has all data, return it
                        Result.success(movie)
                    }
                },
                onFailure = { exception ->
                    // If primary language fetch fails and language is not English, try English
                    if (language != "en-US") {
                        Log.d("MovieRepository", "Primary language failed, trying English fallback")
                        getMovieDetails(apiKey, movieId, "en-US")
                    } else {
                        Result.failure(exception)
                    }
                }
            )
        } catch (e: Exception) {
            Log.e("MovieRepository", "Exception in getMovieDetailsWithFallback", e)
            Result.failure(e)
        }
    }
    
    suspend fun getMovieVideos(apiKey: String, movieId: Int, language: String = "en-US"): Result<MovieVideosResponse> {
        return try {
            Log.d("MovieRepository", "Fetching videos for movie ID: $movieId with language: $language")
            val response = apiService.getMovieVideos(movieId, apiKey, language)
            Log.d("MovieRepository", "Movie videos response: code=${response.code()}, isSuccess=${response.isSuccessful}")
            if (response.isSuccessful) {
                val videosResponse = response.body()
                if (videosResponse != null) {
                    Log.d("MovieRepository", "Movie videos loaded successfully: ${videosResponse.results.size} videos")
                    Result.success(videosResponse)
                } else {
                    Log.e("MovieRepository", "Movie videos response body is null")
                    Result.failure(Exception("No videos found"))
                }
            } else {
                val errorMsg = "API Error: ${response.code()} - ${response.message()}"
                Log.e("MovieRepository", errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("MovieRepository", "Exception loading movie videos", e)
            Result.failure(e)
        }
    }
    
    suspend fun getSimilarMovies(apiKey: String, movieId: Int, language: String = "en-US", page: Int = 1): Result<List<Movie>> {
        return try {
            Log.d("MovieRepository", "Fetching similar movies for movie ID: $movieId with language: $language")
            val response = apiService.getSimilarMovies(movieId, apiKey, page, language)
            Log.d("MovieRepository", "Similar movies response: code=${response.code()}, isSuccess=${response.isSuccessful}")
            if (response.isSuccessful) {
                val movies = response.body()?.results ?: emptyList()
                Log.d("MovieRepository", "Similar movies loaded successfully: ${movies.size} movies")
                Result.success(movies)
            } else {
                val errorMsg = "API Error: ${response.code()} - ${response.message()}"
                Log.e("MovieRepository", errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("MovieRepository", "Exception loading similar movies", e)
            Result.failure(e)
        }
    }
    
    suspend fun getMovieReviews(apiKey: String, movieId: Int, language: String = "en-US", page: Int = 1): Result<MovieReviewsResponse> {
        return try {
            Log.d("MovieRepository", "Fetching reviews for movie ID: $movieId with language: $language")
            val response = apiService.getMovieReviews(movieId, apiKey, page, language)
            Log.d("MovieRepository", "Movie reviews response: code=${response.code()}, isSuccess=${response.isSuccessful}")
            if (response.isSuccessful) {
                val reviewsResponse = response.body()
                if (reviewsResponse != null) {
                    Log.d("MovieRepository", "Movie reviews loaded successfully: ${reviewsResponse.results.size} reviews")
                    Result.success(reviewsResponse)
                } else {
                    Log.e("MovieRepository", "Movie reviews response body is null")
                    Result.failure(Exception("No reviews found"))
                }
            } else {
                val errorMsg = "API Error: ${response.code()} - ${response.message()}"
                Log.e("MovieRepository", errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("MovieRepository", "Exception loading movie reviews", e)
            Result.failure(e)
        }
    }
}

