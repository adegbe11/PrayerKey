import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// PrayerKey's screens, shared by Android and iPhone: Compose Multiplatform,
// with the app's images and fonts as shared resources. Nothing here may touch
// Android or iOS directly; platform pieces are expect/actual.
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    androidTarget {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }
    // Built on a Mac (Codemagic); skipped on Windows. The iPhone app links this framework.
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework {
            baseName = "PrayerKeyUI"
            isStatic = true
        }
    }
    // Compose cannot build for Windows native, so the web build is this
    // module's check that no Java or Android has slipped into the screens.
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            api(project(":shared"))
            api(compose.runtime)
            api(compose.foundation)
            api(compose.ui)
            api(compose.material3)
            api(compose.materialIconsExtended)
            api(compose.components.resources)
            api("org.jetbrains.compose.ui:ui-backhandler:1.8.2")
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.prayerkey.manna.resources"
    generateResClass = always
}

android {
    namespace = "com.prayerkey.manna.ui"
    compileSdk = 36
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
