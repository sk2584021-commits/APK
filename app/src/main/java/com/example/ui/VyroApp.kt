package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.PlayArrow
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.VyroDatabase
import com.example.data.VyroRepository
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CreatePostScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SearchScreen

@Composable
fun VyroApp() {
    val context = LocalContext.current
    val database = VyroDatabase.getDatabase(context)
    val repository = VyroRepository(database.vyroDao())
    val viewModel: VyroViewModel = viewModel(factory = VyroViewModelFactory(repository))
    
    val currentUser by viewModel.currentUser.collectAsState()
    
    if (currentUser == null) {
        AuthScreen(
            onLogin = { username -> viewModel.login(username) }
        )
    } else {
        MainScreen(viewModel)
    }
}

@Composable
fun MainScreen(viewModel: VyroViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                val items = listOf(
                    "home" to Icons.Default.Home,
                    "search" to Icons.Default.Search,
                    "create" to Icons.Default.Add,
                    "reels" to Icons.Default.PlayArrow,
                    "messages" to Icons.Default.Email,
                    "notifications" to Icons.Default.Notifications,
                    "profile" to Icons.Default.Person
                )
                items.forEach { (route, icon) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = route) },
                        selected = currentRoute == route,
                        onClick = {
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding).fillMaxSize().background(MaterialTheme.colorScheme.background)
        ) {
            composable("home") { HomeScreen(viewModel, onUserClick = { navController.navigate("profile/$it") }) }
            composable("search") { SearchScreen(viewModel, onUserClick = { navController.navigate("profile/$it") }) }
            composable("create") { 
                CreatePostScreen(
                    onPostCreated = { caption, hashtags, imageUrl, isVideo ->
                        viewModel.createPost(caption, hashtags, imageUrl, isVideo)
                        if (isVideo) {
                            navController.navigate("reels") {
                                popUpTo("home") { inclusive = false }
                            }
                        } else {
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    }
                ) 
            }
            composable("reels") { com.example.ui.screens.ReelsScreen(viewModel, onUserClick = { navController.navigate("profile/$it") }) }
            composable("messages") {
                com.example.ui.screens.ChatListScreen(
                    viewModel = viewModel,
                    onConversationClick = { conversationId, otherUserId ->
                        navController.navigate("chat/$conversationId/$otherUserId")
                    }
                )
            }
            composable(
                "chat/{conversationId}/{otherUserId}",
                arguments = listOf(
                    androidx.navigation.navArgument("conversationId") { type = androidx.navigation.NavType.IntType },
                    androidx.navigation.navArgument("otherUserId") { type = androidx.navigation.NavType.IntType }
                )
            ) { backStackEntry ->
                val conversationId = backStackEntry.arguments?.getInt("conversationId") ?: return@composable
                val otherUserId = backStackEntry.arguments?.getInt("otherUserId") ?: return@composable
                com.example.ui.screens.ChatScreen(
                    conversationId = conversationId,
                    otherUserId = otherUserId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("notifications") { 
                val notifications by viewModel.notifications.collectAsState(initial = emptyList())
                if (notifications.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(16.dp))
                            Text("No new notifications", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        }
                    } 
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Notifications", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            TextButton(onClick = { viewModel.markAllNotificationsAsRead() }) {
                                Text("Mark all as read")
                            }
                        }
                        androidx.compose.foundation.lazy.LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(notifications.size) { index ->
                                val notification = notifications[index]
                                androidx.compose.material3.Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = androidx.compose.material3.CardDefaults.cardColors(
                                        containerColor = if (notification.notification.isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    androidx.compose.foundation.layout.Row(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp).clickable {
                                            viewModel.markNotificationAsRead(notification.notification.id)
                                            navController.navigate("profile/${notification.notification.senderId}")
                                        }, 
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (!notification.profilePicUrl.isNullOrEmpty()) {
                                            coil.compose.AsyncImage(
                                                model = notification.profilePicUrl,
                                                contentDescription = "Profile Picture",
                                                modifier = Modifier.size(40.dp).clip(androidx.compose.foundation.shape.CircleShape),
                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                            )
                                        } else {
                                            Box(modifier = Modifier.size(40.dp).clip(androidx.compose.foundation.shape.CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                                                Text(notification.username.firstOrNull()?.uppercase() ?: "U", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            androidx.compose.material3.Text(
                                                "${notification.username} ${notification.notification.text}",
                                                fontWeight = if (notification.notification.isRead) FontWeight.Normal else FontWeight.Bold
                                            )
                                            val sdf = java.text.SimpleDateFormat("MMM d, h:mm a", java.util.Locale.getDefault())
                                            Text(sdf.format(java.util.Date(notification.notification.timestamp)), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        if (!notification.notification.isRead) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(modifier = Modifier.size(8.dp).clip(androidx.compose.foundation.shape.CircleShape).background(MaterialTheme.colorScheme.primary))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            composable("settings") {
                com.example.ui.screens.SettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToSavedPosts = { navController.navigate("saved_posts") }
                )
            }
            composable("saved_posts") {
                com.example.ui.screens.SavedPostsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onUserClick = { userId -> navController.navigate("profile/$userId") }
                )
            }
            composable("profile") { 
                ProfileScreen(
                    viewModel = viewModel,
                    onSettingsClick = { navController.navigate("settings") }
                ) 
            }
            composable(
                "profile/{userId}",
                arguments = listOf(androidx.navigation.navArgument("userId") { type = androidx.navigation.NavType.IntType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getInt("userId")
                val scope = androidx.compose.runtime.rememberCoroutineScope()
                ProfileScreen(
                    viewModel = viewModel, 
                    userId = userId, 
                    onNavigateBack = { navController.popBackStack() },
                    onMessageClick = { otherId ->
                        scope.launch {
                            val conversationId = viewModel.getOrCreateConversation(otherId)
                            if (conversationId != -1) {
                                navController.navigate("chat/$conversationId/$otherId")
                            }
                        }
                    }
                )
            }
        }
    }
}
