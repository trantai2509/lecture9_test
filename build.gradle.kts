// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    // KSP: xu ly annotation cua Room luc bien dich
    alias(libs.plugins.ksp) apply false
}
