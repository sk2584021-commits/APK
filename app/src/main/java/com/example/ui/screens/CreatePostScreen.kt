package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun CreatePostScreen(onPostCreated: (String, String, String?, Boolean) -> Unit) {
    var caption by remember { mutableStateOf("") }
    var hashtags by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Create Post", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = caption,
            onValueChange = { caption = it },
            label = { Text("What's on your mind?") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .testTag("caption_input"),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = hashtags,
            onValueChange = { hashtags = it },
            label = { Text("Hashtags (e.g. #vyro #vibes)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        if (selectedImageUri != null) {
            Box(modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(12.dp))) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "Selected image preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = { selectedImageUri = null },
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                ) {
                    Text("X", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            OutlinedButton(
                onClick = { photoPickerLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Image, contentDescription = "Add Media")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select Photo or Video")
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = {
                if (caption.isNotBlank() || selectedImageUri != null) {
                    val isVideo = selectedImageUri?.let { uri ->
                        context.contentResolver.getType(uri)?.startsWith("video/") == true
                    } ?: false
                    onPostCreated(caption, hashtags, selectedImageUri?.toString(), isVideo)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("submit_post_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Publish", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
