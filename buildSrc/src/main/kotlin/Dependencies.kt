
object Versions {
    //see https://developer.android.com/jetpack/androidx/releases/compose-kotlin
    //see https://github.com/jimgoog/ComposeAppUsingPrereleaseComposeCompiler#project-configuration
    val compose_version="1.2.0"

    val compose_activity_version="1.4.0"

    //see https://github.com/JetBrains/compose-jb/releases/latest for latest stable release
    //see https://github.com/JetBrains/compose-jb/releases for all releases
    val jbcompose_version="1.2.0-alpha01-dev755" //latest stable 1.1.1
    val compose_compiler_version="1.3.0-rc02"

    //*IMPORTANT*: when updating this, update it also in buildSrc/build.gradle.kts
    val kotlin_version="1.7.10"
    //*IMPORTANT*: when updating this, update it also in buildSrc/build.gradle.kts
    val android_gradle_plugin_version="7.2.2"

    val vanniktech_maven_publish_version="0.18.0"
}

object Libs {
//    val support_annotations = "com.android.support:support-annotations:${Versions.support_lib}"
//    val support_appcompat_v7 = "com.android.support:appcompat-v7:${Versions.support_lib}"
}
object Plugins {
    val vanniktech_maven_publish="com.vanniktech:gradle-maven-publish-plugin:${Versions.vanniktech_maven_publish_version}"
}