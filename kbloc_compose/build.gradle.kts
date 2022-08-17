plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.vanniktech.maven.publish")
}

android {
    compileSdk = 32

    defaultConfig {
        minSdk = 21
        targetSdk = 32

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = rootProject.extra["compose_compiler_version"] as String
    }

}
  //
dependencies {
    api(project(":kbloc_core"))
    implementation("androidx.core:core-ktx:${Versions.androidx_corektx_version}")
    implementation("androidx.compose.runtime:runtime:${Versions.compose_version}")
    implementation("androidx.compose.runtime:runtime-saveable:${Versions.compose_version}")
    implementation("androidx.compose.material:material:${Versions.compose_material_version}")

    //implementation("androidx.compose.ui:ui:${rootProject.extra["compose_version"]}")
    //implementation("androidx.compose.ui:ui-tooling-preview:${rootProject.extra["compose_version"]}")
    //    implementation("androidx.activity:activity-compose:1.3.1")
    api("androidx.lifecycle:lifecycle-viewmodel-compose:${Versions.androidx_lifecycle_version}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${Versions.androidx_lifecycle_version}")

    compileOnly("androidx.activity:activity-compose:${Versions.compose_activity_version}")

    testImplementation("junit:junit:${Versions.junit_version}")
    testImplementation ("org.assertj:assertj-core:${Versions.assertj_version}")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    //androidTestImplementation("androidx.compose.ui:ui-test-junit4:${rootProject.extra["compose_version"]}")
//    debugImplementation("androidx.compose.ui:ui-tooling:${rootProject.extra["compose_version"]}")
//    debugImplementation("androidx.compose.ui:ui-test-manifest:${rootProject.extra["compose_version"]}")


}