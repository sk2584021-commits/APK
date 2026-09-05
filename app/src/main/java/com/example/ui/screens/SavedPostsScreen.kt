package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.VyroViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedPostsScreen(viewModel: VyroViewModel, onNavigateBack: () -> Unit, onUserClick: (Int) -> Unit) {
    val savedPosts by viewModel.savedPosts.collectAsState(initial = emptyList())
    val userLikes by viewModel.userLikes.collectAsState(initial = emptySet())
    val userFollowing by viewModel.userFollowing.collectAsState(initial = emptySet())
    val currentUser by viewModel.currentUser.collectAsState()
    val savedPostIds by viewModel.savedPostIds.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Posts") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (savedPosts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No saved posts yet.", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(savedPosts) { postWithUser ->
                    PostCard(
                        postWithUser = postWithUser,
                        isLiked = userLikes.contains(postWithUser.post.id),
                        isFollowing = userFollowing.contains(postWithUser.user.id),
                        isOwnPost = currentUser?.id == postWithUser.user.id,
                        isSaved = savedPostIds.contains(postWithUser.post.id),
                        onLike = { viewModel.toggleLike(postWithUser.post.id, postWithUser.user.id) },
                        onFollow = { viewModel.toggleFollow(postWithUser.user.id) },
                        onDelete = { viewModel.deletePost(postWithUser.post.id) },
                        onSave = { viewModel.toggleSavePost(postWithUser.post.id) },
                        onReport = { reason -> viewModel.submitReport(postWithUser.user.id, "POST", reason) },
                        onUserClick = { onUserClick(postWithUser.user.id) },
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
