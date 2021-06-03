plugins {
    `kotlin-dsl`
}
/*repositories {
    jcenter()
}*/

repositories {
    // The org.jetbrains.kotlin.jvm plugin requires a repository
    // where to download the Kotlin compiler dependencies from.
    mavenCentral()
}

/*
kotlinDslPluginOptions {
    experimentalWarning.set(false)
}*/
