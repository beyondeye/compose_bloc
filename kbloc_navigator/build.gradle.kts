
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
                api(project(":kbloc_core"))
                api(project(":kbloc_compose"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines_version}")
                implementation(Deps.AtomicFu.common)
                compileOnly(compose.runtime)
                compileOnly("org.jetbrains.compose.runtime:runtime-saveable:${Versions.jbcompose_version}")

//                compileOnly(compose.material)
            }
        }
        val desktopMain by getting {
            dependencies {
                compileOnly(compose.ui)
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
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("androidx.activity:activity-compose:${Versions.compose_activity_version}")
            }
        }

        val jsMain by getting {
        }
        val jsTest by getting {
        }
    }
}
