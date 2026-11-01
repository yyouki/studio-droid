plugins {
    id("com.android.application")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "dev.studiodroid.prototype"
    // Android SDK 37 is published as platform 37.0; state the minor level explicitly.
    compileSdk {
        version = release(37) {
            minorApiLevel = 0
        }
    }

    defaultConfig {
        applicationId = "dev.studiodroid.prototype"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0-prototype"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // Compose UI is declared directly because Nemo exposes Compose types in its public API.
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("org.jetbrains.compose.runtime:runtime:1.12.2")
    implementation("org.jetbrains.compose.foundation:foundation:1.12.2")
    implementation("org.jetbrains.compose.ui:ui:1.12.2")
    implementation("org.jetbrains.compose.material3:material3:1.9.0")

    // Isolated MIT-licensed editor prototype; not yet adopted by the main application.
    implementation("io.github.ma7moud3ly:nemo-editor:2.0.0")
    // Optional Nemo tab strip and per-document tab state manager.
    implementation("io.github.ma7moud3ly:nemo-tabs:2.0.0")
}
