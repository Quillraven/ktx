plugins {
  id("ktx.base")
  id("ktx.publish")
}

evaluationDependsOn(":async")
evaluationDependsOn(":freetype")

val async = project(":async")
val freetype = project(":freetype")

dependencies {
  api(project(":assets-async"))
  api(project(":freetype"))
  api(libs.gdx.freetype)
  api(libs.kotlinx.coroutines.core)

  testImplementation(async.sourceSets.test.get().output)
  testImplementation(freetype.sourceSets.test.get().output)
  testImplementation(libs.kotlinx.coroutines.jdk8)
  testImplementation(variantOf(libs.gdx.freetype.platform) { classifier("natives-desktop") })
  testImplementation(libs.gdx.backend.headless)
  testImplementation(libs.gdx.backend.lwjgl)
  testImplementation(variantOf(libs.gdx.platform) { classifier("natives-desktop") })
}
