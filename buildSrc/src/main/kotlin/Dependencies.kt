private class VersionCombos(
    val kotlin_version: String,
    val compose_core_libs: String,
    val compose_material: String,
    val compose_activity: String,
    val jbcompose: String,
    val compose_compiler: String,
    val kodein:String,
    val koin:String
)

object Versions {


    private val vc_1_1 = VersionCombos(
        //*IMPORTANT*: when updating this, update it also in buildSrc/build.gradle.kts
        kotlin_version = "1.6.10",
        compose_core_libs = "1.1.1",
        compose_material = "1.1.1",
        compose_activity = "1.4.0",
        jbcompose = "1.1.1",
        compose_compiler = "1.1.1",
        kodein = "7.11.0",
        koin = "3.2.0"
    )

    private val vc_1_2 = VersionCombos(
        //*IMPORTANT*: when updating this, update it also in buildSrc/build.gradle.kts
        kotlin_version = "1.7.10",
        compose_core_libs = "1.2.0",
        compose_material = "1.2.1",
        compose_activity = "1.4.0",
        jbcompose = "1.2.0-alpha01-dev764",
        compose_compiler = "1.3.0",
        kodein="7.13.1", //for Compose Multiplatform 1.2.0-alpha01-dev745 with Kotlin 1.7 compatibility
        koin = "3.2.0" //actually this dependency is wrong it is for compose 1.1.1
    )
    //selected version combo
    private val combo= vc_1_2

    //see https://developer.android.com/jetpack/androidx/releases/compose-kotlin
    //see https://github.com/jimgoog/ComposeAppUsingPrereleaseComposeCompiler#project-configuration
    val compose_core_libs_version = combo.compose_core_libs //used in voyager:1.1.0

    // androidx.compose.material:material
    val compose_material_version = combo.compose_material

    //androidx.activity:activity-compose
    val compose_activity_version = combo.compose_activity
    //-----

    //see https://github.com/JetBrains/compose-jb/releases/latest for latest stable release
    //see https://github.com/JetBrains/compose-jb/releases for all releases
    val jbcompose_version = combo.jbcompose //latest stable composejb 1.1.1   latest dev 1.2.0-alpha01-dev755, used in voyager:1.0.1

    //compose compiler version can be set independently from compose version for using
    //kotlin to compose compatibility table:
    //compose 1.3.0	-> kotlin 1.7.10
    //compose 1.2.0	-> kotlin 1.7.0
    //compose 1.1.1	-> 1.6.10
    //compose 1.1.0 -> 1.6.10
    val compose_compiler_version = combo.compose_compiler //see // https://github.com/JetBrains/compose-jb/issues/2108

    val kotlin_version = combo.kotlin_version

    //*IMPORTANT*: when updating this, update it also in buildSrc/build.gradle.kts
    val android_gradle_plugin_version = "7.2.2"

    //https://github.com/Kotlin/kotlinx.coroutines/releases
    //org.jetbrains.kotlinx:kotlinx-coroutines-core
    //org.jetbrains.kotlinx:kotlinx-coroutines-test
    val coroutines_version = "1.6.4"

    //https://github.com/Kotlin/kotlinx-datetime
    //org.jetbrains.kotlinx:kotlinx-datetime
    val kotlinx_datetime_version = "0.4.0"

    //org.jetbrains.kotlinx:kotlinx-collections-immutable
    val kotlinx_collections_immutable_version = "0.3.5"

    val vanniktech_maven_publish_version = "0.18.0"

    // androidx.lifecycle:lifecycle-viewmodel-compose
    // androidx.lifecycle:lifecycle-runtime-ktx
    val androidx_lifecycle_version = "2.5.1"

    //androidx.core:core-ktx
    val androidx_corektx_version = "1.8.0"

    //https://github.com/kosi-libs/Kodein
    //https://github.com/kosi-libs/Kodein/releases#:~:text=Compare-,7.13.1,-Compose%20Multiplatform%201.2.0
    val kodein=combo.kodein

    //see https://github.com/InsertKoinIO/koin-compose
    val koin=combo.koin

    //https://github.com/google/dagger/releases
    val hilt= "2.43.2"

    //androidx.lifecycle:lifecycle-viewmodel-savedstate
    val lifecycle_savedState=""
    //--------------------------------------
    // *TEST* LIBRARIES
    //io.mockk:mockk
    val mockk_version = "1.12.4"
    val junit_version = "4.13.2"
    val assertj_version = "1.7.1"
}

object Deps {
    //    val support_annotations = "com.android.support:support-annotations:${Versions.support_lib}"
//    val support_appcompat_v7 = "com.android.support:appcompat-v7:${Versions.support_lib}"
    object Koin {
        val core = "io.insert-koin:koin-core:${Versions.koin}"
        val test = "io.insert-koin:koin-test:${Versions.koin}"
        val testJUnit4 = "io.insert-koin:koin-test-junit4:${Versions.koin}"
        val android = "io.insert-koin:koin-android:${Versions.koin}"
        val compose = "io.insert-koin:koin-androidx-compose:${Versions.koin}"
    }
}

object Plugins {
    val vanniktech_maven_publish =
        "com.vanniktech:gradle-maven-publish-plugin:${Versions.vanniktech_maven_publish_version}"
}