package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.VyroViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversationId: Int,
    otherUserId: Int,
    viewModel: VyroViewModel,
    onBack: () -> Unit
) {
    val messages by viewModel.getMessagesForConversation(conversationId).collectAsState(initial = emptyList())
    val otherUser by viewModel.getProfileUser(otherUserId).collectAsState(initial = null)
    val currentUser by viewModel.currentUser.collectAsState()
    
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Mark as read when opened
    LaunchedEffect(messages) {
        viewModel.markMessagesAsRead(conversationId)
        if (messages.isNotEmpty()) {
            listState.scrollToItem(messages.size - 1)
        }
    }

    val blockedUsers by viewModel.blockedUsers.collectAsState(initial = emptyList())
    val usersWhoBlocked by viewModel.usersWhoBlocked.collectAsState(initial = emptyList())
    
    val isBlocked = blockedUsers.contains(otherUserId) || usersWhoBlocked.contains(otherUserId)

    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!otherUser?.profilePicUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = otherUser?.profilePicUrl,
                                    contentDescription = "Profile",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    otherUser?.username?.firstOrNull()?.uppercase() ?: "U",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(otherUser?.username ?: "User", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (blockedUsers.contains(otherUserId)) "Unblock User" else "Block User") },
                            onClick = {
                                viewModel.toggleBlock(otherUserId)
                                showMenu = false
                            }
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (isBlocked) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp).imePadding(), contentAlignment = Alignment.Center) {
                    Text("You cannot reply to this conversation.", color = MaterialTheme.colorScheme.error)
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .imePadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Message...") },
                        shape = RoundedCornerShape(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendMessage(conversationId, otherUserId, inputText)
                                inputText = ""
                            }
                        },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            items(messages) { message ->
                val isOwn = message.senderId == currentUser?.id
                
                var showDelete by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isOwn) Arrangement.End else Arrangement.Start
                ) {
                    if (isOwn && showDelete) {
                        IconButton(
                            onClick = { 
                                viewModel.deleteMessage(message.id) 
                                showDelete = false
                            },
                            modifier = Modifier.align(Alignment.CenterVertically)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    
                    Column(
                        modifier = Modifier
                            .clickable { if (isOwn) showDelete = !showDelete }
                            .background(
                                color = if (isOwn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isOwn) 16.dp else 0.dp,
                                    bottomEnd = if (isOwn) 0.dp else 16.dp
                                )
                            )
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .widthIn(max = 280.dp)
                    ) {
                        Text(
                            text = message.text,
                            color = if (isOwn) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
                        Text(
                            text = sdf.format(Date(message.timestamp)),
                            fontSize = 10.sp,
                            color = if (isOwn) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}
