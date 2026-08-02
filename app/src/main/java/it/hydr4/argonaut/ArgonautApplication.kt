package it.hydr4.argonaut

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application entry point. Hilt builds the object graph here; WorkManager is
 * told to use Hilt's worker factory so the widget sync worker receives
 * repository dependencies.
 */
@HiltAndroidApp
class ArgonautApplication :
    Application(),
    Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
