plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

// Add maven repositories
repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

// Enable JS(IR) target and add dependencies
kotlin {
    js(IR) {
        browser()
        binaries.executable()
    }
    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(compose.web.core)
                implementation(compose.runtime)
                implementation("org.jetbrains.compose.runtime:runtime-saveable:${Versions.jbcompose_version}")
                implementation(project(":kbloc_navigator"))
                implementation(project(":kbloc_router"))
                implementation(Deps.Napier.core)
            }
        }
    }
}