plugins {
  id("ktx.base")
  id("ktx.publish")
}

evaluationDependsOn(":scene2d")

val scene2d = project(":scene2d")

dependencies {
  api(scene2d)
  api(libs.vis.ui)

  testImplementation(scene2d.sourceSets.test.get().output)
  testImplementation(libs.gdx.backend.lwjgl)
  testImplementation(variantOf(libs.gdx.platform) { classifier("natives-desktop") })
}
