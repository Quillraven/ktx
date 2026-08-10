plugins {
  distribution
  alias(libs.plugins.dokka)
  alias(libs.plugins.kotlin) apply false
  alias(libs.plugins.nexus.publish)
}

// Aggregates the Dokka documentation of all subprojects into a single multi-module HTML output.
dependencies {
  dokka(project(":actors"))
  dokka(project(":ai"))
  dokka(project(":app"))
  dokka(project(":artemis"))
  dokka(project(":ashley"))
  dokka(project(":assets"))
  dokka(project(":assets-async"))
  dokka(project(":async"))
  dokka(project(":box2d"))
  dokka(project(":collections"))
  dokka(project(":freetype"))
  dokka(project(":freetype-async"))
  dokka(project(":graphics"))
  dokka(project(":i18n"))
  dokka(project(":inject"))
  dokka(project(":json"))
  dokka(project(":log"))
  dokka(project(":math"))
  dokka(project(":preferences"))
  dokka(project(":reflect"))
  dokka(project(":scene2d"))
  dokka(project(":script"))
  dokka(project(":style"))
  dokka(project(":tiled"))
  dokka(project(":vis"))
  dokka(project(":vis-style"))
}

val libGroup: String by project
val ossrhUsername: String by project
val ossrhPassword: String by project

nexusPublishing {
  repositories {
    sonatype {
      username.set(ossrhUsername)
      password.set(ossrhPassword)
    }
  }
}

val linter = configurations.create("linter") {
  isCanBeConsumed = false
  isCanBeResolved = true
}

dependencies {
  linter(libs.ktlint.cli)
}

tasks.register<JavaExec>("linterIdeSetup") {
  mainClass.set("com.pinterest.ktlint.Main")
  description = "Apply Kotlin code style changes to IntelliJ formatter."
  group = "help"
  classpath = linter
  args = listOf("applyToIDEAProject")
}
