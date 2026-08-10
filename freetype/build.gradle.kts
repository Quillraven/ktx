plugins {
  id("ktx.base")
  id("ktx.publish")
}

dependencies {
  api(project(":assets"))
  api(libs.gdx.freetype)

  testImplementation(variantOf(libs.gdx.freetype.platform) { classifier("natives-desktop") })
  testImplementation(libs.gdx.backend.lwjgl)
  testImplementation(variantOf(libs.gdx.platform) { classifier("natives-desktop") })
}
