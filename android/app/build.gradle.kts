plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.prayerkey.manna"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.prayerkey.manna"
        minSdk = 26
        targetSdk = 36
        versionCode = 17
        versionName = "3.0.0"

        val sermonSocketUrl = providers.gradleProperty("SERMON_SOCKET_URL")
            .orElse(providers.environmentVariable("SERMON_SOCKET_URL"))
            .orElse("https://www.prayerkey.com")
        buildConfigField("String", "SERMON_SOCKET_URL", "\"${sermonSocketUrl.get()}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
        manifestPlaceholders["usesCleartextTraffic"] = "false"
    }

    val releaseStoreFile = providers.gradleProperty("PRAYERKEY_STORE_FILE")
        .orElse(providers.environmentVariable("PRAYERKEY_STORE_FILE"))
    val releaseStorePassword = providers.gradleProperty("PRAYERKEY_STORE_PASSWORD")
        .orElse(providers.environmentVariable("PRAYERKEY_STORE_PASSWORD"))
    val releaseKeyAlias = providers.gradleProperty("PRAYERKEY_KEY_ALIAS")
        .orElse(providers.environmentVariable("PRAYERKEY_KEY_ALIAS"))
    val releaseKeyPassword = providers.gradleProperty("PRAYERKEY_KEY_PASSWORD")
        .orElse(providers.environmentVariable("PRAYERKEY_KEY_PASSWORD"))

    signingConfigs {
        if (releaseStoreFile.isPresent && releaseStorePassword.isPresent &&
            releaseKeyAlias.isPresent && releaseKeyPassword.isPresent
        ) {
            create("prayerKeyRelease") {
                storeFile = file(releaseStoreFile.get())
                storePassword = releaseStorePassword.get()
                keyAlias = releaseKeyAlias.get()
                keyPassword = releaseKeyPassword.get()
            }
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "SERMON_SOCKET_URL", "\"http://10.0.2.2:3001\"")
            manifestPlaceholders["usesCleartextTraffic"] = "true"
        }
        release {
            isMinifyEnabled = true
            isDebuggable = false
            signingConfig = signingConfigs.findByName("prayerKeyRelease")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions.jvmTarget = "17"

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging.resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.05.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")
    implementation("androidx.profileinstaller:profileinstaller:1.4.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("io.socket:socket.io-client:2.1.1") {
        exclude(group = "org.json", module = "json")
    }

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
