plugins {
  `kotlin-dsl`
}

dependencies {
  implementation(libs.plugins.kotlin.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" })
  implementation(libs.plugins.dokka.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" })
}
