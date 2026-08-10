plugins {
  id("ktx.base")
  id("ktx.publish")
}

evaluationDependsOn(":async")

val async = project(":async")

dependencies {
  api(project(":assets"))
  api(async)
  api(libs.kotlinx.coroutines.core)

  testImplementation(async.sourceSets.test.get().output)
  testImplementation(libs.kotlinx.coroutines.jdk8)
  testImplementation(libs.gdx.backend.headless)
  testImplementation(libs.gdx.backend.lwjgl3)
  testImplementation(variantOf(libs.gdx.platform) { classifier("natives-desktop") })
}

