plugins {
  id("ktx.base")
  id("ktx.publish")
}

evaluationDependsOn(":scene2d")

val scene2d = project(":scene2d")

dependencies {
  api(libs.textra.typist)
  api(project(":scene2d"))

  testImplementation(scene2d.sourceSets.test.get().output)
  testImplementation(libs.gdx.backend.lwjgl)
  testImplementation(variantOf(libs.gdx.platform) { classifier("natives-desktop") })
  testImplementation(libs.vis.ui)
}
