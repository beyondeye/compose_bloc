import Versions.jbcompose_version
import org.jetbrains.compose.compose

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("com.vanniktech.maven.publish")
}

apply(plugin = "kotlinx-atomicfu")

setupModuleForComposeMultiplatform()

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":kbloc_core"))//                 api(projects.kbloc_core)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines_version}")
                implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:${Versions.kotlinx_collections_immutable_version}")
                compileOnly(compose.runtime)
                compileOnly("org.jetbrains.compose.runtime:runtime-saveable:${Versions.jbcompose_version}")
                implementation(Deps.AtomicFu.common)
                //implementation(Deps.Napier.core)
            }
        }

        val jvmTest by getting {
            dependencies {
                //              implementation(libs.junit.api)
                //             runtimeOnly(libs.junit.engine)
            }
        }

        val androidTest by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.ui)
//                testImplementation("junit:junit:${Versions.junit_version}")
//                testImplementation ("org.assertj:assertj-core:${Versions.assertj_version}")
//                androidTestImplementation("androidx.test.ext:junit:1.1.3")
//                androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
            }
        }

        val androidMain by getting {
            dependencies {
//                implementation("androidx.core:core-ktx:${Versions.androidx_corektx_version}")
                compileOnly("androidx.activity:activity-compose:${Versions.compose_activity_version}")
                api("androidx.lifecycle:lifecycle-viewmodel-compose:${Versions.androidx_lifecycle_version}")
                implementation("androidx.lifecycle:lifecycle-runtime-ktx:${Versions.androidx_lifecycle_version}")
                // https://developer.android.com/jetpack/androidx/releases/lifecycle#version_26_2
                //implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.0-alpha01")
            }
        }

        val jsMain by getting {
        }
        val jsTest by getting {
        }
    }
}
