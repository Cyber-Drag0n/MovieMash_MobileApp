package com.moviemash.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import android.util.Patterns
import androidx.compose.ui.platform.LocalUriHandler
import java.net.URLEncoder
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.input.KeyboardCapitalization
import coil.compose.AsyncImage
import com.moviemash.app.R
import com.moviemash.app.data.ForgotPasswordRequest
import com.moviemash.app.data.GenreDto
import com.moviemash.app.data.LibraryItemDto
import com.moviemash.app.data.LoginRequest
import com.moviemash.app.data.MediaDetailsDto
import com.moviemash.app.data.MediaItemDto
import com.moviemash.app.data.MovieMashRepository
import com.moviemash.app.data.NotificationDto
import com.moviemash.app.data.OverviewResponse
import com.moviemash.app.data.ProfileUpdateRequest
import com.moviemash.app.data.RateRequest
import com.moviemash.app.data.RegisterRequest
import com.moviemash.app.data.ResetPasswordRequest
import com.moviemash.app.data.ReviewDto
import com.moviemash.app.data.ReviewRequest
import com.moviemash.app.data.SessionManager
import com.moviemash.app.data.SupportMessageDto
import com.moviemash.app.data.SupportRequest
import com.moviemash.app.data.ToggleLibraryRequest
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val LANG = "ru-RU"

enum class AuthMode { Login, Register }

private data class ValidationResult(val valid: Boolean, val message: String? = null)

private fun isValidEmail(value: String): Boolean =
    Patterns.EMAIL_ADDRESS.matcher(value.trim()).matches()

private fun authLoginValidation(login: String, password: String): ValidationResult {
    if (login.trim().length < 3) return ValidationResult(false, "Логин минимум 3 символа")
    if (password.length < 6) return ValidationResult(false, "Пароль минимум 6 символов")
    return ValidationResult(true)
}

private fun authRegisterValidation(
    username: String,
    displayName: String,
    email: String,
    password: String,
    repeatPassword: String
): ValidationResult {
    if (username.trim().length < 3) return ValidationResult(false, "Логин минимум 3 символа")
    if (displayName.trim().length < 2) return ValidationResult(false, "Имя минимум 2 символа")
    if (!isValidEmail(email)) return ValidationResult(false, "Некорректный email")
    if (password.length < 6) return ValidationResult(false, "Пароль минимум 6 символов")
    if (password != repeatPassword) return ValidationResult(false, "Пароли не совпадают")
    return ValidationResult(true)
}

private fun supportValidation(
    firstName: String,
    lastName: String,
    email: String,
    subject: String,
    message: String
): ValidationResult {
    if (firstName.trim().length < 2) return ValidationResult(false, "Имя минимум 2 символа")
    if (lastName.trim().length < 2) return ValidationResult(false, "Фамилия минимум 2 символа")
    if (!isValidEmail(email)) return ValidationResult(false, "Некорректный email")
    if (subject.trim().length < 3) return ValidationResult(false, "Тема минимум 3 символа")
    if (message.trim().length < 10) return ValidationResult(false, "Сообщение минимум 10 символов")
    return ValidationResult(true)
}

private fun profileValidation(
    displayName: String,
    username: String,
    email: String,
    password: String,
    passwordRepeat: String
): ValidationResult {
    if (displayName.trim().length < 2) return ValidationResult(false, "Имя минимум 2 символа")
    if (username.trim().length < 3) return ValidationResult(false, "Логин минимум 3 символа")
    if (!isValidEmail(email)) return ValidationResult(false, "Некорректный email")
    if (password.isNotBlank() || passwordRepeat.isNotBlank()) {
        if (password.length < 6) return ValidationResult(false, "Пароль минимум 6 символов")
        if (password != passwordRepeat) return ValidationResult(false, "Пароли не совпадают")
    }
    return ValidationResult(true)
}

private fun reviewValidation(text: String): ValidationResult {
    if (text.trim().length < 20) return ValidationResult(false, "Отзыв минимум 20 символов")
    return ValidationResult(true)
}

private fun mediaTitle(item: MediaItemDto): String = item.title ?: item.name ?: "Без названия"
private fun mediaYear(item: MediaItemDto): String =
    (item.release_date ?: item.first_air_date ?: "").take(4).ifBlank { "—" }

private fun detailsTitle(item: MediaDetailsDto): String = item.title ?: item.name ?: "Без названия"
private fun detailsYear(item: MediaDetailsDto): String =
    (item.release_date ?: item.first_air_date ?: "").take(4).ifBlank { "—" }

private fun encodeUrl(value: String): String =
    URLEncoder.encode(value.trim(), "UTF-8")

private fun buildVkVideoUrl(query: String): String =
    "https://vkvideo.ru/?q=${encodeUrl(query)}"

private fun normalizeProviderName(name: String?): String =
    name.orEmpty()
        .lowercase()
        .replace(Regex("[\\s\\u00A0]+"), "")
        .replace(Regex("[^a-zа-я0-9+]+"), "")

