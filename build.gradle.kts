// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.momentwo.module.architecture)
    // The root checker shares the convention artifact; register sibling plugin versions here.
    alias(libs.plugins.momentwo.android.application) apply false
    alias(libs.plugins.momentwo.android.application.compose) apply false
    alias(libs.plugins.momentwo.android.library) apply false
    alias(libs.plugins.momentwo.android.library.compose) apply false
    alias(libs.plugins.momentwo.android.feature) apply false
    alias(libs.plugins.momentwo.android.hilt) apply false
    alias(libs.plugins.momentwo.android.network) apply false
    alias(libs.plugins.momentwo.android.room) apply false
    alias(libs.plugins.momentwo.jvm.library) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.androidRoom) apply false
}
true // Needed to make the Suppress annotation work for the plugins block
