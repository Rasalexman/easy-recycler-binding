//------ APP VERSION
extra["appVersion"] = "1.3.2"

//------ CONFIG DATA
extra["minSdkVersion"] = 18
extra["buildSdkVersion"] = 33
extra["kotlinApiVersion"] = "1.7"
extra["jvmVersion"] = "11"
extra["agpVersion"] = "7.3.0"
extra["kotlinVersion"] = "1.7.20"
extra["jitpackPath"] = "https://jitpack.io"
extra["codePath"] = "src/main/kotlin"
extra["resPath"] = "src/main/res"

//------- LIBS VERSIONS
val navigation = "2.5.3"
val leakcanary = "2.9.1"
val coroutines = "1.6.4"
val core: String = "1.9.0"
val constraintLayout = "2.1.4"
val material = "1.7.0"
val viewPager2 = "1.1.0-beta01"
val recyclerView = "1.3.0-rc01"
val lifecycle = "2.5.1"
val fragment: String = "1.5.4"
val paging: String = "3.1.1"
extra["navigation"] = navigation

//------- Libs path
extra["leakCanary"] = "com.squareup.leakcanary:leakcanary-android:$leakcanary"
extra["coroutines"] = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines"
extra["coreKtx"] = "androidx.core:core-ktx:$core"
extra["constraintlayout"] = "androidx.constraintlayout:constraintlayout:$constraintLayout"
extra["navigationUI"] = "androidx.navigation:navigation-ui-ktx:$navigation"
extra["navigationFragment"] = "androidx.navigation:navigation-fragment-ktx:$navigation"
extra["material"] = "com.google.android.material:material:$material"
extra["viewpager2"] = "androidx.viewpager2:viewpager2:$viewPager2"
extra["recyclerview"] = "androidx.recyclerview:recyclerview:$recyclerView"
extra["fragmentKtx"] = "androidx.fragment:fragment-ktx:$fragment"
extra["paging"] = "androidx.paging:paging-runtime:$paging"
extra["viewmodelKtx"] = "androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle"
extra["livedataKtx"] = "androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle"
