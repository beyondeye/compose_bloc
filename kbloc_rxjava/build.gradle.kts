plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("com.vanniktech.maven.publish")
}

setupModuleForComposeMultiplatform(withJS = false)

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
            }
        }
        val jvmMain by getting {
            dependencies {
                api(project(":kbloc_navigator"))
                compileOnly("androidx.compose.runtime:runtime-rxjava3:${Versions.compose_core_libs_version}")
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
            }
        }

        val androidMain by getting {
            dependencies {
            }
        }
    }
}
