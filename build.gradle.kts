buildscript {
    //see https://developer.android.com/jetpack/androidx/releases/compose-kotlin
    //see https://github.com/jimgoog/ComposeAppUsingPrereleaseComposeCompiler#project-configuration
    val compose_version by extra("1.2.0")
    val compose_compiler_version by extra("1.3.0-rc02")
    val compose_activity by extra("1.5.1")
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven(url ="https://androidx.dev/storage/compose-compiler/repository/")
    }
    dependencies {
        //kotlin version compatible with compose compiler version 1.3.0-rc02
        // see https://developer.android.com/jetpack/androidx/releases/compose-kotlin
        // see https://android-developers.googleblog.com/search/label/Compose
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.10")
        classpath("com.android.tools.build:gradle:7.2.1")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven(url ="https://androidx.dev/storage/compose-compiler/repository/")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}