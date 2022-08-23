//*IMPORTANT*: when updating this, update it also in Dependencies.kt
val kotlin_version="1.7.10"
//*IMPORTANT*: when updating this, update it also in Dependencies.kt
val android_gradle_plugin_version="7.2.2"

plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
}
dependencies {
    implementation("com.android.tools.build:gradle:$android_gradle_plugin_version")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
}
