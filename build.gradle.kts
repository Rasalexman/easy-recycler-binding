import appdependencies.ClassPath

// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://plugins.gradle.org/m2/") }

    }
    dependencies {
        classpath(ClassPath.gradle)
        classpath(ClassPath.kotlingradle)

        classpath(ClassPath.google)
        classpath(ClassPath.navisafe)
        classpath(ClassPath.mavenplugin)
        //classpath(appdependencies.ClassPath.dokkaplugin)
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
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
