package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.VyroViewModel

@Composable
fun SearchScreen(viewModel: VyroViewModel, onUserClick: (Int) -> Unit = {}) {
    val query by viewModel.searchQuery.collectAsState()
    val userResults by viewModel.searchResultsUsers.collectAsState(initial = emptyList())
    val postResults by viewModel.searchResultsPosts.collectAsState(initial = emptyList())
    
    val userLikes by viewModel.userLikes.collectAsState(initial = emptySet())
    val userFollowing by viewModel.userFollowing.collectAsState(initial = emptySet())
    val currentUser by viewModel.currentUser.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text("Search users or hashtags...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(12.dp)
        )
        
        if (query.isBlank()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Trending hashtags will appear here", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (userResults.isNotEmpty()) {
                    item {
                        Text("Users", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(top = 8.dp))
                    }
                    items(userResults) { user ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().clickable { onUserClick(user.id) }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(40.dp).clip(androidx.compose.foundation.shape.CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!user.profilePicUrl.isNullOrEmpty()) {
                                        coil.compose.AsyncImage(
                                            model = user.profilePicUrl,
                                            contentDescription = "Profile Picture",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                    } else {
                                        Text(user.username.firstOrNull()?.uppercase() ?: "U", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(user.username, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                if (currentUser?.id != user.id) {
                                    val isFollowing = userFollowing.contains(user.id)
                                    TextButton(onClick = { viewModel.toggleFollow(user.id) }) {
                                        Text(if (isFollowing) "Following" else "Follow")
                                    }
                                }
                            }
                        }
                    }
                }
                
                if (postResults.isNotEmpty()) {
                    item {
                        Text("Posts", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(top = 16.dp))
                    }
                    items(postResults) { postWithUser ->
                        val savedPostIds by viewModel.savedPostIds.collectAsState(initial = emptyList())
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
                
                if (userResults.isEmpty() && postResults.isEmpty()) {
                    item {
                        Text("No results found.", modifier = Modifier.padding(16.dp))
                    }
                }
            }
        }
    }
}
