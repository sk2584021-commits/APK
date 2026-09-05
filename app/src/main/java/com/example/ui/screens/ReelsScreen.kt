package com.example.ui.screens

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.data.PostWithUser
import com.example.ui.VyroViewModel

@Composable
fun ReelsScreen(viewModel: VyroViewModel, onUserClick: (Int) -> Unit = {}) {
    val videoPosts by viewModel.videoPosts.collectAsState(initial = emptyList())
    val userLikes by viewModel.userLikes.collectAsState(initial = emptySet())
    val userFollowing by viewModel.userFollowing.collectAsState(initial = emptySet())
    val currentUser by viewModel.currentUser.collectAsState()
    val savedPostIds by viewModel.savedPostIds.collectAsState(initial = emptyList())
    val blockedUsers by viewModel.blockedUsers.collectAsState(initial = emptyList())
    val usersWhoBlocked by viewModel.usersWhoBlocked.collectAsState(initial = emptyList())
    val safeVideos = videoPosts.filter { !blockedUsers.contains(it.user.id) && !usersWhoBlocked.contains(it.user.id) }

    if (safeVideos.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Text("No Reels available yet.", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { safeVideos.size })

    VerticalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize().background(Color.Black)
    ) { page ->
        val postWithUser = safeVideos[page]
        val isLiked = userLikes.contains(postWithUser.post.id)
        val isFollowing = userFollowing.contains(postWithUser.user.id)
        val isOwnPost = currentUser?.id == postWithUser.user.id
        val isSaved = savedPostIds.contains(postWithUser.post.id)

        ReelItem(
            postWithUser = postWithUser,
            isLiked = isLiked,
            isFollowing = isFollowing,
            isOwnPost = isOwnPost,
            isActive = pagerState.currentPage == page,
            isSaved = isSaved,
            onLike = { viewModel.toggleLike(postWithUser.post.id, postWithUser.user.id) },
            onFollow = { viewModel.toggleFollow(postWithUser.user.id) },
            onUserClick = { onUserClick(postWithUser.user.id) },
            onDelete = { viewModel.deletePost(postWithUser.post.id) },
            onSave = { viewModel.toggleSavePost(postWithUser.post.id) },
            onReport = { reason -> viewModel.submitReport(postWithUser.user.id, "VIDEO", reason) },
            viewModel = viewModel
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReelItem(
    postWithUser: PostWithUser,
    isLiked: Boolean,
    isFollowing: Boolean,
    isOwnPost: Boolean,
    isActive: Boolean,
    isSaved: Boolean,
    onLike: () -> Unit,
    onFollow: () -> Unit,
    onUserClick: () -> Unit,
    onDelete: () -> Unit,
    onSave: () -> Unit,
    onReport: (String) -> Unit,
    viewModel: VyroViewModel
) {
    var isMuted by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }
    var showComments by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showReportDialog) {
        var reportReason by remember { mutableStateOf("") }
        val reasons = listOf("Spam", "Harassment", "Inappropriate content", "Other")
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report Video") },
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

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        VideoPlayer(
            videoUrl = postWithUser.post.imageUrl ?: "",
            isActive = isActive && isPlaying,
            isMuted = isMuted,
            modifier = Modifier
                .fillMaxSize()
                .clickable { isPlaying = !isPlaying }
        )

        // Overlay actions
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Like
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onLike) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) Color.Red else Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Text("${postWithUser.post.likesCount}", color = Color.White, fontWeight = FontWeight.Bold)
            }

            // Comment
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { showComments = true }) {
                    Icon(Icons.Default.Comment, contentDescription = "Comment", tint = Color.White, modifier = Modifier.size(32.dp))
                }
                Text("${postWithUser.post.commentsCount}", color = Color.White, fontWeight = FontWeight.Bold)
            }
            
            // Share
            IconButton(onClick = {
                val shareIntent = android.content.Intent().apply {
                    action = android.content.Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(android.content.Intent.EXTRA_TEXT, "Check out this reel from @${postWithUser.user.username} on VYRO: ${postWithUser.post.caption}")
                }
                context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Reel"))
            }) {
                Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(32.dp))
            }

            // Save
            IconButton(onClick = onSave) {
                Icon(
                    imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Save",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Options
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.White, modifier = Modifier.size(32.dp))
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    if (isOwnPost) {
                        DropdownMenuItem(text = { Text("Delete") }, onClick = { onDelete(); showMenu = false })
                    } else {
                        DropdownMenuItem(text = { Text("Report") }, onClick = { showReportDialog = true; showMenu = false })
                    }
                }
            }
            
            // Mute Toggle
            IconButton(onClick = { isMuted = !isMuted }) {
                Icon(
                    imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                    contentDescription = "Mute",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Creator Info and Caption
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .padding(end = 64.dp) // space for actions
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable(onClick = onUserClick),
                    contentAlignment = Alignment.Center
                ) {
                    if (!postWithUser.user.profilePicUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = postWithUser.user.profilePicUrl,
                            contentDescription = "Profile",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(postWithUser.user.username.firstOrNull()?.uppercase() ?: "U", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("@${postWithUser.user.username}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.clickable(onClick = onUserClick))
                
                if (!isOwnPost && !isFollowing) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = onFollow,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(32.dp).background(Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Text("Follow", fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (postWithUser.post.caption.isNotBlank()) {
                Text(postWithUser.post.caption, color = Color.White, fontSize = 14.sp)
            }
            if (postWithUser.post.hashtags.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(postWithUser.post.hashtags, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
        
        if (showComments) {
            @OptIn(ExperimentalMaterial3Api::class)
            ModalBottomSheet(
                onDismissRequest = { showComments = false }
            ) {
                com.example.ui.screens.CommentsSection(
                    postId = postWithUser.post.id,
                    postOwnerId = postWithUser.user.id,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun VideoPlayer(
    videoUrl: String,
    isActive: Boolean,
    isMuted: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
        }
    }

    LaunchedEffect(videoUrl) {
        val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
    }

    LaunchedEffect(isActive) {
        if (isActive) {
            exoPlayer.play()
        } else {
            exoPlayer.pause()
        }
    }

    LaunchedEffect(isMuted) {
        exoPlayer.volume = if (isMuted) 0f else 1f
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = {
            PlayerView(context).apply {
                player = exoPlayer
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        modifier = modifier
    )
}
