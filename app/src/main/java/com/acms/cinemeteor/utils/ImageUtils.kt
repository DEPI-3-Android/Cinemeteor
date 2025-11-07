package com.acms.cinemeteor.utils

object ImageUtils {
    private const val BASE_IMAGE_URL = "https://image.tmdb.org/t/p/"
    private const val POSTER_SIZE = "w500"
    private const val BACKDROP_SIZE = "w780"
    
    fun getPosterUrl(posterPath: String?): String? {
        return if (posterPath.isNullOrBlank()) {
            null
        } else {
            "${BASE_IMAGE_URL}${POSTER_SIZE}${posterPath}"
        }
    }
    
    fun getBackdropUrl(backdropPath: String?): String? {
        return if (backdropPath.isNullOrBlank()) {
            null
        } else {
            "${BASE_IMAGE_URL}${BACKDROP_SIZE}${backdropPath}"
        }
    }
}

