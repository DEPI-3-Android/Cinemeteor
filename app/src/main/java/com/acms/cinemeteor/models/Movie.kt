package com.acms.cinemeteor.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


@Parcelize
data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("backdrop_path")
    val backdropPath: String?,
    @SerializedName("release_date")
    val releaseDate: String?,
    @SerializedName("vote_average")
    val voteAverage: Double,
    @SerializedName("vote_count")
    val voteCount: Int,
    val popularity: Double?,
    @SerializedName("original_language")
    val originalLanguage: String?,
    @SerializedName("original_title")
    val originalTitle: String?
): Parcelable

data class MovieResponse(
    val page: Int,
    val results: List<Movie>,
    @SerializedName("total_pages")
    val totalPages: Int,
    @SerializedName("total_results")
    val totalResults: Int
)

data class Video(
    val id: String,
    @SerializedName("iso_3166_1")
    val iso31661: String?,
    @SerializedName("iso_639_1")
    val iso6391: String?,
    val key: String,
    val name: String,
    val site: String,
    val type: String
)

data class MovieVideosResponse(
    val id: Int,
    val results: List<Video>
)

data class Review(
    val id: String,
    val author: String,
    val content: String,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("author_details")
    val authorDetails: AuthorDetails? = null
)

data class AuthorDetails(
    val name: String?,
    val username: String?,
    @SerializedName("avatar_path")
    val avatarPath: String?,
    val rating: Double?
)

data class MovieReviewsResponse(
    val id: Int,
    val page: Int,
    val results: List<Review>,
    @SerializedName("total_pages")
    val totalPages: Int,
    @SerializedName("total_results")
    val totalResults: Int
)

