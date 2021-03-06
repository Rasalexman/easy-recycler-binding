import appdependencies.Versions
import resources.Resources.codeDirs

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("maven-publish")
}

android {
    compileSdkVersion(appdependencies.Builds.COMPILE_VERSION)
    defaultConfig {
        minSdkVersion(appdependencies.Builds.MIN_VERSION)
        targetSdkVersion(appdependencies.Builds.TARGET_VERSION)
        versionCode = appdependencies.Builds.ERB.VERSION_CODE
        versionName = appdependencies.Builds.ERB.VERSION_NAME
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

    /*dexOptions {
        javaMaxHeapSize = "4g"
    }*/

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    packagingOptions {
        exclude("META-INF/notice.txt")
        exclude("DebugProbesKt.bin")
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

    sourceSets {
        getByName("main") {
            java.setSrcDirs(codeDirs)
        }
    }

    kotlinOptions {
        languageVersion = "1.5"
        apiVersion = "1.5"
    }

    buildFeatures {
        dataBinding = true
    }
}

dependencies {
    implementation(fileTree(mapOf("include" to listOf("*.jar"), "dir" to "libs")))
    implementation(kotlin("stdlib-jdk8", Versions.kotlin))

    implementation(appdependencies.Libs.Core.viewPager2)
    implementation(appdependencies.Libs.Core.recyclerView)
    implementation(appdependencies.Libs.Core.coroutines)
    implementation(appdependencies.Libs.Core.fragment_ktx)
    implementation(appdependencies.Libs.Core.paging3)
}

group = "com.rasalexman.easyrecyclerbinding"
version = appdependencies.Builds.ERB.VERSION_NAME

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.register<Jar>(name = "sourceJar") {
    from(android.sourceSets["main"].java.srcDirs)
    archiveClassifier.set("sources")
}

afterEvaluate {

    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])

                // You can then customize attributes of the publication as shown below.
                groupId = "com.rasalexman.easyrecyclerbinding"
                artifactId = "easyrecyclerbinding"
                version = appdependencies.Builds.ERB.VERSION_NAME
                //artifact("$buildDir/outputs/aar/easy-recycler-binding-release.aar")
                artifact(tasks["sourceJar"])
            }
            create<MavenPublication>("debug") {
                from(components["debug"])

                // You can then customize attributes of the publication as shown below.
                groupId = "com.rasalexman.easyrecyclerbinding"
                artifactId = "easyrecyclerbinding-debug"
                version = appdependencies.Builds.ERB.VERSION_NAME
                //artifact("$buildDir/outputs/aar/easy-recycler-binding-debug.aar")
                artifact(tasks["sourceJar"])
            }
        }

        repositories {
            maven {
                name = "easyrecyclerbinding"
                url = uri("${buildDir}/publishing-repository")
            }
        }
    }
}
