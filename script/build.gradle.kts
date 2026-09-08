plugins {
  id("ktx.base")
  id("ktx.publish")
}

dependencies {
  api(libs.kotlin.scripting.common)
  api(libs.kotlin.scripting.jvm)
  api(libs.kotlin.scripting.jvm.host)
  api(libs.kotlin.scripting.compiler.embeddable)
  api(libs.kotlinx.coroutines.core)

  testImplementation(libs.gdx.backend.lwjgl3)
  testImplementation(variantOf(libs.gdx.platform) { classifier("natives-desktop") })
}
