plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val prayerKeySupabaseUrl = providers.gradleProperty("PRAYERKEY_SUPABASE_URL")
    .orElse(providers.environmentVariable("PRAYERKEY_SUPABASE_URL"))
    .orElse("")
val prayerKeySupabaseKey = providers.gradleProperty("PRAYERKEY_SUPABASE_PUBLISHABLE_KEY")
    .orElse(providers.environmentVariable("PRAYERKEY_SUPABASE_PUBLISHABLE_KEY"))
    .orElse("")

android {
    namespace = "com.prayerkey.manna"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.prayerkey.manna"
        minSdk = 26
        targetSdk = 36
        versionCode = 21
        versionName = "3.0.2"


        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
        manifestPlaceholders["usesCleartextTraffic"] = "false"
        buildConfigField("String", "SUPABASE_URL", "\"${prayerKeySupabaseUrl.get()}\"")
        buildConfigField("String", "SUPABASE_PUBLISHABLE_KEY", "\"${prayerKeySupabaseKey.get()}\"")
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

    /*
     * One APK per architecture.
     *
     * sherpa-onnx's native library is 24 MB on arm64 and there is one for every
     * ABI, so a single universal APK is 182 MB of which a given phone can use a
     * quarter. Play's app bundle splits this automatically; these splits make
     * the same true of a local build, and make the real per-device size visible
     * rather than hidden behind a number nobody ships.
     *
     * x86_64 is here for the emulator only.
     */
    splits.abi {
        isEnable = true
        reset()
        include("arm64-v8a", "armeabi-v7a", "x86_64")
        /* Also build one APK that carries every architecture.
           Without it the only files to hand were per-ABI, so an APK sent to
           somebody with the wrong phone fails to install with "your device
           isn't compatible" - which says nothing about the actual reason. The
           bundle is unaffected; Play always splits it per device. */
        isUniversalApk = true
    }

    /* The ONNX model must reach the device as a file, not a deflated stream —
       onnxruntime memory-maps it. */
    androidResources.noCompress += "onnx"
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

    /* sherpa-onnx, for punctuation. Not on Maven Central — the project ships
       an .aar on its GitHub releases, fetched by `fetchSherpaOnnx` below and
       kept out of git, because a 38 MB binary does not belong in a repository. */
    implementation(files("libs/sherpa-onnx-1.13.6.aar"))

    /* Pure Java, no native code. The sherpa model ships as a .tar.bz2 and the
       platform can read neither, so this unpacks it. Chosen over hosting a zip
       ourselves because the app has no server and should not need one. */
    implementation("org.apache.commons:commons-compress:1.27.1")
    implementation("androidx.work:work-runtime-ktx:2.10.1")

    testImplementation("junit:junit:4.13.2")
    /* android.jar's org.json is a stub that throws "not mocked" off-device, so
       the devotional asset could not be validated on the JVM. This puts a real
       implementation on the test classpath only — the app still uses the
       platform's. */
    testImplementation("org.json:json:20240303")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

/**
 * Fetches the sherpa-onnx Android library.
 *
 * It is not published to Maven Central; the project releases a prebuilt .aar
 * on GitHub. Committing 38 MB of native binary would bloat every clone of this
 * repository forever, so it is downloaded once into app/libs and gitignored.
 *
 * The static-link build is the one taken: it folds onnxruntime into a single
 * .so instead of shipping it beside, which is smaller and removes a load-order
 * problem on older devices.
 */
val sherpaVersion = "1.13.6"
val sherpaAar = layout.projectDirectory.file("libs/sherpa-onnx-$sherpaVersion.aar").asFile

val fetchSherpaOnnx by tasks.registering {
    outputs.file(sherpaAar)
    onlyIf { !sherpaAar.exists() }
    doLast {
        sherpaAar.parentFile.mkdirs()
        val url = "https://github.com/k2-fsa/sherpa-onnx/releases/download/" +
            "v$sherpaVersion/sherpa-onnx-static-link-onnxruntime-$sherpaVersion.aar"
        logger.lifecycle("Fetching sherpa-onnx $sherpaVersion ...")
        // uri() is Gradle's own helper; a bare java.net.URL does not resolve
        // here, because `java` in a Kotlin DSL script is the java extension.
        uri(url).toURL().openStream().use { input ->
            sherpaAar.outputStream().use { input.copyTo(it) }
        }
        require(sherpaAar.length() > 30_000_000) {
            "sherpa-onnx download was cut short at ${sherpaAar.length() / 1_048_576} MB"
        }
    }
}

tasks.named("preBuild") { dependsOn(fetchSherpaOnnx) }
