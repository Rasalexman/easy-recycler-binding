plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("maven-publish")
}

val appVersion: String by rootProject.extra
group = "com.rasalexman.easyrecyclerbinding"
version = appVersion

android {
    val buildSdkVersion: Int by extra
    val minSdkVersion: Int by extra
    val appVersion: String by extra
    val kotlinApiVersion: String by extra
    val jvmVersion: String by extra

    compileSdk = buildSdkVersion

    defaultConfig {
        namespace = "com.rasalexman.easyrecyclerbinding"
        minSdk = minSdkVersion
        targetSdk = buildSdkVersion
        version = appVersion

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    /*dexOptions {
        javaMaxHeapSize = "4g"
    }*/

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    packagingOptions {
        resources.excludes.add("META-INF/notice.txt")
        resources.excludes.add("DebugProbesKt.bin")
    }

    // Declare the task that will monitor all configurations.
    /*configurations.all {
        // 2 Define the resolution strategy in case of conflicts.
        resolutionStrategy {
            // Fail eagerly on version conflict (includes transitive dependencies),
            // e.g., multiple different versions of the same dependency (group and name are equal).
            failOnVersionConflict()

            // Prefer modules that are part of this build (multi-project or composite build) over external modules.
            preferProjectModules()
        }
    }*/

    val codePath: String by rootProject.extra
    sourceSets {
        getByName("main") {
            java.setSrcDirs(listOf(codePath))
        }
    }

    buildFeatures {
        dataBinding = true
    }

    kotlinOptions {
        apiVersion = kotlinApiVersion
        languageVersion = kotlinApiVersion
        jvmTarget = jvmVersion
    }
}

dependencies {
    val viewpager2: String by rootProject.extra
    val recyclerview: String by rootProject.extra
    val coroutines: String by rootProject.extra
    val fragmentKtx: String by rootProject.extra
    val paging: String by rootProject.extra
    val coreKtx: String by rootProject.extra

    compileOnly(viewpager2)
    compileOnly(recyclerview)
    implementation(coroutines)
    compileOnly(fragmentKtx)
    compileOnly(paging)
    compileOnly(coreKtx)
}

tasks.register<Jar>(name = "sourceJar") {
    from(android.sourceSets["main"].java.srcDirs)
    archiveClassifier.set("sources")
}

java {
    sourceSets {
        create("main") {
            java.setSrcDirs(android.sourceSets["main"].java.srcDirs)
        }
    }

    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11

    withSourcesJar()
    //withJavadocJar()
}

afterEvaluate {

    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])

                // You can then customize attributes of the publication as shown below.
                groupId = "com.rasalexman.easyrecyclerbinding"
                artifactId = "easyrecyclerbinding"
                version = appVersion

                //artifact(tasks["sourceJar"])
            }
            create<MavenPublication>("debug") {
                from(components["debug"])

                // You can then customize attributes of the publication as shown below.
                groupId = "com.rasalexman.easyrecyclerbinding"
                artifactId = "easyrecyclerbinding-debug"
                version = appVersion

                //artifact(tasks["sourceJar"])
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
