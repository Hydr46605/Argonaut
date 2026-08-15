import io.gitlab.arturbosch.detekt.Detekt
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.detekt)
}

/** CalVer `YYYY.MM.MICRO` as in Argos; MICRO maps onto the patch digit of the versionCode. */
data class CalendarVersion(val year: Int, val month: Int, val micro: Int) {
    val name: String = "%04d.%02d.%d".format(year, month, micro)
    val code: Int = (year - 2000) * 10_000 + month * 100 + micro
}

fun parseCalendarVersion(raw: String): CalendarVersion {
    val match = Regex("""^(\d{4})\.(\d{2})\.(\d{1,2})$""").matchEntire(raw.trim())
        ?: error("Invalid Argonaut version '$raw'; expected YYYY.MM.MICRO.")
    val (yearText, monthText, microText) = match.destructured
    val year = yearText.toInt()
    val month = monthText.toInt()
    val micro = microText.toInt()
    require(year in 2000..4099) { "Release year must be between 2000 and 4099, was $year" }
    require(month in 1..12) { "Release month must be 1..12, was $month" }
    require(micro in 0..99) { "Release micro must be 0..99, was $micro" }
    return CalendarVersion(year, month, micro)
}

val argonautVersion = providers.gradleProperty("version")
    .map(::parseCalendarVersion)
    .get()

// Release signing: keystore.properties is gitignored. CI writes it from secrets;
// local releases generate it once via `./gradlew createReleaseKeystore`.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val signingConfigAvailable = keystorePropertiesFile.isFile

fun loadReleaseSigningProperties(): Properties {
    val props = Properties()
    keystorePropertiesFile.inputStream().use { input -> props.load(input) }
    return props
}

android {
    namespace = "it.hydr4.argonaut"
    compileSdk = 37

    defaultConfig {
        applicationId = "it.hydr4.argonaut"
        minSdk = 24
        targetSdk = 37
        versionCode = argonautVersion.code
        versionName = argonautVersion.name

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        buildConfigField("String", "ARGONAUT_VERSION", "\"${argonautVersion.name}\"")
        buildConfigField("String", "ARGONAUT_VERSION_CODE", "\"${argonautVersion.code}\"")
    }

    signingConfigs {
        if (signingConfigAvailable) {
            create("release") {
                val props = loadReleaseSigningProperties()
                storeFile = rootProject.file(props.getProperty("storeFile"))
                storePassword = props.getProperty("storePassword")
                keyAlias = props.getProperty("keyAlias")
                keyPassword = props.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (signingConfigAvailable) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            // Widgets and WorkManager run fine on debug; keep them debuggable.
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        // android.util.Log is a no-op stub on the JVM; diagnostics must not
        // crash unit tests.
        unitTests.isReturnDefaultValues = true
    }

    packaging.resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"

    lint {
        abortOnError = true
        checkReleaseBuilds = true
        warningsAsErrors = true
        disable += setOf(
            "AndroidGradlePluginVersion",
            "GradleDependency",
            "IconDensities",
            "IconMissingDensityFolder",
        )
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
        allWarningsAsErrors = true
    }
}

dependencies {
    // Local Argos composite build (dependency-substituted from ../Argos).
    implementation(libs.argos)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.coroutines.android)

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3.windowsizeclass)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Widgets & background sync.
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.work.runtime.ktx)

    // Hilt (KSP).
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    implementation(libs.kotlinx.serialization.json)

    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Unit tests.
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlinx.serialization.json)

    // Instrumented tests (Hilt + Compose).
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(composeBom)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
    parallel = true
}

tasks.withType<Detekt>().configureEach {
    // The launcher JVM may be newer than detekt supports; analyze at 17.
    jvmTarget = "17"
    reports {
        sarif.required = true
        html.required = true
        txt.required = false
    }
}

// Every quality gate is runnable with one command: `./gradlew check` covers
// formatting (root spotless), static analysis (detekt), unit tests + lint
// (Android defaults) and the instrumented-test compilation.
tasks.named("check") {
    dependsOn(":spotlessCheck", "detekt", "assembleDebugAndroidTest")
}

tasks.register("verifyAll") {
    group = "verification"
    description = "Local CI gate: formatting, static analysis, unit tests, lint, APK assembly."
    dependsOn("check", "assembleDebug")
}

/**
 * One-shot local release signing setup: generates a self-signed keystore and
 * the gitignored `keystore.properties` consumed by the release signingConfig.
 * Idempotent — rerun safely; CI instead writes `keystore.properties` from
 * secrets and decodes the keystore itself.
 */
tasks.register("createReleaseKeystore") {
    group = "release"
    description = "Generates a local self-signed release keystore and keystore.properties."
    val storeFile = rootProject.file("release.keystore.jks")
    val propsFile = rootProject.file("keystore.properties")
    doLast {
        if (!storeFile.exists()) {
            val exitCode = ProcessBuilder(
                "keytool", "-genkeypair", "-v",
                "-keystore", storeFile.absolutePath,
                "-alias", "argonaut",
                "-keyalg", "RSA", "-keysize", "2048", "-validity", "10000",
                "-storepass", "argonaut-release",
                "-keypass", "argonaut-release",
                "-dname", "CN=Argonaut, OU=Hydra, O=Hydra, C=IT",
            ).inheritIO().start().waitFor()
            check(exitCode == 0) { "keytool failed with exit code $exitCode" }
        }
        if (!propsFile.exists()) {
            propsFile.writeText(
                listOf(
                    "storeFile=release.keystore.jks",
                    "storePassword=argonaut-release",
                    "keyAlias=argonaut",
                    "keyPassword=argonaut-release",
                ).joinToString("\n") + "\n",
            )
        }
        println("Release signing ready: ${storeFile.name} + ${propsFile.name} (restart the build to pick it up).")
    }
}
