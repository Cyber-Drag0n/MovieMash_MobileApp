package com.moviemash.app.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.moviemash.app.data.MovieMashRepository
import com.moviemash.app.data.SessionManager
import kotlinx.coroutines.launch

private data class DrawerItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieMashApp() {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val repository = remember { MovieMashRepository() }
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val token by sessionManager.tokenFlow.collectAsState(initial = null)
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route ?: "home"

    LaunchedEffect(token) {
        repository.setToken(token)
    }

    val drawerItems = remember(token) {
        buildList {
            add(DrawerItem("home", "Главная", Icons.Default.Home))
            add(DrawerItem("browse", "Поиск", Icons.Default.Search))
            if (token == null) {
                add(DrawerItem("auth", "Вход / Регистрация", Icons.Default.AccountCircle))
            } else {
                add(DrawerItem("account", "Аккаунт", Icons.Default.AccountCircle))
                add(DrawerItem("notifications", "Уведомления", Icons.Default.Notifications))
            }
            add(DrawerItem("support", "Поддержка", Icons.Default.SupportAgent))
            add(DrawerItem("subscriptions", "Подписки", Icons.Default.Subscriptions))
        }
    }

    fun openRoute(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerSheet(
                currentRoute = currentRoute,
                token = token,
                items = drawerItems,
                onNavigate = { route ->
                    scope.launch { drawerState.close() }
                    openRoute(route)
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                MovieMashTopBar(
                    onMenu = {
                        scope.launch {
                            if (drawerState.isClosed) drawerState.open() else drawerState.close()
                        }
                    }
                )
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(padding)
            ) {
                composable("home") {
                    HomeScreen(
                        repository = repository,
                        onOpenMedia = { type, id -> openRoute("movie/$type/$id") },
                        onOpenBrowse = { openRoute("browse") },
                        onOpenSupport = { openRoute("support") },
                        onOpenSubscriptions = { openRoute("subscriptions") },
                        onOpenAuth = { openRoute("auth") }
                    )
                }

                composable("browse") {
                    BrowseScreen(
                        repository = repository,
                        onOpenMedia = { type, id -> openRoute("movie/$type/$id") }
                    )
                }

                composable("account") {
                    AccountScreen(
                        repository = repository,
                        sessionManager = sessionManager,
                        token = token,
                        onOpenMedia = { type, id -> openRoute("movie/$type/$id") },
                        onOpenAuth = { openRoute("auth") },
                        onOpenSupport = { openRoute("support") },
                        onOpenNotifications = { openRoute("notifications") },
                        onLogoutFinished = {
                            navController.navigate("home") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable("support") {
                    SupportScreen(repository = repository)
                }

                composable("subscriptions") {
                    SubscriptionsScreen(onOpenAuth = { openRoute("auth") })
                }

                composable("notifications") {
                    NotificationsScreen(repository = repository)
                }

                composable("auth") {
                    AuthHubScreen(
                        onLogin = { openRoute("auth/login") },
                        onRegister = { openRoute("auth/register") },
                        onHome = { openRoute("home") }
                    )
                }

                composable(
                    route = "movie/{mediaType}/{tmdbId}",
                    arguments = listOf(
                        navArgument("mediaType") { type = NavType.StringType },
                        navArgument("tmdbId") { type = NavType.IntType }
                    )
                ) { backStack ->
                    val mediaType = backStack.arguments?.getString("mediaType") ?: "movie"
                    val tmdbId = backStack.arguments?.getInt("tmdbId") ?: 0
                    MovieScreen(
                        repository = repository,
                        token = token,
                        mediaType = mediaType,
                        tmdbId = tmdbId,
                        onOpenAuth = { openRoute("auth") }
                    )
                }

                composable("auth/login") {
                    AuthScreen(
                        repository = repository,
                        sessionManager = sessionManager,
                        mode = AuthMode.Login,
                        onSuccess = {
                            navController.navigate("account") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                            }
                        },
                        onSwitchToRegister = { openRoute("auth/register") },
                        onForgot = { openRoute("auth/forgot") }
                    )
                }

                composable("auth/register") {
                    AuthScreen(
                        repository = repository,
                        sessionManager = sessionManager,
                        mode = AuthMode.Register,
                        onSuccess = {
                            navController.navigate("account") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                            }
                        },
                        onSwitchToLogin = { openRoute("auth/login") }
                    )
                }

                composable("auth/forgot") {
                    ForgotPasswordScreen(
                        repository = repository,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = "auth/reset/{token}",
                    arguments = listOf(navArgument("token") { type = NavType.StringType })
                ) { backStack ->
                    val tokenArg = Uri.decode(backStack.arguments?.getString("token").orEmpty())
                    ResetPasswordScreen(
                        repository = repository,
                        token = tokenArg,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@Composable
private fun MovieMashTopBar(onMenu: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
            .background(AppBg)
            .padding(start = 24.dp, end = 14.dp, top = 34.dp, bottom = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(24.dp)
                    .background(BrandRed, RoundedCornerShape(2.dp))
            )
            Spacer(Modifier.width(6.dp))
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(21.dp))
            Spacer(Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(24.dp)
                    .background(BrandRed, RoundedCornerShape(2.dp))
            )
            Spacer(Modifier.width(7.dp))
            Text("MovieMash", color = Color.White, fontWeight = FontWeight.ExtraBold)
        }
        IconButton(
            onClick = onMenu,
            modifier = Modifier
                .size(54.dp)
                .border(2.dp, Stroke, RoundedCornerShape(11.dp))
                .background(CardBg, RoundedCornerShape(11.dp))
        ) {
            Icon(Icons.Default.Menu, contentDescription = "Меню", tint = Color.White, modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
private fun DrawerSheet(
    currentRoute: String,
    token: String?,
    items: List<DrawerItem>,
    onNavigate: (String) -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = CardBg,
        drawerContentColor = Color.White
    ) {
        DrawerHeader(token = token)
        items.forEach { item ->
            NavigationDrawerItem(
                label = { Text(item.label) },
                selected = currentRoute.startsWith(item.route),
                onClick = { onNavigate(item.route) },
                icon = { Icon(item.icon, contentDescription = null) },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = BrandRed,
                    unselectedContainerColor = Color.Transparent,
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    unselectedIconColor = Color(0xFFEDEDED),
                    unselectedTextColor = Color(0xFFEDEDED)
                )
            )
        }
    }
}

@Composable
private fun DrawerHeader(token: String?) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .padding(16.dp)
            .background(
                brush = Brush.linearGradient(
                    listOf(BrandRed, Color(0xFF3A0909), AppBg)
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(18.dp)
    ) {
        androidx.compose.foundation.layout.Column {
            Text(
                "MovieMash",
                color = Color.White,
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
            )
            Text(
                if (token == null) "Войдите, чтобы открыть аккаунт"
                else "Аккаунт подключён",
                color = Color(0xFFECECEC),
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
            )
        }
    }
}