private fun buildProviderUrl(providerName: String?, title: String): String {
    val q = encodeUrl(title)
    val n = normalizeProviderName(providerName)
    return when {
        n.contains("vkvideo") || n.contains("vk") -> "https://vkvideo.ru/?q=$q"
        n.contains("primevideo") || n.contains("amazon") -> "https://www.primevideo.com/search/ref=atv_nb_sr?ie=UTF8&phrase=$q"
        n.contains("disney") -> "https://www.disneyplus.com/browse/search?q=$q"
        n.contains("netflix") -> "https://www.netflix.com/search?q=$q"
        n.contains("appletv") || n.contains("apple") -> "https://tv.apple.com/search?term=$q"
        n.contains("hulu") -> "https://www.hulu.com/search?q=$q"
        n.contains("paramount") -> "https://www.paramountplus.com/search/?query=$q"
        n.contains("peacock") -> "https://www.peacocktv.com/watch/search/$q"
        n.contains("crunchyroll") -> "https://www.crunchyroll.com/search?from=&q=$q"
        n.contains("mubi") -> "https://mubi.com/search?query=$q"
        n.contains("plex") -> "https://watch.plex.tv/search?query=$q"
        n.contains("youtube") -> "https://www.youtube.com/results?search_query=$q"
        else -> "https://www.google.com/search?q=${encodeUrl("${providerName.orEmpty()} $title") }"
    }
}

private fun uniqueProviders(country: com.moviemash.app.data.WatchProviderCountryDto?): List<com.moviemash.app.data.WatchProviderDto> {
    val merged = country?.let { it.flatrate + it.buy + it.rent }.orEmpty()
    return merged.distinctBy { it.provider_id ?: it.provider_name.orEmpty() }
}

private fun repositoryType(item: MediaItemDto): String =
    item.media_type ?: if (item.title != null) "movie" else "tv"

@Composable
fun HomeScreen(
    repository: MovieMashRepository,
    onOpenMedia: (String, Int) -> Unit,
    onOpenBrowse: () -> Unit,
    onOpenSupport: () -> Unit,
    onOpenSubscriptions: () -> Unit,
    onOpenAuth: () -> Unit
) {
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var movies by remember { mutableStateOf(emptyList<MediaItemDto>()) }
    var series by remember { mutableStateOf(emptyList<MediaItemDto>()) }
    var genres by remember { mutableStateOf(emptyList<GenreDto>()) }

    LaunchedEffect(Unit) {
        loading = true
        error = null
        runCatching {
            movies = repository.tmdb.trendingMovies(LANG).results.filter { it.id != null }.take(20)
            series = repository.tmdb.trendingSeries(LANG).results.filter { it.id != null }.take(20)
            genres = repository.tmdb.movieGenres(LANG).genres.take(10)
        }.onFailure {
            error = it.message ?: "Не удалось загрузить главную"
        }
        loading = false
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            HeroBanner(
                title = "Лучшее приложение для фильмов",
                subtitle = "Фильмы, сериалы, избранное, отзывы и поддержка в одном нативном приложении MovieMash.",
                onPrimary = onOpenBrowse,
                onSecondary = onOpenSubscriptions
            )
        }

        item { SectionTitle("Популярные фильмы") }
        item {
            when {
                loading -> LoadingRow()
                error != null -> ErrorCard(error!!, onRetry = onOpenBrowse)
                else -> PosterRow(
                    mediaItems = movies,
                    repository = repository,
                    onOpenMedia = { id -> onOpenMedia("movie", id) }
                )
            }
        }

        item { SectionTitle("Популярные сериалы") }
        item {
            when {
                loading -> LoadingRow()
                error != null -> ErrorCard(error!!, onRetry = onOpenBrowse)
                else -> PosterRow(
                    mediaItems = series,
                    repository = repository,
                    onOpenMedia = { id -> onOpenMedia("tv", id) }
                )
            }
        }
    }
}

@Composable
private fun HeroBanner(
    title: String,
    subtitle: String,
    onPrimary: () -> Unit,
    onSecondary: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(610.dp)
            .background(AppBg)
    ) {
        Image(
            painter = painterResource(R.drawable.hero_home),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0x77141414), AppBg),
                        startY = 150f
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title,
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(14.dp))
            Text(
                subtitle,
                color = Muted,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                maxLines = 6,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(26.dp))
            Button(
                onClick = onPrimary,
                modifier = Modifier
                    .fillMaxWidth(0.64f)
                    .height(58.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(7.dp))
                Text("Начать просмотр", fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun ActionStrip(items: List<Pair<String, () -> Unit>>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        items(items.size) { index ->
            val item = items[index]
            AssistChip(
                onClick = item.second,
                label = { Text(item.first) },
                leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) }
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        color = Color.White,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp)
    )
}

@Composable
private fun GenreChip(title: String) {
    Box(
        modifier = Modifier
            .border(1.dp, Stroke, RoundedCornerShape(8.dp))
            .background(CardBg, RoundedCornerShape(8.dp))
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        Text(title, color = Color.White, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun LoadingRow() {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(5) {
            Box(
                modifier = Modifier
                    .width(150.dp)
                    .height(220.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF1A1A1A))
            )
        }
    }
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171717))
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(message, color = Color(0xFFFF8080))
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onRetry) { Text("Повторить") }
        }
    }
}

@Composable
private fun PosterRow(
    mediaItems: List<MediaItemDto>,
    repository: MovieMashRepository,
    onOpenMedia: (Int) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(mediaItems.size) { index ->
            val item = mediaItems[index]
            val id = item.id ?: return@items
            MediaPosterCard(
                title = mediaTitle(item),
                subtitle = mediaYear(item),
                poster = repository.imageUrl(item.poster_path, "w500"),
                onClick = { onOpenMedia(id) }
            )
        }
    }
}

