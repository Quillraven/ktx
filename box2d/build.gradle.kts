plugins {
  id("ktx.base")
  id("ktx.publish")
}

dependencies {
  api(libs.gdx.box2d)
  testImplementation(variantOf(libs.gdx.box2d.platform) { classifier("natives-desktop") })
  testImplementation(variantOf(libs.gdx.platform) { classifier("natives-desktop") })
}
