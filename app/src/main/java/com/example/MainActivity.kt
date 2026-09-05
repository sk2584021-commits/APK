package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.VyroApp
import com.example.ui.theme.VyroTheme
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import coil.Coil

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val imageLoader = ImageLoader.Builder(this)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .build()
        Coil.setImageLoader(imageLoader)
        
        enableEdgeToEdge()
        setContent {
            VyroTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    VyroApp()
                }
            }
        }
    }
}
