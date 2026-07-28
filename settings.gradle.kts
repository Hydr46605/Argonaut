pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// Argos is developed side-by-side; the composite build guarantees Argonaut
// always builds against the latest local Argos, with dependency substitution
// kicking in automatically for the `it.hydr4:Argos` coordinates. CI checks out
// Argos into the sibling directory before configuring the build.
if (rootDir.parentFile.resolve("Argos").isDirectory) {
    includeBuild("../Argos")
}

rootProject.name = "Argonaut"
include(":app")
