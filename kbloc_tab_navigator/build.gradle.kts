plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("com.vanniktech.maven.publish")
}

setupModuleForComposeMultiplatform()

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":kbloc_navigator"))
                compileOnly(compose.runtime)
                compileOnly(compose.material)
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
 //               implementation(compose.runtime)
 //               implementation(compose.ui)
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
