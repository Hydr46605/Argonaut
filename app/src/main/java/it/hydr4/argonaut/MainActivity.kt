package it.hydr4.argonaut

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import it.hydr4.argonaut.ui.ArgonautApp

/**
 * The single activity of Argonaut: every screen transition is handled by
 * Compose Navigation inside [ArgonautApp].
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArgonautApp()
        }
    }
}
