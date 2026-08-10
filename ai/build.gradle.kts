plugins {
  id("ktx.base")
  id("ktx.publish")
}

dependencies {
  api(libs.gdx.ai)
  testImplementation(libs.gdx.backend.lwjgl)
  testImplementation(variantOf(libs.gdx.platform) { classifier("natives-desktop") })
}
