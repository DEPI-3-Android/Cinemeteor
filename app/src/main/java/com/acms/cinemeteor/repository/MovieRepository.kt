package com.acms.cinemeteor.repository

import android.util.Log
import com.acms.cinemeteor.api.RetrofitClient
import com.acms.cinemeteor.models.Movie
import com.acms.cinemeteor.models.MovieResponse
import retrofit2.Response

class MovieRepository {
    private val apiService = RetrofitClient.tmdbApiService
    
    suspend fun getPopularMovies(apiKey: String, page: Int = 1): Result<List<Movie>> {
        return try {
            Log.d("MovieRepository", "Fetching popular movies...")
            val response = apiService.getPopularMovies(apiKey, page)
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
    
    suspend fun getTrendingMovies(apiKey: String, page: Int = 1): Result<List<Movie>> {
        return try {
            Log.d("MovieRepository", "Fetching trending movies...")
            val response = apiService.getTrendingMovies(apiKey, page)
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
    
    suspend fun getNowPlayingMovies(apiKey: String, page: Int = 1): Result<List<Movie>> {
        return try {
            val response = apiService.getNowPlayingMovies(apiKey, page)
            if (response.isSuccessful) {
                Result.success(response.body()?.results ?: emptyList())
            } else {
                Result.failure(Exception("API Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun searchMovies(apiKey: String, query: String, page: Int = 1): Result<List<Movie>> {
        return try {
            if (query.isBlank()) {
                Log.d("MovieRepository", "Search query is blank")
                return Result.success(emptyList())
            }
            Log.d("MovieRepository", "Searching movies with query: '$query'")
            val response = apiService.searchMovies(apiKey, query, page)
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
    
    suspend fun getTopRatedMovies(apiKey: String, page: Int = 1): Result<List<Movie>> {
        return try {
            val response = apiService.getTopRatedMovies(apiKey, page)
            if (response.isSuccessful) {
                Result.success(response.body()?.results ?: emptyList())
            } else {
                Result.failure(Exception("API Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

