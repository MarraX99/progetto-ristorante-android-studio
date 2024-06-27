plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.com.google.devtools.ksp)
    alias(libs.plugins.androidx.navigation.safeargs.kotlin)
    alias(libs.plugins.androidx.room)
}

android {
    namespace = "com.projectrestaurant"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.projectrestaurant"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        //noinspection DataBindingWithoutKapt
        dataBinding = true
    }
    room {
        schemaDirectory("$projectDir/schemas")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.coordinatorlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx) // optional - Kotlin Extensions and Coroutines support for Room
    implementation(libs.bumptech.glide)
    //implementation("com.google.firebase:firebase-storage:21.0.0")
    //implementation("com.firebaseui:firebase-ui-storage:7.2.0")
    annotationProcessor(libs.androidx.room.compiler)
    ksp(libs.androidx.room.compiler) // To use Kotlin Symbol Processing (KSP)
    //ksp("com.github.bumptech.glide:compiler:4.16.0")
    testImplementation(libs.junit)
    testImplementation(libs.androidx.room.testing) // optional - Test helpers for Room
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}