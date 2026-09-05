package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.PostWithUser
import com.example.ui.VyroViewModel

@Composable
fun HomeScreen(viewModel: VyroViewModel, onUserClick: (Int) -> Unit = {}) {
    val feedPosts by viewModel.feedPosts.collectAsState(initial = emptyList())
    val userLikes by viewModel.userLikes.collectAsState(initial = emptySet())
    val userFollowing by viewModel.userFollowing.collectAsState(initial = emptySet())
    val currentUser by viewModel.currentUser.collectAsState()
    val savedPostIds by viewModel.savedPostIds.collectAsState(initial = emptyList())
    val blockedUsers by viewModel.blockedUsers.collectAsState(initial = emptyList())
    val usersWhoBlocked by viewModel.usersWhoBlocked.collectAsState(initial = emptyList())

    val safePosts = feedPosts.filter {
        !blockedUsers.contains(it.user.id) && !usersWhoBlocked.contains(it.user.id)
    }

    if (safePosts.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(16.dp))
                Text("Your feed is empty", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Follow some users to see their posts here", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "VYRO Feed",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(safePosts) { postWithUser ->
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCard(
    postWithUser: PostWithUser,
    isLiked: Boolean,
    isFollowing: Boolean,
    isOwnPost: Boolean,
    isSaved: Boolean = false,
    onLike: () -> Unit,
    onFollow: () -> Unit,
    onDelete: () -> Unit,
    onSave: () -> Unit = {},
    onReport: (String) -> Unit = {},
    onUserClick: () -> Unit = {},
    viewModel: VyroViewModel
) {
    var showComments by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showReportDialog) {
        var reportReason by remember { mutableStateOf("") }
        val reasons = listOf("Spam", "Harassment", "Inappropriate content", "Other")
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report Post") },
            text = {
                Column {
                    reasons.forEach { reason ->
                        Row(
                            Modifier.fillMaxWidth().clickable { reportReason = reason }.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = reportReason == reason, onClick = { reportReason = reason })
                            Spacer(Modifier.width(8.dp))
                            Text(reason)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (reportReason.isNotBlank()) {
                        onReport(reportReason)
                        showReportDialog = false
                    }
                }) { Text("Submit") }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) { Text("Cancel") }
            }
        )
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        .clickable(onClick = onUserClick),
                    contentAlignment = Alignment.Center
                ) {
                    if (!postWithUser.user.profilePicUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = postWithUser.user.profilePicUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            postWithUser.user.username.firstOrNull()?.uppercase() ?: "U",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f).clickable(onClick = onUserClick)) {
                    Text(postWithUser.user.username, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text(
                        android.text.format.DateUtils.getRelativeTimeSpanString(postWithUser.post.timestamp).toString(),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (!isOwnPost && !isFollowing) {
                    TextButton(onClick = onFollow) {
                        Text("Follow")
                    }
                }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        if (isOwnPost) {
                            DropdownMenuItem(text = { Text("Delete") }, onClick = { onDelete(); showMenu = false })
                        } else {
                            DropdownMenuItem(text = { Text("Report") }, onClick = { showReportDialog = true; showMenu = false })
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (!postWithUser.post.imageUrl.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(
                        model = postWithUser.post.imageUrl,
                        contentDescription = "Post image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    if (postWithUser.post.isVideo) {
                        Box(modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(48.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            Text(postWithUser.post.caption, fontSize = 15.sp)
            if (postWithUser.post.hashtags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(postWithUser.post.hashtags, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onLike, modifier = Modifier.testTag("like_button_${postWithUser.post.id}")) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                    )
                }
                Text("${postWithUser.post.likesCount}", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.width(16.dp))
                IconButton(onClick = { showComments = true }) {
                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Comment")
                }
                Text("${postWithUser.post.commentsCount}", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = {
                    val shareIntent = android.content.Intent().apply {
                        action = android.content.Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_TEXT, "Check out this post from ${postWithUser.user.username} on VYRO: ${postWithUser.post.caption}")
                    }
                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Share post"))
                }) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                }
                IconButton(onClick = onSave) {
                    Icon(
                        if (isSaved) androidx.compose.material.icons.Icons.Default.Bookmark else androidx.compose.material.icons.Icons.Default.BookmarkBorder,
                        contentDescription = "Save"
                    )
                }
            }
            
            if (showComments) {
                ModalBottomSheet(
                    onDismissRequest = { showComments = false },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
                ) {
                    CommentsSection(postId = postWithUser.post.id, postOwnerId = postWithUser.user.id, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun CommentsSection(postId: Int, postOwnerId: Int, viewModel: VyroViewModel) {
    val comments by viewModel.getCommentsForPost(postId).collectAsState(initial = emptyList())
    var commentText by remember { mutableStateOf("") }
    val currentUser by viewModel.currentUser.collectAsState()

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text("Comments", fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(bottom = 16.dp))
        
        LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
            items(comments) { commentWithUser ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!commentWithUser.profilePicUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = commentWithUser.profilePicUrl,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(commentWithUser.username.firstOrNull()?.uppercase() ?: "U", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(commentWithUser.username, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                android.text.format.DateUtils.getRelativeTimeSpanString(commentWithUser.comment.timestamp).toString(),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(commentWithUser.comment.text, fontSize = 14.sp)
                    }
                    if (currentUser?.id == commentWithUser.comment.userId) {
                        IconButton(onClick = { viewModel.deleteComment(commentWithUser.comment.id, postId) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Comment", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
            if (comments.isEmpty()) {
                item {
                    Text("No comments yet. Be the first!", modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                placeholder = { Text("Add a comment...", fontSize = 14.sp) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (commentText.isNotBlank()) {
                        viewModel.addComment(postId, postOwnerId, commentText)
                        commentText = ""
                    }
                },
                modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Post", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}
