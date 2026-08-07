package it.hydr4.argonaut.ui.screens.about

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.hydr4.argonaut.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/** Static about-screen data. */
data class AboutUiState(
    val version: String = "",
    val repositoryUrl: String = "",
)

@HiltViewModel
class AboutViewModel @Inject constructor() : ViewModel() {

    val uiState: StateFlow<AboutUiState> = MutableStateFlow(
        AboutUiState(
            version = BuildConfig.ARGONAUT_VERSION,
            repositoryUrl = REPOSITORY_URL,
        ),
    ).asStateFlow()

    private companion object {
        const val REPOSITORY_URL = "https://github.com/Hydr46605/Argonaut"
    }
}