@Composable
private fun MediaPosterCard(
    title: String,
    subtitle: String,
    poster: String?,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(150.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(214.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(CardBg)
        ) {
            if (poster != null) {
                AsyncImage(
                    model = poster,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleSmall, color = Color.White)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${subtitle}", color = Muted, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.width(8.dp))
            Text("★★★★★", color = BrandRed, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun FeaturedMediaCard(
    title: String,
    imageRes: Int? = null,
    imageUrl: String? = null,
    onWatch: () -> Unit,
    onBookmark: (() -> Unit)? = null,
    onFavorite: (() -> Unit)? = null,
    onNotify: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(458.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(CardBg)
    ) {
        when {
            imageUrl != null -> AsyncImage(
                model = imageUrl,
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            imageRes != null -> Image(
                painter = painterResource(imageRes),
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xEE141414))))
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, color = Color.White, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = onWatch,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                shape = RoundedCornerShape(7.dp)
            ) {
                Icon(Icons.Default.PlayArrow, null)
                Spacer(Modifier.width(7.dp))
                Text("Смотреть", fontWeight = FontWeight.ExtraBold)
            }
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                listOf(
                    Triple(Icons.Default.BookmarkBorder, "Смотреть позже", onBookmark),
                    Triple(Icons.Default.Favorite, "Избранное", onFavorite),
                    Triple(Icons.Default.Notifications, "Уведомления", onNotify)
                ).forEach { (icon, description, action) ->
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .border(1.dp, Stroke, RoundedCornerShape(8.dp))
                            .background(Color(0xCC0F0F0F), RoundedCornerShape(8.dp))
                            .clickable(enabled = action != null) { action?.invoke() },
                        contentAlignment = Alignment.Center
                    ) { Icon(icon, contentDescription = description, tint = Color.White) }
                }
            }
        }
    }
}

