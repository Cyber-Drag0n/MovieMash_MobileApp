package com.moviemash.app.data

import com.google.gson.annotations.SerializedName

data class ApiMessage(
    val message: String? = null,
    val ok: Boolean? = null
)

data class AuthResponse(
    val token: String? = null,
    val user: UserDto? = null
)

data class UserDto(
    val id: Int? = null,
    val username: String? = null,
    val email: String? = null,
    val display_name: String? = null,
    val role: String? = null,
    val is_verified: Int? = null,
    val is_active: Int? = null,
    val avatar_url: String? = null,
    val created_at: String? = null,
    val last_login: String? = null
)

data class OverviewResponse(
    val user: UserDto? = null,
    val stats: StatsDto? = null,
    val notifications: List<NotificationDto> = emptyList(),
    val lists: LibraryListsDto? = null
)

data class StatsDto(
    val favorites_count: Int? = null,
    val watched_count: Int? = null,
    val watchlist_count: Int? = null,
    val support_messages_count: Int? = null,
    val unread_notifications_count: Int? = null
)

data class NotificationDto(
    val id: Int? = null,
    val title: String? = null,
    val body: String? = null,
    val type: String? = null,
    val link_url: String? = null,
    val is_read: Int? = null,
    val created_at: String? = null
)

data class LibraryListsDto(
    val favorites: List<LibraryItemDto> = emptyList(),
    val watched: List<LibraryItemDto> = emptyList(),
    val watchlist: List<LibraryItemDto> = emptyList()
)

data class LibraryItemDto(
    val tmdb_id: Int? = null,
    val media_type: String? = null,
    val watched: Boolean? = null,
    val user_rating: Int? = null,
    val added_at: String? = null
)

data class ReviewDto(
    val id: Int? = null,
    val tmdb_id: Int? = null,
    val media_type: String? = null,
    val review_text: String? = null,
    val review_status: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    val rating: Int? = null,
    val username: String? = null,
    val display_name: String? = null
)

data class SupportMessageDto(
    val id: Int? = null,
    val name: String? = null,
    val email: String? = null,
    val subject: String? = null,
    val message: String? = null,
    val status: String? = null,
    val reply_text: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)

data class MediaActionResponse(
    val ok: Boolean? = null,
    val active: Boolean? = null,
    val kind: String? = null,
    val rating: Int? = null
)

data class LoginRequest(val login: String, val password: String)
data class RegisterRequest(val username: String, val email: String, val password: String, val display_name: String? = null)
data class ForgotPasswordRequest(val email: String)
data class ResetPasswordRequest(val token: String, val password: String, val passwordRepeat: String)
data class ProfileUpdateRequest(
    val display_name: String? = null,
    val username: String? = null,
    val email: String? = null,
    val password: String? = null,
    val passwordRepeat: String? = null
)
data class SupportRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val subject: String,
    val message: String
)
data class ToggleLibraryRequest(
    val tmdb_id: Int,
    val media_type: String,
    val kind: String
)
data class RateRequest(
    val tmdb_id: Int,
    val media_type: String,
    val rating: Int
)
data class ReviewRequest(
    val tmdb_id: Int,
    val media_type: String,
    val review_text: String,
    val rating: Int
)

data class GenreListResponse(
    val genres: List<GenreDto> = emptyList()
)

data class GenreDto(
    val id: Int? = null,
    val name: String? = null
)

data class PagedResponse<T>(
    val page: Int? = null,
    val results: List<T> = emptyList(),
    val total_pages: Int? = null,
    val total_results: Int? = null
)

data class MediaItemDto(
    val id: Int? = null,
    val title: String? = null,
    val name: String? = null,
    val overview: String? = null,
    val poster_path: String? = null,
    val backdrop_path: String? = null,
    val release_date: String? = null,
    val first_air_date: String? = null,
    val vote_average: Double? = null,
    val vote_count: Int? = null,
    val media_type: String? = null,
    val genre_ids: List<Int> = emptyList()
)

data class MediaDetailsDto(
    val id: Int? = null,
    val title: String? = null,
    val name: String? = null,
    val overview: String? = null,
    val poster_path: String? = null,
    val backdrop_path: String? = null,
    val release_date: String? = null,
    val first_air_date: String? = null,
    val vote_average: Double? = null,
    val vote_count: Int? = null,
    val runtime: Int? = null,
    val episode_run_time: List<Int> = emptyList(),
    val number_of_seasons: Int? = null,
    val number_of_episodes: Int? = null,
    val status: String? = null,
    val tagline: String? = null,
    val genres: List<GenreDto> = emptyList(),
    val credits: CreditsDto? = null,
    val videos: VideosDto? = null,
    @SerializedName("watch/providers")
    val watch_providers: WatchProvidersDto? = null
)

data class CreditsDto(
    val cast: List<CastDto> = emptyList(),
    val crew: List<CrewDto> = emptyList()
)

data class CastDto(
    val id: Int? = null,
    val name: String? = null,
    val character: String? = null,
    val profile_path: String? = null
)

data class CrewDto(
    val id: Int? = null,
    val name: String? = null,
    val job: String? = null
)

data class VideosDto(
    val results: List<VideoDto> = emptyList()
)

data class VideoDto(
    val id: String? = null,
    val name: String? = null,
    val site: String? = null,
    val key: String? = null,
    val type: String? = null
)

data class WatchProvidersDto(
    val results: Map<String, WatchProviderCountryDto>? = null
)

data class WatchProviderCountryDto(
    val flatrate: List<WatchProviderDto> = emptyList(),
    val buy: List<WatchProviderDto> = emptyList(),
    val rent: List<WatchProviderDto> = emptyList()
)

data class WatchProviderDto(
    val provider_id: Int? = null,
    val provider_name: String? = null,
    val logo_path: String? = null
)
