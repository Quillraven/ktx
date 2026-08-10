plugins {
  id("ktx.base")
  id("ktx.publish")
}

dependencies {
  api(kotlin("scripting-jsr223"))

  testImplementation(libs.gdx.backend.lwjgl3)
  testImplementation(variantOf(libs.gdx.platform) { classifier("natives-desktop") })
}