@Composable
fun BrowseScreen(
    repository: MovieMashRepository,
    onOpenMedia: (String, Int) -> Unit
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var query by rememberSaveable { mutableStateOf("") }
    var mode by rememberSaveable { mutableStateOf("all") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var results by remember { mutableStateOf(emptyList<MediaItemDto>()) }

    fun validation(): ValidationResult {
        if (query.trim().length < 2) return ValidationResult(false, "Введите минимум 2 символа")
        return ValidationResult(true)
    }

    fun runSearch() {
        val v = validation()
        if (!v.valid) {
            error = v.message
            results = emptyList()
            return
        }
        scope.launch {
            loading = true
            error = null
            runCatching {
                val all = repository.tmdb.search(query.trim(), LANG).results.filter { it.id != null }
                results = when (mode) {
                    "movie" -> all.filter { it.title != null || it.media_type == "movie" }
                    "tv" -> all.filter { it.name != null || it.media_type == "tv" }
                    else -> all
                }.take(30)
            }.onFailure {
                error = it.message ?: "Ошибка поиска"
            }
            loading = false
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .border(1.dp, Stroke, RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("movie" to "Фильмы", "tv" to "Сериалы").forEach { (key, label) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (mode == key) CardBg else Color.Transparent, RoundedCornerShape(6.dp))
                            .clickable { mode = key }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) { Text(label, color = if (mode == key) Color.White else Muted, style = MaterialTheme.typography.titleSmall) }
                }
            }
        }
        item { SectionTitle("Наши жанры") }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                label = { Text("Поиск фильмов и сериалов") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandRed,
                    unfocusedBorderColor = Stroke,
                    focusedContainerColor = CardBg,
                    unfocusedContainerColor = CardBg
                ),
                singleLine = true,
                isError = query.isNotBlank() && !validation().valid,
                supportingText = {
                    if (query.isNotBlank() && !validation().valid) {
                        Text(validation().message ?: "")
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search,
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Sentences
                ),
                keyboardActions = KeyboardActions(onSearch = {
                    focusManager.clearFocus()
                    runSearch()
                })
            )
        }

        item {
            Button(
                onClick = {
                    focusManager.clearFocus()
                    runSearch()
                },
                enabled = query.trim().length >= 2 && !loading,
                colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Найти")
            }
        }

        item {
            if (loading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        if (error != null) {
            item { ErrorCard(error!!, onRetry = { runSearch() }) }
        }

        items(results.size) { index ->
            val item = results[index]
            val id = item.id ?: return@items
            val type = repositoryType(item)
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onOpenMedia(type, id) },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF151515)),
                shape = RoundedCornerShape(22.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(92.dp)
                            .height(138.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF222222))
                    ) {
                        val poster = repository.imageUrl(item.poster_path, "w300")
                        if (poster != null) {
                            AsyncImage(
                                model = poster,
                                contentDescription = mediaTitle(item),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        Text(mediaTitle(item), style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(mediaYear(item), color = Color(0xFFBDBDBD))
                        Spacer(Modifier.height(6.dp))
                        Text(item.overview.orEmpty(), maxLines = 4, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }

    LaunchedEffect(query, mode) {
        if (query.trim().length >= 2) {
            runSearch()
        }
    }
}

@Composable
fun MovieScreen(
    repository: MovieMashRepository,
    token: String?,
    mediaType: String,
    tmdbId: Int,
    onOpenAuth: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    var loading by remember { mutableStateOf(true) }
    var details by remember { mutableStateOf<MediaDetailsDto?>(null) }
    var publicReviews by remember { mutableStateOf(emptyList<ReviewDto>()) }
    var status by remember { mutableStateOf(Triple(false, false, false)) }
    var rating by remember { mutableFloatStateOf(5f) }
    var reviewText by rememberSaveable { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    fun refresh() {
        scope.launch {
            loading = true
            runCatching {
                val loaded = if (mediaType == "tv") {
                    repository.tmdb.seriesDetails(tmdbId, LANG)
                } else {
                    repository.tmdb.movieDetails(tmdbId, LANG)
                }
                details = loaded

                if (token != null) {
                    val st = runCatching { repository.backend.libraryStatus(tmdbId, mediaType) }.getOrNull()
                    status = Triple(st?.favorites == true, st?.watched == true, st?.watchlist == true)
                    val myReview = runCatching { repository.backend.myReview(tmdbId, mediaType).item }.getOrNull()
                    myReview?.let {
                        reviewText = it.review_text.orEmpty()
                        rating = (it.rating ?: 5).toFloat()
                    }
                }
                publicReviews = runCatching { repository.backend.publicReviews(tmdbId, mediaType).items }.getOrDefault(emptyList())
            }.onFailure {
                message = it.message ?: "Не удалось загрузить страницу"
            }
            loading = false
        }
    }

    LaunchedEffect(mediaType, tmdbId, token) {
        refresh()
    }

    val d = details
    if (loading && d == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    if (d == null) return

    val poster = repository.imageUrl(d.backdrop_path ?: d.poster_path, "w780")
    val title = detailsTitle(d)
    val vkVideoUrl = buildVkVideoUrl(title)
    val watchRegions = d.watch_providers?.results.orEmpty()
    val preferredRegion = when {
        watchRegions.containsKey("RU") -> "RU"
        watchRegions.containsKey("US") -> "US"
        watchRegions.containsKey("GB") -> "GB"
        else -> watchRegions.keys.firstOrNull()
    }
    val providers = uniqueProviders(preferredRegion?.let { watchRegions[it] })

    LazyColumn(
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            FeaturedMediaCard(
                title = title,
                imageUrl = poster,
                onWatch = { uriHandler.openUri(vkVideoUrl) },
                onBookmark = {
                    if (token == null) onOpenAuth() else scope.launch {
                        runCatching {
                            repository.backend.toggleLibrary(ToggleLibraryRequest(tmdbId, mediaType, "watchlist"))
                            refresh()
                        }.onFailure { message = it.message }
                    }
                },
                onFavorite = {
                    if (token == null) onOpenAuth() else scope.launch {
                        runCatching {
                            repository.backend.toggleLibrary(ToggleLibraryRequest(tmdbId, mediaType, "favorites"))
                            refresh()
                        }.onFailure { message = it.message }
                    }
                },
            )
        }

        item {
            Card(
                modifier = Modifier.padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Описание:", color = Muted, style = MaterialTheme.typography.titleSmall)
                    Text(d.overview.orEmpty(), color = Muted, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        item {
            Card(
                modifier = Modifier.padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Где смотреть", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    Button(
                        onClick = { uriHandler.openUri(vkVideoUrl) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                        shape = RoundedCornerShape(7.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Смотреть в VK Video", fontWeight = FontWeight.Bold)
                    }

                    if (providers.isNotEmpty()) {
                        Text("Провайдеры ${preferredRegion ?: ""}", color = Muted, style = MaterialTheme.typography.bodyMedium)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(providers.size) { index ->
                                val provider = providers[index]
                                AssistChip(
                                    onClick = { uriHandler.openUri(buildProviderUrl(provider.provider_name, title)) },
                                    label = { Text(provider.provider_name.orEmpty()) },
                                    leadingIcon = {
                                        val logo = repository.imageUrl(provider.logo_path, "w92")
                                        if (logo != null) {
                                            AsyncImage(
                                                model = logo,
                                                contentDescription = provider.provider_name,
                                                modifier = Modifier.size(24.dp).clip(RoundedCornerShape(6.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    } else {
                        Text("Официальные провайдеры не найдены, доступен поиск по названию в VK Video.", color = Muted)
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = status.first,
                        onClick = {
                            if (token == null) {
                                onOpenAuth()
                                return@FilterChip
                            }
                            scope.launch {
                                runCatching {
                                    repository.backend.toggleLibrary(
                                        ToggleLibraryRequest(tmdbId, mediaType, "favorites")
                                    )
                                    refresh()
                                }.onFailure { message = it.message }
                            }
                        },
                        label = { Text("Избранное") },
                        leadingIcon = { Icon(Icons.Default.Favorite, null) }
                    )
                    FilterChip(
                        selected = status.third,
                        onClick = {
                            if (token == null) {
                                onOpenAuth()
                                return@FilterChip
                            }
                            scope.launch {
                                runCatching {
                                    repository.backend.toggleLibrary(
                                        ToggleLibraryRequest(tmdbId, mediaType, "watchlist")
                                    )
                                    refresh()
                                }.onFailure { message = it.message }
                            }
                        },
                        label = { Text("Смотреть позже") },
                        leadingIcon = { Icon(Icons.Default.BookmarkBorder, null) }
                    )
                }

                if (token == null) {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF151515))) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Чтобы ставить оценки и писать отзывы, войдите в аккаунт.")
                            Spacer(Modifier.height(10.dp))
                            Button(onClick = onOpenAuth) { Text("Войти") }
                        }
                    }
                } else {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF151515))) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Мои действия", style = MaterialTheme.typography.titleMedium)

                            Text("Оценка: ${rating.roundToInt()} / 5")
                            Slider(
                                value = rating,
                                onValueChange = { rating = it },
                                valueRange = 1f..5f,
                                steps = 3
                            )

                            OutlinedTextField(
                                value = reviewText,
                                onValueChange = { reviewText = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Отзыв") },
                                minLines = 4,
                                supportingText = {
                                    val v = reviewValidation(reviewText)
                                    if (!v.valid) Text(v.message ?: "")
                                },
                                isError = reviewText.isNotBlank() && !reviewValidation(reviewText).valid
                            )

                            Button(
                                onClick = {
                                    val valid = reviewValidation(reviewText)
                                    if (!valid.valid) {
                                        message = valid.message
                                        return@Button
                                    }
                                    scope.launch {
                                        runCatching {
                                            repository.backend.rateLibrary(
                                                RateRequest(tmdbId, mediaType, rating.roundToInt())
                                            )
                                            repository.backend.postReview(
                                                ReviewRequest(
                                                    tmdb_id = tmdbId,
                                                    media_type = mediaType,
                                                    review_text = reviewText.trim(),
                                                    rating = rating.roundToInt()
                                                )
                                            )
                                            message = "Отзыв отправлен"
                                            refresh()
                                        }.onFailure {
                                            message = it.message
                                        }
                                    }
                                },
                                enabled = reviewText.trim().length >= 7
                            ) {
                                Text("Отправить отзыв")
                            }
                        }
                    }
                }

                if (message != null) {
                    Text(message!!, color = Color(0xFFFFB3B3))
                }
            }
        }

        item { SectionTitle("Актёры") }
        item {
            val castList = d.credits?.cast.orEmpty().take(12)

            if (castList.isEmpty()) {
                Text(
                    "Актёры не найдены",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color(0xFFBDBDBD)
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(castList.size) { index ->
                        val cast = castList[index]
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(92.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(78.dp)
                                    .height(78.dp)
                                    .clip(RoundedCornerShape(39.dp))
                                    .background(Color(0xFF242424))
                            ) {
                                val photo = repository.imageUrl(cast.profile_path, "w185")
                                if (photo != null) {
                                    AsyncImage(
                                        model = photo,
                                        contentDescription = cast.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                cast.name.orEmpty(),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        item { SectionTitle("Отзывы") }
        item {
            Column(
                Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (publicReviews.isEmpty()) {
                    Text("Пока нет одобренных отзывов.", color = Muted)
                } else {
                    publicReviews.take(8).forEach { review ->
                        Card(colors = CardDefaults.cardColors(containerColor = CardBg), shape = RoundedCornerShape(8.dp)) {
                            Column(Modifier.padding(14.dp)) {
                                Text(review.display_name ?: review.username ?: "Пользователь", style = MaterialTheme.typography.titleSmall)
                                Text(review.review_text.orEmpty(), color = Muted, maxLines = 4, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccountScreen(
    repository: MovieMashRepository,
    sessionManager: SessionManager,
    token: String?,
    onOpenMedia: (String, Int) -> Unit,
    onOpenAuth: () -> Unit,
    onOpenSupport: () -> Unit,
    onOpenNotifications: () -> Unit,
    onLogoutFinished: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var overview by remember { mutableStateOf<OverviewResponse?>(null) }
    var reviews by remember { mutableStateOf(emptyList<ReviewDto>()) }
    var supportMessages by remember { mutableStateOf(emptyList<SupportMessageDto>()) }
    var displayName by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordRepeat by rememberSaveable { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    fun loadData() {
        scope.launch {
            if (token == null) {
                loading = false
                return@launch
            }
            loading = true
            runCatching {
                overview = repository.backend.overview()
                reviews = runCatching { repository.backend.accountReviews().items }.getOrDefault(emptyList())
                supportMessages = runCatching { repository.backend.accountSupportMessages().items }.getOrDefault(emptyList())
                val user = overview?.user
                displayName = user?.display_name.orEmpty()
                username = user?.username.orEmpty()
                email = user?.email.orEmpty()
            }.onFailure {
                statusMessage = it.message
            }
            loading = false
        }
    }

    LaunchedEffect(token) {
        loadData()
    }

    if (token == null) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.AccountCircle, contentDescription = null)
            Text("Войдите, чтобы видеть библиотеку, уведомления и отзывы.", style = MaterialTheme.typography.titleMedium)
            Button(onClick = onOpenAuth) { Text("Войти") }
            OutlinedButton(onClick = onOpenSupport) { Text("Написать в поддержку") }
        }
        return
    }

    if (loading && overview == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val user = overview?.user
    val stats = overview?.stats

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF151515))) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(user?.display_name ?: user?.username ?: "Аккаунт", style = MaterialTheme.typography.headlineSmall)
                    Text(user?.email.orEmpty(), color = Color(0xFFBDBDBD))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(onClick = onOpenNotifications, label = { Text("Уведомления") }, leadingIcon = { Icon(Icons.Default.Notifications, null) })
                        AssistChip(onClick = onOpenSupport, label = { Text("Поддержка") }, leadingIcon = { Icon(Icons.Default.SupportAgent, null) })
                    }
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF151515))) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Профиль", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Имя") },
                        isError = displayName.isNotBlank() && displayName.trim().length < 2,
                        supportingText = {
                            if (displayName.isNotBlank() && displayName.trim().length < 2) Text("Минимум 2 символа")
                        }
                    )
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Логин") },
                        isError = username.isNotBlank() && username.trim().length < 3,
                        supportingText = {
                            if (username.isNotBlank() && username.trim().length < 3) Text("Минимум 3 символа")
                        }
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Email") },
                        isError = email.isNotBlank() && !isValidEmail(email),
                        supportingText = {
                            if (email.isNotBlank() && !isValidEmail(email)) Text("Некорректный email")
                        }
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Новый пароль") }
                    )
                    OutlinedTextField(
                        value = passwordRepeat,
                        onValueChange = { passwordRepeat = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Повтор пароля") }
                    )

                    val validation = profileValidation(displayName, username, email, password, passwordRepeat)
                    if (!validation.valid) {
                        Text(validation.message ?: "", color = Color(0xFFFFB3B3))
                    }

                    Button(
                        onClick = {
                            val valid = profileValidation(displayName, username, email, password, passwordRepeat)
                            if (!valid.valid) {
                                statusMessage = valid.message
                                return@Button
                            }
                            scope.launch {
                                runCatching {
                                    repository.backend.profile(
                                        ProfileUpdateRequest(
                                            display_name = displayName.trim(),
                                            username = username.trim(),
                                            email = email.trim(),
                                            password = password.takeIf { it.isNotBlank() },
                                            passwordRepeat = passwordRepeat.takeIf { it.isNotBlank() }
                                        )
                                    )
                                    statusMessage = "Профиль сохранён"
                                    password = ""
                                    passwordRepeat = ""
                                    loadData()
                                }.onFailure {
                                    statusMessage = it.message
                                }
                            }
                        },
                        enabled = profileValidation(displayName, username, email, password, passwordRepeat).valid
                    ) {
                        Text("Сохранить")
                    }
                }
            }
        }

        item { SectionTitle("Библиотека") }

        val lists = overview?.lists
        if (!lists?.favorites.isNullOrEmpty()) {
            item { LibrarySection("Избранное", lists!!.favorites, repository, onOpenMedia) }
        }
        if (!lists?.watchlist.isNullOrEmpty()) {
            item { LibrarySection("Смотреть позже", lists!!.watchlist, repository, onOpenMedia) }
        }

        item { SectionTitle("Мои отзывы") }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(horizontal = 16.dp)) {
                if (reviews.isEmpty()) {
                    Text("Пока нет отзывов.", color = Color(0xFFBDBDBD))
                } else {
                    reviews.take(8).forEach { r ->
                        ReviewCard(review = r, repository = repository)
                    }
                }
            }
        }

        item { SectionTitle("Обращения в поддержку") }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(horizontal = 16.dp)) {
                if (supportMessages.isEmpty()) {
                    Text("Пока нет обращений.", color = Color(0xFFBDBDBD))
                } else {
                    supportMessages.take(8).forEach { m ->
                        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF151515))) {
                            Column(Modifier.padding(12.dp)) {
                                Text(m.subject.orEmpty(), style = MaterialTheme.typography.titleSmall)
                                Text(m.status.orEmpty(), color = Color(0xFFBDBDBD))
                            }
                        }
                    }
                }
            }
        }

        item {
            if (statusMessage != null) {
                Text(statusMessage!!, color = Color(0xFFFFB3B3))
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onOpenNotifications) { Text("Уведомления") }
                OutlinedButton(onClick = {
                    scope.launch {
                        runCatching {
                            repository.backend.logout()
                        }
                        sessionManager.saveToken(null)
                        repository.setToken(null)
                        onLogoutFinished()
                    }
                }) { Text("Выйти") }
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = Color(0xFF151515))) {
        Column(Modifier.padding(14.dp)) {
            Text(title, color = Color(0xFFBDBDBD))
            Text(value, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
private fun LibrarySection(
    title: String,
    items: List<LibraryItemDto>,
    repository: MovieMashRepository,
    onOpenMedia: (String, Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(items.size) { index ->
                val item = items[index]
                LibraryPosterCard(
                    item = item,
                    repository = repository,
                    onOpenMedia = onOpenMedia
                )
            }
        }
    }
}

@Composable
private fun LibraryPosterCard(
    item: LibraryItemDto,
    repository: MovieMashRepository,
    onOpenMedia: (String, Int) -> Unit
) {
    val id = item.tmdb_id ?: return
    var title by remember(id, item.media_type) { mutableStateOf("Загрузка...") }
    var poster by remember(id, item.media_type) { mutableStateOf<String?>(null) }

    LaunchedEffect(id, item.media_type) {
        runCatching {
            val details = if (item.media_type == "tv") {
                repository.tmdb.seriesDetails(id, LANG)
            } else {
                repository.tmdb.movieDetails(id, LANG)
            }
            title = detailsTitle(details)
            poster = repository.imageUrl(details.poster_path ?: details.backdrop_path, "w300")
        }.onFailure {
            title = item.media_type?.let { type -> "$type #$id" } ?: "Видео #$id"
        }
    }

    Card(
        modifier = Modifier.width(132.dp).clickable { onOpenMedia(item.media_type ?: "movie", id) },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151515)),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color(0xFF222222))
            ) {
                if (poster != null) {
                    AsyncImage(
                        model = poster,
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Text(
                title,
                modifier = Modifier.padding(10.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ReviewCard(
    review: ReviewDto,
    repository: MovieMashRepository
) {
    val id = review.tmdb_id ?: 0
    var title by remember(id, review.media_type) { mutableStateOf("Загрузка...") }

    LaunchedEffect(id, review.media_type) {
        if (id == 0) {
            title = review.display_name ?: review.username ?: "Пользователь"
            return@LaunchedEffect
        }

        runCatching {
            val details = if (review.media_type == "tv") {
                repository.tmdb.seriesDetails(id, LANG)
            } else {
                repository.tmdb.movieDetails(id, LANG)
            }
            title = detailsTitle(details)
        }.onFailure {
            title = review.display_name ?: review.username ?: "Пользователь"
        }
    }

    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF151515))) {
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(review.review_text.orEmpty(), maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun NotificationsScreen(repository: MovieMashRepository) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var items by remember { mutableStateOf(emptyList<NotificationDto>()) }

    fun load() {
        scope.launch {
            loading = true
            items = runCatching { repository.backend.notifications().items }.getOrDefault(emptyList())
            loading = false
        }
    }

    LaunchedEffect(Unit) {
        load()
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Уведомления", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = {
                    scope.launch {
                        runCatching {
                            repository.backend.readAllNotifications()
                            load()
                        }
                    }
                }) { Text("Прочитать все") }
            }
        }
        if (loading) {
            item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
        }
        items(items.size) { index ->
            val n = items[index]
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF151515))) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(n.title.orEmpty(), style = MaterialTheme.typography.titleMedium)
                    Text(n.body.orEmpty())
                    Text(n.created_at.orEmpty(), color = Color(0xFFBDBDBD), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        if (items.isEmpty() && !loading) {
            item { Text("Нет уведомлений.", modifier = Modifier.padding(top = 12.dp)) }
        }
    }
}

