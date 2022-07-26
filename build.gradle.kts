buildscript {
    //see https://developer.android.com/jetpack/androidx/releases/compose-kotlin
    val compose_version by extra("1.1.1")
    val compose_activity by extra("1.5.0")
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        //kotlin version compatible with compose 1.1.1
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")
        classpath("com.android.tools.build:gradle:7.2.1")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}