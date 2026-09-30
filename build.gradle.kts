import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
    alias(libs.plugins.android.library) apply false
}

subprojects {
    plugins.withId(rootProject.libs.plugins.kotlin.compose.get().pluginId) {
        extensions.configure<ComposeCompilerGradlePluginExtension> {
            stabilityConfigurationFiles.add(
                rootProject.layout.projectDirectory.file("stability_config.conf")
            )
            reportsDestination = layout.buildDirectory.dir("compose_compiler")
            metricsDestination = layout.buildDirectory.dir("compose_compiler")
        }
    }
}
