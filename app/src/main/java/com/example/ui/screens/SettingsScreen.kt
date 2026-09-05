package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.VyroViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: VyroViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSavedPosts: () -> Unit = {}
) {
    val currentUser by viewModel.currentUser.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                TextButton(onClick = { viewModel.logout() }) { Text("Logout", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            var showPrivacyDialog by remember { mutableStateOf(false) }

            if (showPrivacyDialog) {
                var isPrivate by remember { mutableStateOf(currentUser?.isPrivate ?: false) }
                AlertDialog(
                    onDismissRequest = { showPrivacyDialog = false },
                    title = { Text("Privacy Settings") },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Private Account")
                            Spacer(modifier = Modifier.weight(1f))
                            Switch(checked = isPrivate, onCheckedChange = { isPrivate = it })
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { 
                            currentUser?.let { user -> viewModel.updateUser(user.copy(isPrivate = isPrivate)) }
                            showPrivacyDialog = false
                        }) { Text("Save") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPrivacyDialog = false }) { Text("Cancel") }
                    }
                )
            }

            SettingsSection("ACCOUNT")
            SettingsItem("Edit Profile") { /* Handled in ProfileScreen for now, but could be moved */ }
            SettingsItem("Privacy") { showPrivacyDialog = true }

            var showBlockedAccountsDialog by remember { mutableStateOf(false) }

            if (showBlockedAccountsDialog) {
                val blockedUsers by viewModel.blockedUsers.collectAsState(initial = emptyList())
                AlertDialog(
                    onDismissRequest = { showBlockedAccountsDialog = false },
                    title = { Text("Blocked Accounts") },
                    text = {
                        if (blockedUsers.isEmpty()) {
                            Text("No blocked accounts.")
                        } else {
                            Column(Modifier.verticalScroll(rememberScrollState())) {
                                blockedUsers.forEach { blockedId ->
                                    Row(
                                        Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("User ID: $blockedId")
                                        TextButton(onClick = { viewModel.toggleBlock(blockedId) }) {
                                            Text("Unblock")
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showBlockedAccountsDialog = false }) { Text("Close") }
                    }
                )
            }

            SettingsSection("ACTIVITY")
            SettingsItem("Notifications") { /* TODO */ }
            SettingsItem("Saved Posts") { onNavigateToSavedPosts() }
            SettingsItem("Blocked Accounts") { showBlockedAccountsDialog = true }

            SettingsSection("APP")
            SettingsItem("Appearance") { /* TODO */ }
            SettingsItem("About VYRO") { /* TODO */ }
            SettingsItem("Help & Support") { /* TODO */ }

            SettingsSection("ACCOUNT ACTIONS")
            SettingsItem("Logout", color = MaterialTheme.colorScheme.error) { showLogoutDialog = true }
            SettingsItem("Delete Account", color = MaterialTheme.colorScheme.error) { /* TODO */ }
        }
    }
}

@Composable
fun SettingsSection(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.primary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsItem(title: String, color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 16.sp, color = color)
    }
}
