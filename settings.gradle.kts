rootProject.buildFileName = "build.gradle.kts"
include(":app", ":easy-recycler-binding")
rootProject.name = "EasyRecyclerBinding"

buildCache {
    local {
        directory = File(rootDir, "build-cache")
        removeUnusedEntriesAfterDays = 2
    }
}