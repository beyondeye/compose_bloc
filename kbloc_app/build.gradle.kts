plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 32

    defaultConfig {
        applicationId = "com.beyondeye.kbloc_app"
        minSdk = 21
        targetSdk = 32
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = rootProject.extra["compose_compiler_version"] as String
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

//    implementation(project(":kbloc_core"))
    implementation(project(":kbloc_compose"))
    implementation(project(":kbloc_navigator"))
    implementation("androidx.core:core-ktx:${Versions.androidx_corektx_version}")
    implementation("androidx.compose.runtime:runtime:${Versions.compose_version}")
    implementation("androidx.compose.runtime:runtime-saveable:${Versions.compose_version}")
    implementation("androidx.compose.ui:ui:${Versions.compose_version}")
    implementation("androidx.compose.material:material:${Versions.compose_material_version}")
    implementation("androidx.compose.ui:ui-tooling-preview:${Versions.compose_version}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${Versions.androidx_lifecycle_version}")
    implementation("androidx.activity:activity-compose:${Versions.compose_activity_version}")

    testImplementation("junit:junit:${Versions.junit_version}")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:${Versions.compose_version}")
    debugImplementation("androidx.compose.ui:ui-tooling:${Versions.compose_version}")
    debugImplementation("androidx.compose.ui:ui-test-manifest:${Versions.compose_version}")
}