import appdependencies.ClassPath

// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://plugins.gradle.org/m2/") }

    }
    dependencies {
        classpath(appdependencies.ClassPath.gradle)
        classpath(appdependencies.ClassPath.kotlingradle)

        //classpath(appdependencies.ClassPath.google)
        classpath(appdependencies.ClassPath.navisafe)
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://www.jitpack.io") }
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
