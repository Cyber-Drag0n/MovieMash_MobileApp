package com.moviemash.app.data

import com.moviemash.app.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

private const val TMDB_BASE_URL = "https://api.themoviedb.org/3/"
private const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/"

interface BackendApi {
    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("api/auth/logout")
    suspend fun logout(): ApiMessage

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequest): ApiMessage

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordRequest): ApiMessage

    @GET("api/auth/me")
    suspend fun me(): MeResponse

    @GET("api/account/overview")
    suspend fun overview(): OverviewResponse

    @GET("api/account/reviews")
    suspend fun accountReviews(): ItemsResponse<ReviewDto>

    @GET("api/account/support-messages")
    suspend fun accountSupportMessages(): ItemsResponse<SupportMessageDto>

    @PATCH("api/account/profile")
    suspend fun profile(@Body body: ProfileUpdateRequest): ProfileUpdateResponse

    @POST("api/support/messages")
    suspend fun sendSupport(@Body body: SupportRequest): ApiMessage

    @GET("api/notifications")
    suspend fun notifications(): ItemsResponse<NotificationDto>

    @PATCH("api/notifications/read-all")
    suspend fun readAllNotifications(): ApiMessage

    @PATCH("api/notifications/{id}/read")
    suspend fun readNotification(@Path("id") id: Int): ApiMessage

    @GET("api/library/me")
    suspend fun libraryMe(): LibraryResponse

    @GET("api/library/status")
    suspend fun libraryStatus(@Query("tmdb_id") tmdbId: Int, @Query("media_type") mediaType: String): LibraryStatusResponse

    @POST("api/library/toggle")
    suspend fun toggleLibrary(@Body body: ToggleLibraryRequest): MediaActionResponse

    @POST("api/library/rate")
    suspend fun rateLibrary(@Body body: RateRequest): MediaActionResponse

    @POST("api/reviews")
    suspend fun postReview(@Body body: ReviewRequest): ApiMessage

    @GET("api/reviews/me")
    suspend fun myReview(@Query("tmdb_id") tmdbId: Int, @Query("media_type") mediaType: String): ReviewItemResponse

    @GET("api/reviews/public")
    suspend fun publicReviews(@Query("tmdb_id") tmdbId: Int, @Query("media_type") mediaType: String): ItemsResponse<ReviewDto>
}

data class MeResponse(val user: UserDto? = null)
data class ItemsResponse<T>(val items: List<T> = emptyList())
data class ProfileUpdateResponse(val ok: Boolean? = null, val user: UserDto? = null)
data class LibraryResponse(val lists: LibraryListsDto? = null)
data class LibraryStatusResponse(
    val favorites: Boolean = false,
    val watched: Boolean = false,
    val watchlist: Boolean = false
)
data class ReviewItemResponse(val item: ReviewDto? = null)

interface TmdbApi {
    @GET("trending/movie/week")
    suspend fun trendingMovies(@Query("language") language: String = "ru-RU"): PagedResponse<MediaItemDto>

    @GET("trending/tv/week")
    suspend fun trendingSeries(@Query("language") language: String = "ru-RU"): PagedResponse<MediaItemDto>

    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("language") language: String = "ru-RU",
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("with_genres") genres: String? = null,
        @Query("page") page: Int = 1
    ): PagedResponse<MediaItemDto>

    @GET("discover/tv")
    suspend fun discoverSeries(
        @Query("language") language: String = "ru-RU",
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("with_genres") genres: String? = null,
        @Query("page") page: Int = 1
    ): PagedResponse<MediaItemDto>

    @GET("search/multi")
    suspend fun search(
        @Query("query") query: String,
        @Query("language") language: String = "ru-RU",
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("page") page: Int = 1
    ): PagedResponse<MediaItemDto>

    @GET("movie/{id}")
    suspend fun movieDetails(
        @Path("id") id: Int,
        @Query("language") language: String = "ru-RU",
        @Query("append_to_response") appendToResponse: String = "credits"
    ): MediaDetailsDto

    @GET("tv/{id}")
    suspend fun seriesDetails(
        @Path("id") id: Int,
        @Query("language") language: String = "ru-RU",
        @Query("append_to_response") appendToResponse: String = "credits"
    ): MediaDetailsDto

    @GET("genre/movie/list")
    suspend fun movieGenres(@Query("language") language: String = "ru-RU"): GenreListResponse

    @GET("genre/tv/list")
    suspend fun seriesGenres(@Query("language") language: String = "ru-RU"): GenreListResponse
}

class MovieMashRepository {
    private val tokenInterceptor = TokenInterceptor()
    private val backendClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(tokenInterceptor)
        .build()

    private val tmdbClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            chain.proceed(
                chain.request().newBuilder()
                    .header("Authorization", "Bearer ${BuildConfig.TMDB_BEARER}")
                    .header("accept", "application/json")
                    .build()
            )
        }
        .build()

    private val gson = GsonConverterFactory.create()
    private val backendRetrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(backendClient)
        .addConverterFactory(gson)
        .build()

    private val tmdbRetrofit = Retrofit.Builder()
        .baseUrl(TMDB_BASE_URL)
        .client(tmdbClient)
        .addConverterFactory(gson)
        .build()

    val backend: BackendApi = backendRetrofit.create(BackendApi::class.java)
    val tmdb: TmdbApi = tmdbRetrofit.create(TmdbApi::class.java)

    fun setToken(token: String?) {
        tokenInterceptor.token = token
    }

    fun imageUrl(path: String?, size: String = "w500"): String? {
        if (path.isNullOrBlank()) return null
        return IMAGE_BASE_URL + size + path
    }

    private class TokenInterceptor : Interceptor {
        @Volatile var token: String? = null

        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            val request = chain.request().newBuilder().apply {
                token?.let { header("Authorization", "Bearer $it") }
                header("Accept", "application/json")
            }.build()
            return chain.proceed(request)
        }
    }
}
