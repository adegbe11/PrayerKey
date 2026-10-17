// On CI (GitHub's shared machines) Maven Central answers 429 "too many
// requests", so builds there go to Google's mirror of Central first.
val ciMirror = System.getenv("CI") != null

pluginManagement {
    repositories {
        if (System.getenv("CI") != null) maven("https://maven-central.storage-download.googleapis.com/maven2/")
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // Keep compatibility with developer machines that inject a corporate mirror.
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        if (ciMirror) maven("https://maven-central.storage-download.googleapis.com/maven2/")
        google()
        mavenCentral()
    }
}

rootProject.name = "Manna"
include(":app")
include(":shared")
include(":ui")
