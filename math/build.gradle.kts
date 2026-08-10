plugins {
  id("ktx.base")
  id("ktx.publish")
}

dependencies {
  testImplementation(variantOf(libs.gdx.platform) { classifier("natives-desktop") })
}
