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
        classpath("com.android.tools.build:gradle:7.2.2")
        //https://github.com/vanniktech/gradle-maven-publish-plugin
        // see also  https://proandroiddev.com/publishing-a-maven-artifact-3-3-step-by-step-instructions-to-mavencentral-publishing-bd661081645d
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.18.0")
    }
}

subprojects {
    repositories {
        google()
        mavenCentral()
        maven(url ="https://androidx.dev/storage/compose-compiler/repository/")
    }
//    plugins.withId("com.vanniktech.maven.publish") {
//        configure<com.vanniktech.maven.publish.MavenPublishPluginExtension> {
//            sonatypeHost = com.vanniktech.maven.publish.SonatypeHost.S01
//        }
//    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}