@Composable
fun SupportScreen(repository: MovieMashRepository) {
    val scope = rememberCoroutineScope()
    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var subject by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf("") }
    var result by remember { mutableStateOf<String?>(null) }

    val validation = supportValidation(firstName, lastName, email, subject, message)

    Column(
        Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Связаться с поддержкой", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(firstName, { firstName = it }, Modifier.fillMaxWidth(), label = { Text("Имя") }, isError = firstName.isNotBlank() && firstName.trim().length < 2)
        OutlinedTextField(lastName, { lastName = it }, Modifier.fillMaxWidth(), label = { Text("Фамилия") }, isError = lastName.isNotBlank() && lastName.trim().length < 2)
        OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), label = { Text("Email") }, isError = email.isNotBlank() && !isValidEmail(email))
        OutlinedTextField(subject, { subject = it }, Modifier.fillMaxWidth(), label = { Text("Тема") }, isError = subject.isNotBlank() && subject.trim().length < 3)
        OutlinedTextField(message, { message = it }, Modifier.fillMaxWidth().height(160.dp), label = { Text("Сообщение") }, isError = message.isNotBlank() && message.trim().length < 10)

        if (!validation.valid) {
            Text(validation.message ?: "", color = Color(0xFFFFB3B3))
        }

        Button(
            onClick = {
                if (!validation.valid) {
                    result = validation.message
                    return@Button
                }
                scope.launch {
                    runCatching {
                        repository.backend.sendSupport(
                            SupportRequest(
                                firstName = firstName.trim(),
                                lastName = lastName.trim(),
                                email = email.trim(),
                                subject = subject.trim(),
                                message = message.trim()
                            )
                        )
                        result = "Сообщение отправлено"
                    }.onFailure {
                        result = it.message
                    }
                }
            },
            enabled = validation.valid
        ) {
            Icon(Icons.Default.Send, null)
            Spacer(Modifier.width(8.dp))
            Text("Отправить")
        }

        if (result != null) {
            Text(result!!, color = Color(0xFFFFB3B3))
        }
    }
}

