
object Versions {
    //see https://developer.android.com/jetpack/androidx/releases/compose-kotlin
    //see https://github.com/jimgoog/ComposeAppUsingPrereleaseComposeCompiler#project-configuration
    val compose_version="1.1.1" //used in voyager:1.1.0

    // androidx.compose.material:material
    val compose_material_version="1.2.1"

    //androidx.activity:activity-compose
    val compose_activity_version="1.4.0"


    //see https://github.com/JetBrains/compose-jb/releases/latest for latest stable release
    //see https://github.com/JetBrains/compose-jb/releases for all releases
    val jbcompose_version="1.1.1" //latest stable 1.1.1   latest dev 1.2.0-alpha01-dev755, used in voyager:1.0.1
    //compose compiler version can be set independently from compose version for using
    //kotlin to compose compatibility table:
    //compose 1.3.0	-> kotlin 1.7.10
    //compose 1.2.0	-> kotlin 1.7.0
    //compose 1.1.1	-> 1.6.10
    //compose 1.1.0 -> 1.6.10
    val compose_compiler_version="1.1.1"

    //*IMPORTANT*: when updating this, update it also in buildSrc/build.gradle.kts
    val kotlin_version="1.6.10"
    //*IMPORTANT*: when updating this, update it also in buildSrc/build.gradle.kts
    val android_gradle_plugin_version="7.2.2"

    //https://github.com/Kotlin/kotlinx.coroutines/releases
    //org.jetbrains.kotlinx:kotlinx-coroutines-core
    //org.jetbrains.kotlinx:kotlinx-coroutines-test
    val coroutines_version="1.6.4"

    //https://github.com/Kotlin/kotlinx-datetime
    //org.jetbrains.kotlinx:kotlinx-datetime
    val kotlinx_datetime_version="0.4.0"

    //org.jetbrains.kotlinx:kotlinx-collections-immutable
    val kotlinx_collections_immutable_version="0.3.5"

    val vanniktech_maven_publish_version="0.18.0"

    // androidx.lifecycle:lifecycle-viewmodel-compose
    // androidx.lifecycle:lifecycle-runtime-ktx
    val androidx_lifecycle_version="2.5.1"

    //androidx.core:core-ktx
    val androidx_corektx_version="1.8.0"


    //--------------------------------------
    // *TEST* LIBRARIES
    //io.mockk:mockk
    val mockk_version="1.12.4"
    val junit_version="4.13.2"
    val assertj_version="1.7.1"
}

object Libs {
//    val support_annotations = "com.android.support:support-annotations:${Versions.support_lib}"
//    val support_appcompat_v7 = "com.android.support:appcompat-v7:${Versions.support_lib}"
}
object Plugins {
    val vanniktech_maven_publish="com.vanniktech:gradle-maven-publish-plugin:${Versions.vanniktech_maven_publish_version}"
}