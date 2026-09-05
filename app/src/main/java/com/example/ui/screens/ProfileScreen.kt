package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.VyroViewModel

import androidx.compose.material.icons.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: VyroViewModel,
    userId: Int? = null,
    onNavigateBack: (() -> Unit)? = null,
    onMessageClick: ((Int) -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val profileUser by (if (userId != null) viewModel.getProfileUser(userId) else viewModel.currentUser).collectAsState(initial = null)
    val posts by (if (userId != null) viewModel.getPostsForUser(userId) else viewModel.myPosts).collectAsState(initial = emptyList())
    val userFollowing by viewModel.userFollowing.collectAsState(initial = emptySet())
    
    var showEditProfile by remember { mutableStateOf(false) }

    if (showEditProfile && profileUser != null && profileUser?.id == currentUser?.id) {
        var username by remember { mutableStateOf(profileUser!!.username) }
        var bio by remember { mutableStateOf(profileUser!!.bio) }
        var selectedImageUri by remember { mutableStateOf<Uri?>(if (profileUser!!.profilePicUrl != null) Uri.parse(profileUser!!.profilePicUrl) else null) }
        
        val photoPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
            onResult = { uri -> if (uri != null) selectedImageUri = uri }
        )
        
        AlertDialog(
            onDismissRequest = { showEditProfile = false },
            title = { Text("Edit Profile") },
            text = {
                Column {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable {
                                    photoPickerLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedImageUri != null) {
                                AsyncImage(
                                    model = selectedImageUri,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text("Add Photo", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Bio") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateProfile(username, bio, selectedImageUri?.toString())
                    showEditProfile = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfile = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (profileUser != null) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                if (onNavigateBack != null) {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.padding(bottom = 8.dp).offset(x = (-12).dp)) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!profileUser!!.profilePicUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = profileUser!!.profilePicUrl,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    profileUser!!.username.firstOrNull()?.uppercase() ?: "U",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(profileUser!!.fullName.ifBlank { profileUser!!.username }, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Text("@${profileUser!!.username}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                            if (profileUser!!.bio.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(profileUser!!.bio, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Followers: ${profileUser!!.followersCount} | Following: ${profileUser!!.followingCount}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        if (profileUser!!.id == currentUser?.id) {
                            IconButton(onClick = { showEditProfile = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                            }
                            if (onSettingsClick != null) {
                                IconButton(onClick = onSettingsClick) {
                                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                                }
                            }
                            IconButton(onClick = { viewModel.logout() }) {
                                Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                            }
                        } else {
                            val isFollowing = userFollowing.contains(profileUser!!.id)
                            val blockedUsers by viewModel.blockedUsers.collectAsState(initial = emptyList())
                            val isBlocked = blockedUsers.contains(profileUser!!.id)
                            var showMenu by remember { mutableStateOf(false) }
                            var showReportDialog by remember { mutableStateOf(false) }

                            if (showReportDialog) {
                                var reportReason by remember { mutableStateOf("") }
                                val reasons = listOf("Spam", "Harassment", "Fake account", "Other")
                                AlertDialog(
                                    onDismissRequest = { showReportDialog = false },
                                    title = { Text("Report User") },
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
                                                viewModel.submitReport(profileUser!!.id, "USER", reportReason)
                                                showReportDialog = false
                                            }
                                        }) { Text("Submit") }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showReportDialog = false }) { Text("Cancel") }
                                    }
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { viewModel.toggleFollow(profileUser!!.id) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isFollowing) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                                            contentColor = if (isFollowing) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
                                        ),
                                        modifier = Modifier.fillMaxWidth(0.4f)
                                    ) {
                                        Text(if (isFollowing) "Following" else "Follow")
                                    }
                                    
                                    OutlinedButton(
                                        onClick = { onMessageClick?.invoke(profileUser!!.id) },
                                        modifier = Modifier.fillMaxWidth(0.4f)
                                    ) {
                                        Text("Message")
                                    }
                                }
                                Box {
                                    IconButton(onClick = { showMenu = true }) {
                                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                                    }
                                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                        DropdownMenuItem(
                                            text = { Text(if (isBlocked) "Unblock User" else "Block User") },
                                            onClick = { viewModel.toggleBlock(profileUser!!.id); showMenu = false }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Report User") },
                                            onClick = { showReportDialog = true; showMenu = false }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            Divider()
            
            if (posts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        Spacer(Modifier.height(16.dp))
                        Text("No posts yet", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(2.dp)
                ) {
                    items(posts) { postWithUser ->
                    Surface(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .padding(2.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (!postWithUser.post.imageUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = postWithUser.post.imageUrl,
                                contentDescription = "Post image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            if (postWithUser.post.isVideo) {
                                Box(modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(32.dp))
                                }
                            }
                        } else {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(8.dp).fillMaxSize()) {
                                Text(postWithUser.post.caption, maxLines = 4, fontSize = 11.sp)
                            }
                        }
                        if (currentUser?.id == profileUser!!.id) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopEnd) {
                                IconButton(
                                    onClick = { viewModel.deletePost(postWithUser.post.id) },
                                    modifier = Modifier.size(24.dp).padding(4.dp).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}
