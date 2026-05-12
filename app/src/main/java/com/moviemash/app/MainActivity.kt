package com.moviemash.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.moviemash.app.ui.MovieMashApp
import com.moviemash.app.ui.MovieMashTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MovieMashTheme {
                MovieMashApp()
            }
        }
    }
}
