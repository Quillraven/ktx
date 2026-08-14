pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
  }
}

dependencyResolutionManagement {
  repositories {
    mavenLocal()
    mavenCentral()
    maven("https://central.sonatype.com/repository/maven-snapshots/")
    maven("https://jitpack.io/")
  }
}

includeBuild("build-logic")

include(
  "actors",
  "ai",
  "app",
  "artemis",
  "ashley",
  "assets",
  "assets-async",
  "async",
  "box2d",
  "collections",
  "json",
  "graphics",
  "freetype",
  "freetype-async",
  "i18n",
  "inject",
  "log",
  "math",
  "preferences",
  "reflect",
  "scene2d",
  "script",
  "style",
  "textratypist",
  "tiled",
  "vis",
  "vis-style"
)
