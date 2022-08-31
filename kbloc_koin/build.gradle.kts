plugins {
    kotlin("multiplatform")
//    kotlin("native.cocoapods")
    id("com.android.library")
    id("com.vanniktech.maven.publish")
    id("org.jetbrains.compose")
}

setupModuleForComposeMultiplatform()

kotlin {
//    iosX64()
//    iosArm64()
//    iosSimulatorArm64()

//    cocoapods {
//        summary = "KBloc core module"
//        homepage = "Link to KBloc core homepage"
//        ios.deploymentTarget = "14.1"
//        framework {
//            baseName = "kbloc-core"
//        }
//    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":kbloc_navigator"))
                implementation(Deps.Koin.core)
                compileOnly(compose.runtime)
                compileOnly("org.jetbrains.compose.runtime:runtime-saveable:${Versions.jbcompose_version}")
            }
        }
        val commonTest by getting {
            dependencies {
            }
        }
        val androidMain by getting
        val androidTest by getting {
            dependencies {
                implementation("io.mockk:mockk:${Versions.mockk_version}")
            }
        }
//        val iosX64Main by getting
//        val iosArm64Main by getting
//        val iosSimulatorArm64Main by getting
//        val iosMain by creating {
//            dependsOn(commonMain)
//            iosX64Main.dependsOn(this)
//            iosArm64Main.dependsOn(this)
//            iosSimulatorArm64Main.dependsOn(this)
//        }
//        val iosX64Test by getting
//        val iosArm64Test by getting
//        val iosSimulatorArm64Test by getting
//        val iosTest by creating {
//            dependsOn(commonTest)
//            iosX64Test.dependsOn(this)
//            iosArm64Test.dependsOn(this)
//            iosSimulatorArm64Test.dependsOn(this)
//        }
        val jvmTest by getting {
            dependencies {
//                implementation(libs.junit.api)
//                runtimeOnly(libs.junit.engine)
            }
        }

        val jsMain by getting {
        }
        val jsTest by getting {
        }
    }
}


