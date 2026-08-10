plugins {
  id("ktx.base")
  id("ktx.publish")
}

dependencies {
  api(libs.artemis.odb)

  testImplementation(libs.spek.api)
  testImplementation(libs.assertj.core)

  testRuntimeOnly(libs.junit.platform.launcher)
  testRuntimeOnly(libs.spek.junit.platform.engine)
}

tasks.withType<Test> {
  testLogging {
    showExceptions = true
    events("FAILED", "SKIPPED")
  }

  useJUnitPlatform {
    includeEngines("spek")
  }
}
