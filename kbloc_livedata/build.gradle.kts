plugins {
    kotlin("android")
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

dependencies {
    api(project(":kbloc_navigator"))

    implementation("androidx.compose.runtime:runtime:${Versions.compose_core_libs_version}")

//    testRuntimeOnly(libs.junit.engine)
//    testImplementation(libs.junit.api)
}
