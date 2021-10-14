package appdependencies

import appdependencies.Versions.appCoreX

object Libs {
    object Core {
        //const val appcompat = "androidx.appcompat:appcompat:$appCompatX"
        const val coreKtx = "androidx.core:core-ktx:$appCoreX"
        const val constraintlayout = "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}"
        const val navigationFragmentKtx = "androidx.navigation:navigation-fragment-ktx:${Versions.Navigation.fragment}"
        const val navigationUiKtx = "androidx.navigation:navigation-ui-ktx:${Versions.Navigation.ui}"
        const val material = "com.google.android.material:material:${Versions.material}"
        const val viewPager2 = "androidx.viewpager2:viewpager2:${Versions.viewPager2}"
        const val recyclerView = "androidx.recyclerview:recyclerview:${Versions.recyclerView}"
        const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutinesAndroid}"
        const val fragment_ktx = "androidx.fragment:fragment-ktx:${Versions.fragment}"
        const val paging3 = "androidx.paging:paging-runtime:${Versions.paging3}"

        // kotlin view model
        const val viewmodelKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}"
        // kotlin live data extensions
        const val livedataKtx = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifecycle}"
    }


    object Tests {
        //--- LEAK DETECTOR
        const val leakCanary = "com.squareup.leakcanary:leakcanary-android:${Versions.leakcanary}"

        const val junit = "junit:junit:${Versions.junit}"
        const val runner = "androidx.test:runner:${Versions.runner}"
        const val espresso = "androidx.test.espresso:espresso-core:${Versions.espresso}"
    }

}