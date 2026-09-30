plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.download.maps.common"
    compileSdk {
        version = release(libs.versions.compileSdk.get().toInt())
    }

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui.graphics)

    implementation(libs.androidx.navigation3.runtime)

    implementation(libs.hilt.android)
}
