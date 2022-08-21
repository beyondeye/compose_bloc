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
include(":kbloc_rxjava")
include(":kbloc_tab_navigator")
include(":kbloc_bottom_sheet_navigator")
include(":kbloc_transitions")
include(":kbloc_kodein")
include(":kbloc_koin")
include(":kbloc_androidx")
include(":kbloc_hilt")
