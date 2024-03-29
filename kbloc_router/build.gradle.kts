
plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("com.vanniktech.maven.publish")
}

//apply(plugin = "kotlinx-atomicfu")

setupModuleForComposeMultiplatform()

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":kbloc_core"))
                implementation(project(":kbloc_navigator"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines_version}")
                compileOnly(compose.runtime)
                compileOnly("org.jetbrains.compose.runtime:runtime-saveable:${Versions.jbcompose_version}")
//                implementation(Deps.Napier.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
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
            dependencies {
                implementation(compose.web.core)
            }
        }
        val jsTest by getting {
        }
    }
}