@Composable
fun SubscriptionsScreen(onOpenAuth: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Подписки", style = MaterialTheme.typography.headlineSmall)
        SubscriptionCard("Базовый", "0 ₽", listOf("Просмотр каталога", "Поиск", "Аккаунт"))
        SubscriptionCard("Премиум", "299 ₽", listOf("Без рекламы", "Оценки и отзывы", "Уведомления"))
        SubscriptionCard("Семейный", "499 ₽", listOf("До 5 профилей", "История просмотров", "Синхронизация"))
        Button(onClick = onOpenAuth) { Text("Войти и выбрать план") }
    }
}

@Composable
private fun SubscriptionCard(title: String, price: String, features: List<String>) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF151515))) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(price, style = MaterialTheme.typography.headlineSmall)
            features.forEach { Text("• $it") }
        }
    }
}


@Composable
fun AuthHubScreen(
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onHome: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        Text("Добро пожаловать", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Text(
            "Выберите, что нужно сделать дальше: войти в аккаунт или создать новый.",
            color = Muted,
            textAlign = TextAlign.Center
        )
        Button(
            onClick = onLogin,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
            shape = RoundedCornerShape(8.dp)
        ) { Text("Войти", fontWeight = FontWeight.Bold) }
        OutlinedButton(
            onClick = onRegister,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(8.dp)
        ) { Text("Регистрация", fontWeight = FontWeight.Bold) }
        TextButton(onClick = onHome) { Text("Вернуться на главную") }
    }
}

