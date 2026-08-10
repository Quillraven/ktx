plugins {
  id("ktx.base")
  id("ktx.publish")
}

dependencies {
  testImplementation(libs.gdx.backend.lwjgl)
  testImplementation(variantOf(libs.gdx.platform) { classifier("natives-desktop") })
}
