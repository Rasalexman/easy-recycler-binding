import appdependencies.Builds.APP_ID
import appdependencies.Builds.COMPILE_VERSION
import appdependencies.Builds.MIN_VERSION
import appdependencies.Builds.TARGET_VERSION
import appdependencies.Libs
import appdependencies.Versions
import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import resources.Resources.App.dirs

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    compileSdk = COMPILE_VERSION
    defaultConfig {
        applicationId = APP_ID
        minSdk = MIN_VERSION
        targetSdk = TARGET_VERSION
        //versionCode = appdependencies.Builds.App.VERSION_CODE
        version = appdependencies.Builds.App.VERSION_NAME
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "1.8"
        languageVersion = "1.5"
        apiVersion = "1.5"
    }

    tasks.withType<KotlinCompile>().all {
        kotlinOptions.suppressWarnings = true
    }

    packagingOptions {
        resources.excludes.add("META-INF/notice.txt")
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

    implementation(Libs.Core.coreKtx)
    implementation(Libs.Core.fragment_ktx)
    implementation(Libs.Core.recyclerView)
    implementation(Libs.Core.constraintlayout)
    implementation(Libs.Core.navigationFragmentKtx)
    implementation(Libs.Core.navigationUiKtx)
    implementation(Libs.Core.viewPager2)
    implementation(Libs.Core.material)
    implementation(Libs.Core.livedataKtx)
    implementation(Libs.Core.viewmodelKtx)
    implementation(Libs.Core.paging3)
    //implementation("com.rasalexman.easyrecyclerbinding:easyrecyclerbinding:0.0.4")

    implementation(project(":easy-recycler-binding"))

    debugImplementation(Libs.Tests.leakCanary)
    testImplementation(Libs.Tests.junit)
    androidTestImplementation(Libs.Tests.runner)
    androidTestImplementation(Libs.Tests.espresso)
}