plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 32

    androidComponents {
        // exclude the release variant, failing in Github CI validateSigning
        val release = selector().withBuildType("release")
        beforeVariants(release) { variantBuilder -> variantBuilder.enable = false }
    }

    defaultConfig {
        applicationId = "dev.alexbeggs.counter"
        minSdk = 23
        targetSdk = 32
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }

    packagingOptions {
        resources {
            excludes += setOf("META-INF/INDEX.LIST", "META-INF/io.netty.versions.properties")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions { jvmTarget = "1.8" }
    namespace = "dev.alexbeggs.counter"
}

dependencies {
    implementation(project(":mobius-timetravel-server-http"))
    implementation(project(":mobius-timetravel-core"))

    implementation("com.google.android.material:material:1.5.0")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")

    // LiveData
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.3.1")
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.1")
    implementation("androidx.fragment:fragment-ktx:1.3.6")

    testImplementation("com.spotify.mobius:mobius-test:1.5.8")

    implementation("com.spotify.mobius:mobius-rx3:1.5.8") // only for RxJava 3 support
    implementation("com.spotify.mobius:mobius-android:1.5.8") // only for Android support
    implementation("com.spotify.mobius:mobius-extras:1.5.8") // utilities for common patterns

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}
