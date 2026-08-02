package it.hydr4.argonaut.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import it.hydr4.argo.ArgoClient
import it.hydr4.argo.api.ArgoClientConfig
import it.hydr4.argo.api.InMemoryCookieJar
import it.hydr4.argo.api.OkHttpEngine
import it.hydr4.argo.storage.TokenStore
import it.hydr4.argonaut.core.network.NetworkMonitor
import it.hydr4.argonaut.data.AuthRepository
import it.hydr4.argonaut.data.DashboardRepository
import it.hydr4.argonaut.data.DefaultAuthRepository
import it.hydr4.argonaut.data.DefaultDashboardRepository
import it.hydr4.argonaut.data.SettingsRepository
import it.hydr4.argonaut.data.SharedPreferencesSettingsRepository
import it.hydr4.argonaut.data.storage.AndroidTokenStore
import it.hydr4.argonaut.data.storage.DashboardSnapshotCache
import it.hydr4.argonaut.data.storage.WidgetSnapshotStore
import it.hydr4.argonaut.widget.WidgetSyncScheduler
import it.hydr4.argonaut.widget.WorkManagerWidgetSyncScheduler
import javax.inject.Singleton

/**
 * Composition root. The [ArgoClient] is a process-scoped singleton: the SSO
 * session, its repositories and the encrypted token store all live as long as
 * the application process. Repository interfaces are bound to their
 * Argos-backed implementations here, so ViewModels stay decoupled.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ArgonautModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: DefaultAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindDashboardRepository(impl: DefaultDashboardRepository): DashboardRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SharedPreferencesSettingsRepository): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindWidgetSyncScheduler(impl: WorkManagerWidgetSyncScheduler): WidgetSyncScheduler

    companion object {

        @Provides
        @Singleton
        fun provideTokenStore(@ApplicationContext context: Context): TokenStore = AndroidTokenStore(context)

        @Provides
        @Singleton
        fun provideArgoClient(storage: TokenStore): ArgoClient = ArgoClient.create(
            config = ArgoClientConfig(),
            // The SSO dance binds cookies across redirect hops; Argos ships an
            // in-memory jar exactly for this, no persistence needed.
            engine = OkHttpEngine(cookieJar = InMemoryCookieJar()),
            storage = storage,
        )

        @Provides
        @Singleton
        fun provideWidgetSnapshotStore(@ApplicationContext context: Context): DashboardSnapshotCache = WidgetSnapshotStore(context)

        @Provides
        @Singleton
        fun provideNetworkMonitor(@ApplicationContext context: Context): NetworkMonitor = NetworkMonitor(context)
    }
}
