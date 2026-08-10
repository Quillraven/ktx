plugins {
  id("ktx.base")
  id("ktx.publish")
}

dependencies {
  api(libs.kotlinx.coroutines.core)

  testImplementation(libs.gdx.backend.headless)
  testImplementation(libs.gdx.backend.lwjgl)
  testImplementation(variantOf(libs.gdx.platform) { classifier("natives-desktop") })
  testImplementation(libs.free.port.finder)
  testImplementation(libs.wiremock)
  testImplementation(libs.slf4j.nop)
}
