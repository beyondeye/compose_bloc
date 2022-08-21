plugins {
    kotlin("android")
    kotlin("kapt")
    id("com.android.library")
    id("com.vanniktech.maven.publish")
}

setupModuleForAndroidxCompose(
    composeCompilerVersion = Versions.compose_compiler_version
)

android {
    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

kapt {
    correctErrorTypes = true
}

dependencies {
    api(project(":kbloc_androidx"))

    implementation("androidx.compose.runtime:runtime:${Versions.compose_core_libs_version}")
    implementation("androidx.compose.ui:ui:${Versions.compose_core_libs_version}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:${Versions.androidx_lifecycle_version}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.androidx_lifecycle_version}")

    implementation("com.google.dagger:hilt-android:${Versions.hilt}")
    kapt("com.google.dagger:hilt-compiler:${Versions.hilt}")

//    testRuntimeOnly(libs.junit.engine)
//    testImplementation(libs.junit.api)
}
