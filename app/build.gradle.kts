/*apply plugin: 'com.android.application'
apply plugin: 'kotlin-android'
apply plugin: 'androidx.navigation.safeargs.kotlin'
apply plugin: 'kotlin-kapt'

android {
    compileSdkVersion 30
    buildToolsVersion "29.0.3"

    defaultConfig {
        applicationId "com.rasalexman.erb"
        minSdkVersion 21
        targetSdkVersion 30
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = '1.8'
    }

    buildFeatures{
        dataBinding = true
        viewBinding = true
    }
}*/

import appdependencies.Builds.APP_ID
import appdependencies.Builds.BUILD_TOOLS
import appdependencies.Builds.COMPILE_VERSION
import appdependencies.Builds.MIN_VERSION
import appdependencies.Builds.TARGET_VERSION
import appdependencies.Libs
import appdependencies.Versions
import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import resources.Resources.App.dirs
import resources.Resources.App.javaDirs

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    compileSdkVersion(COMPILE_VERSION)
    buildToolsVersion = BUILD_TOOLS
    defaultConfig {
        applicationId = APP_ID
        minSdkVersion(MIN_VERSION)
        targetSdkVersion(TARGET_VERSION)
        versionCode = appdependencies.Builds.App.VERSION_CODE
        versionName = appdependencies.Builds.App.VERSION_NAME
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            isDebuggable = true
        }

        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    sourceSets {
        getByName("main") {
            java.setSrcDirs(arrayListOf(
                    "${buildDir.absolutePath}/generated/source/kaptKotlin/",
                    "src/main/java"
            ))
            res.setSrcDirs(dirs)
        }
    }

    /*dexOptions {
        javaMaxHeapSize = "4g"
    }*/

    applicationVariants.forEach { variant ->
        variant.outputs.forEach { output ->
            val outputImpl = output as BaseVariantOutputImpl
            val project = project.name
            val sep = "_"
            val flavor = variant.flavorName
            val buildType = variant.buildType.name
            val version = variant.versionName

            val newApkName = "$project$sep$flavor$sep$buildType$sep$version.apk"
            outputImpl.outputFileName = newApkName
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    tasks.withType<KotlinCompile>().all {
        kotlinOptions.suppressWarnings = true
        kotlinOptions.jvmTarget = "1.8"
        kotlinOptions.noReflect = true
        kotlinOptions.freeCompilerArgs += listOf(
                "-XXLanguage:+InlineClasses"
        )
    }

    packagingOptions {
        exclude("META-INF/notice.txt")
    }

    // Declare the task that will monitor all configurations.
    configurations.all {
        // 2 Define the resolution strategy in case of conflicts.
        resolutionStrategy {
            // Fail eagerly on version conflict (includes transitive dependencies),
            // e.g., multiple different versions of the same dependency (group and name are equal).
            failOnVersionConflict()

            // Prefer modules that are part of this build (multi-project or composite build) over external modules.
            preferProjectModules()
        }
    }

    buildFeatures {
        dataBinding = true
    }
}

kapt {
    useBuildCache = true
}

dependencies {

    implementation(fileTree(mapOf("include" to listOf("*.jar"), "dir" to "libs")))
    implementation(kotlin("stdlib-jdk8", Versions.kotlin))

    //implementation(project(":easy-recycler-binding"))

    implementation(appdependencies.Libs.Core.coreKtx)
    implementation(appdependencies.Libs.Core.constraintlayout)
    implementation(appdependencies.Libs.Core.navigationFragmentKtx)
    implementation(appdependencies.Libs.Core.navigationUiKtx)
    implementation(appdependencies.Libs.Core.viewPager2)
    implementation(appdependencies.Libs.Core.material)
    implementation("com.github.rasalexman:easy-recycler-binding:0.0.3")

    /*implementation "org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version"
    implementation 'androidx.core:core-ktx:1.3.2'
    implementation 'androidx.appcompat:appcompat:1.2.0'
    implementation 'androidx.fragment:fragment-ktx:1.3.1'
    implementation 'androidx.navigation:navigation-fragment-ktx:2.3.4'
    implementation 'androidx.navigation:navigation-ui-ktx:2.3.4'
    implementation 'com.google.android.material:material:1.3.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.0.4'
    implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.3.0'
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.0'

    implementation project(':easy-recycler-binding')

    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.2'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.3.0'*/

    testImplementation(Libs.Tests.junit)
    androidTestImplementation(Libs.Tests.runner)
    androidTestImplementation(Libs.Tests.espresso)
}