@Composable
fun AuthScreen(
    repository: MovieMashRepository,
    sessionManager: SessionManager,
    mode: AuthMode,
    onSuccess: () -> Unit,
    onSwitchToRegister: (() -> Unit)? = null,
    onSwitchToLogin: (() -> Unit)? = null,
    onForgot: (() -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    var login by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var displayName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var repeatPassword by rememberSaveable { mutableStateOf("") }
    var result by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    val validation = if (mode == AuthMode.Login) {
        authLoginValidation(login, password)
    } else {
        authRegisterValidation(username, displayName, email, password, repeatPassword)
    }

    Column(
        Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(if (mode == AuthMode.Login) "Вход" else "Регистрация", style = MaterialTheme.typography.headlineSmall)
        Text(
            if (mode == AuthMode.Login)
                "Войдите в аккаунт, чтобы открыть избранное, отзывы и список просмотра."
            else
                "Создайте аккаунт и получите полный доступ к MovieMash.",
            color = Color(0xFFBDBDBD)
        )

        if (mode == AuthMode.Login) {
            OutlinedTextField(
                value = login,
                onValueChange = { login = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Логин или email") },
                singleLine = true,
                isError = login.isNotBlank() && login.trim().length < 3
            )
        } else {
            OutlinedTextField(value = username, onValueChange = { username = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Логин") }, singleLine = true, isError = username.isNotBlank() && username.trim().length < 3)
            OutlinedTextField(value = displayName, onValueChange = { displayName = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Имя") }, singleLine = true, isError = displayName.isNotBlank() && displayName.trim().length < 2)
            OutlinedTextField(value = email, onValueChange = { email = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Email") }, singleLine = true, isError = email.isNotBlank() && !isValidEmail(email))
        }

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Пароль") },
            singleLine = true,
            isError = password.isNotBlank() && password.length < 6
        )

        if (mode == AuthMode.Register) {
            OutlinedTextField(
                value = repeatPassword,
                onValueChange = { repeatPassword = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Повтор пароля") },
                singleLine = true,
                isError = repeatPassword.isNotBlank() && repeatPassword != password
            )
        }

        if (!validation.valid) {
            Text(validation.message ?: "", color = Color(0xFFFFB3B3))
        }

        Button(
            onClick = {
                if (!validation.valid) {
                    result = validation.message
                    return@Button
                }
                scope.launch {
                    loading = true
                    runCatching {
                        val auth = if (mode == AuthMode.Login) {
                            repository.backend.login(LoginRequest(login.trim(), password))
                        } else {
                            repository.backend.register(
                                RegisterRequest(
                                    username = username.trim(),
                                    email = email.trim(),
                                    password = password,
                                    display_name = displayName.trim()
                                )
                            )
                        }
                        val token = auth.token ?: throw IllegalStateException("Токен не получен")
                        sessionManager.saveToken(token)
                        repository.setToken(token)
                        result = "Успешно"
                        onSuccess()
                    }.onFailure {
                        result = it.message
                    }
                    loading = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = validation.valid && !loading
        ) {
            Text(
                when {
                    loading && mode == AuthMode.Login -> "Вход..."
                    loading -> "Создание..."
                    mode == AuthMode.Login -> "Войти"
                    else -> "Зарегистрироваться"
                }
            )
        }

        if (mode == AuthMode.Login) {
            TextButton(onClick = { onForgot?.invoke() }) { Text("Забыли пароль?") }
            TextButton(onClick = { onSwitchToRegister?.invoke() }) { Text("Нет аккаунта? Регистрация") }
        } else {
            TextButton(onClick = { onSwitchToLogin?.invoke() }) { Text("Уже есть аккаунт? Войти") }
        }

        if (result != null) {
            Text(result!!, color = Color(0xFFFFB3B3))
        }
    }
}

@Composable
fun ForgotPasswordScreen(repository: MovieMashRepository, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var email by rememberSaveable { mutableStateOf("") }
    var result by remember { mutableStateOf<String?>(null) }
    val valid = isValidEmail(email)

    Column(
        Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Восстановление пароля", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
            singleLine = true,
            isError = email.isNotBlank() && !valid
        )
        if (email.isNotBlank() && !valid) {
            Text("Некорректный email", color = Color(0xFFFFB3B3))
        }
        Button(
            onClick = {
                if (!valid) {
                    result = "Некорректный email"
                    return@Button
                }
                scope.launch {
                    runCatching {
                        val res = repository.backend.forgotPassword(ForgotPasswordRequest(email.trim()))
                        result = res.message ?: "Ссылка отправлена"
                    }.onFailure {
                        result = it.message
                    }
                }
            },
            enabled = valid
        ) { Text("Отправить ссылку") }
        TextButton(onClick = onBack) { Text("Назад") }
        if (result != null) Text(result!!, color = Color(0xFFFFB3B3))
    }
}

@Composable
fun ResetPasswordScreen(repository: MovieMashRepository, token: String, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var password by rememberSaveable { mutableStateOf("") }
    var repeat by rememberSaveable { mutableStateOf("") }
    var result by remember { mutableStateOf<String?>(null) }
    val valid = password.length >= 6 && password == repeat

    Column(
        Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Сброс пароля", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(password, { password = it }, Modifier.fillMaxWidth(), label = { Text("Новый пароль") }, singleLine = true, isError = password.isNotBlank() && password.length < 6)
        OutlinedTextField(repeat, { repeat = it }, Modifier.fillMaxWidth(), label = { Text("Повтор") }, singleLine = true, isError = repeat.isNotBlank() && repeat != password)

        if (password.isNotBlank() && password.length < 6) Text("Пароль минимум 6 символов", color = Color(0xFFFFB3B3))
        if (repeat.isNotBlank() && repeat != password) Text("Пароли не совпадают", color = Color(0xFFFFB3B3))

        Button(
            onClick = {
                if (!valid) {
                    result = "Проверьте пароль"
                    return@Button
                }
                scope.launch {
                    runCatching {
                        repository.backend.resetPassword(ResetPasswordRequest(token, password, repeat))
                        result = "Пароль изменён"
                    }.onFailure {
                        result = it.message
                    }
                }
            },
            enabled = valid
        ) { Text("Сменить пароль") }

        TextButton(onClick = onBack) { Text("Назад") }
        if (result != null) Text(result!!, color = Color(0xFFFFB3B3))
    }
}
