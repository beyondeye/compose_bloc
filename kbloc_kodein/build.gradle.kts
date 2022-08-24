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
                implementation("org.kodein.di:kodein-di-framework-compose:${Versions.kodein}")
            }
        }

        val jvmTest by getting {
            dependencies {
  //              implementation(libs.junit.api)
   //             runtimeOnly(libs.junit.engine)
            }
        }

        val jsMain by getting {
        }
        val jsTest by getting {
        }
    }
}
