import Versions.jbcompose_version
import org.jetbrains.compose.compose

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
                api(projects.kbloc_core) //                 api(projects.kbloc_core)
//                implementation("androidx.compose.runtime:runtime:${Versions.compose_version}")
                compileOnly(compose.runtime)
//                implementation("androidx.compose.material:material:${Versions.compose_material_version}")
                compileOnly(compose.material)
//                implementation("androidx.compose.runtime:runtime-saveable:${Versions.compose_version}")
                compileOnly("org.jetbrains.compose.runtime:runtime-saveable:$jbcompose_version")
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
            }
        }
    }
}
