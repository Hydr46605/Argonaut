plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.spotless)
}

// Repository-wide quality gates: formatting and static analysis run for every
// module and are wired into `check` in the app module.
// Spotless on Windows hits a known configuration-cache serialization issue
// (lineEndingsPolicy provider); run the formatting tasks outside the cache
// rather than disabling the cache for the whole build.
tasks.withType<com.diffplug.gradle.spotless.SpotlessTask>().configureEach {
    notCompatibleWithConfigurationCache("Spotless tasks are not configuration-cache serializable on all platforms")
}
tasks.withType<com.diffplug.gradle.spotless.SpotlessTaskImpl>().configureEach {
    notCompatibleWithConfigurationCache("Spotless tasks are not configuration-cache serializable on all platforms")
}

spotless {
    kotlin {
        // No Kotlin plugin at the root, so targets are explicit per source root.
        target("**/*.kt")
        targetExclude("**/build/**", "**/.gradle/**", "build/**")
        ktlint(libs.versions.ktlint.get())
            .editorConfigOverride(
                mapOf(
                    "ktlint_standard_filename" to "disabled",
                    // Compose composables are PascalCase by convention.
                    "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
                ),
            )
    }
    kotlinGradle {
        ktlint(libs.versions.ktlint.get())
    }
}
