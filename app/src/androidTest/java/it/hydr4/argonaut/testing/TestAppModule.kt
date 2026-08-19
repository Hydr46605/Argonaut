package it.hydr4.argonaut.testing

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import it.hydr4.argonaut.data.AuthRepository
import it.hydr4.argonaut.data.DashboardRepository
import it.hydr4.argonaut.data.RefreshResult
import it.hydr4.argonaut.data.SettingsRepository
import it.hydr4.argonaut.data.model.DashboardSummary
import it.hydr4.argonaut.di.ArgonautModule
import it.hydr4.argonaut.widget.WidgetSyncScheduler
import javax.inject.Singleton

/**
 * Replaces the production graph with fakes so UI tests never touch the
 * network, the Keystore or WorkManager. Tests reach the singleton fakes
 * through `@Inject lateinit var` fields to configure behavior.
 */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [ArgonautModule::class])
object TestAppModule {

    @Provides
    @Singleton
    fun provideFakeAuthRepository(): FakeAuthRepository = FakeAuthRepository()

    @Provides
    @Singleton
    fun bindAuthRepository(impl: FakeAuthRepository): AuthRepository = impl

    @Provides
    @Singleton
    fun provideFakeDashboardRepository(): FakeDashboardRepository = FakeDashboardRepository(
        refreshResult = RefreshResult.Success(
            DashboardSummary(
                overallAverage = 8.4,
                recentGrades = listOf(
                    it.hydr4.argonaut.data.model.VotoItem(subject = "Matematica", value = 8.5),
                    it.hydr4.argonaut.data.model.VotoItem(subject = "Italiano", value = 7.0),
                ),
            ),
        ),
    )

    @Provides
    @Singleton
    fun bindDashboardRepository(impl: FakeDashboardRepository): DashboardRepository = impl

    @Provides
    @Singleton
    fun provideFakeSettingsRepository(): FakeSettingsRepository = FakeSettingsRepository()

    @Provides
    @Singleton
    fun bindSettingsRepository(impl: FakeSettingsRepository): SettingsRepository = impl

    @Provides
    @Singleton
    fun provideFakeWidgetSyncScheduler(): FakeWidgetSyncScheduler = FakeWidgetSyncScheduler()

    @Provides
    @Singleton
    fun bindWidgetSyncScheduler(impl: FakeWidgetSyncScheduler): WidgetSyncScheduler = impl
}
