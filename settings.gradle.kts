pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()        //see https://mvnrepository.com/repos/space-public-compose-dev
        maven(url ="https://androidx.dev/storage/compose-compiler/repository/")
    }
}

rootProject.name = "compose_bloc"
include(":kbloc_core")
include(":kbloc_compose")
include(":kbloc_navigator")
include(":kbloc_app")
