plugins {
  id("ktx.base")
  id("ktx.publish")
}

dependencies {
  api(project(":style"))
  api(libs.vis.ui)
}
