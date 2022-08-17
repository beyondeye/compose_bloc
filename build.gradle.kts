buildscript {
    val compose_version by extra(Versions.compose_version)
    val compose_compiler_version by extra(Versions.compose_compiler_version)
    val compose_activity by extra(Versions.compose_activity_version)
    val jbcompose_version by extra(Versions.jbcompose_version)
    val kotlin_version by extra(Versions.kotlin_version)
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
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
        classpath("com.android.tools.build:gradle:${Versions.android_gradle_plugin_version}")
        //https://github.com/vanniktech/gradle-maven-publish-plugin
        // see also  https://proandroiddev.com/publishing-a-maven-artifact-3-3-step-by-step-instructions-to-mavencentral-publishing-bd661081645d
        classpath("com.vanniktech:gradle-maven-publish-plugin:${Versions.vanniktech_maven_publish_version}")
        classpath("org.jetbrains.compose:compose-gradle-plugin:${Versions.jbcompose_version}")
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