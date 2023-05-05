import com.android.build.gradle.internal.api.BaseVariantOutputImpl

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("androidx.navigation.safeargs.kotlin")
}

android {

    val buildSdkVersion: Int by extra
    val minSdkVersion: Int by extra
    val appVersion: String by extra
    val codePath: String by extra
    val resPath: String by extra

    compileSdk = buildSdkVersion
    defaultConfig {
        namespace = "com.rasalexman.erb"
        applicationId = "com.rasalexman.erb"
        minSdk = minSdkVersion
        targetSdk = buildSdkVersion
        version = appVersion
        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
            java.setSrcDirs(
                listOf(
                    "${buildDir.absolutePath}/generated/source/kaptKotlin/",
                    codePath
                )
            )
            res.setSrcDirs(listOf(resPath))
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

dependencies {

    implementation(fileTree(mapOf("include" to listOf("*.jar"), "dir" to "libs")))

    val viewpager2: String by rootProject.extra
    val recyclerview: String by rootProject.extra
    val coroutines: String by rootProject.extra
    val fragmentKtx: String by rootProject.extra
    val paging: String by rootProject.extra
    val coreKtx: String by rootProject.extra
    val constraintlayout: String by rootProject.extra
    val navigationFragment: String by rootProject.extra
    val navigationUI: String by rootProject.extra
    val material: String by rootProject.extra
    val livedataKtx: String by rootProject.extra
    val viewmodelKtx: String by rootProject.extra
    val leakCanary: String by rootProject.extra

    implementation(viewpager2)
    implementation(recyclerview)
    implementation(coroutines)
    implementation(fragmentKtx)
    implementation(paging)
    implementation(coreKtx)
    implementation(constraintlayout)
    implementation(navigationFragment)
    implementation(navigationUI)
    implementation(material)
    implementation(livedataKtx)
    implementation(viewmodelKtx)

    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01")

    implementation(project(":easy-recycler-binding"))

    debugImplementation(leakCanary)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}