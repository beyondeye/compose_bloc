pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven(url ="https://androidx.dev/storage/compose-compiler/repository/")
    }
}

rootProject.name = "compose_bloc"
include(":kbloc_core")
include(":kbloc_compose")
include(":kbloc_app")
