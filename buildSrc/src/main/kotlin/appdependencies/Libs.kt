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

        // kotlin view model
        const val viewmodelKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}"
        // kotlin live data extensions
        const val livedataKtx = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifecycle}"
    }


    object Tests {
        //--- LEAK DETECTOR
        const val leakCanary = "com.squareup.leakcanary:leakcanary-android:${Versions.leakcanary}"

        const val junit = "junit:junit:${Versions.junit}"
        const val runner = "com.android.support.test:runner:${Versions.runner}"
        const val espresso = "com.android.support.test.espresso:espresso-core:${Versions.espresso}"
    }

}