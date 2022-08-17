plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("com.vanniktech.maven.publish")
}

kotlin {
    android()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = "KBloc core module"
        homepage = "Link to KBloc core homepage"
        ios.deploymentTarget = "14.1"
        framework {
            baseName = "kbloc-core"
        }
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines_version}")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:${Versions.kotlinx_datetime_version}")
                api("org.jetbrains.kotlinx:kotlinx-collections-immutable:${Versions.kotlinx_collections_immutable_version}")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                //https://github.com/mockk/mockk/releases
                implementation ("io.mockk:mockk-common:${Versions.mockk_version}")
                //see https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-test/index.html
                //sse https://developer.android.com/kotlin/coroutines/test#testdispatchers
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.coroutines_version}")
            }
        }
        val androidMain by getting
        val androidTest by getting {
            dependencies {
                implementation("io.mockk:mockk:${Versions.mockk_version}")
            }
        }
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by creating {
            dependsOn(commonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }
    }
}

android {
    compileSdk = 32
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 21
        targetSdk = 32
    }
}