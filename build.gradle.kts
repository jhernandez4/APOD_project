// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    // the plugin below isnt being recognized for some reason
    id("androidx.navigation.safeargs.kotlin") version "2.5.0" apply false
}