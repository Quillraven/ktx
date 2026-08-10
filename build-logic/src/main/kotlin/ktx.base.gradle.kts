import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.jvm.JvmTargetValidationMode
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  `java`
  jacoco
  id("org.jetbrains.kotlin.jvm")
}

val libs = the<VersionCatalogsExtension>().named("libs")
val libGroup: String by project
val projectName: String by project

group = libGroup
version = rootProject.file("version.txt").readText().trim()

base {
  archivesName.set(projectName)
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

kotlin {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_1_8)
    freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
  }
}

tasks.withType<KotlinCompile>().configureEach {
  jvmTargetValidationMode.set(JvmTargetValidationMode.IGNORE)
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_1_8)
  }
}

tasks.named<KotlinCompile>("compileTestKotlin") {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_11)
  }
}

val linter = configurations.create("linter") {
  isCanBeConsumed = false
  isCanBeResolved = true
}

dependencies {
  implementation(libs.findLibrary("kotlin-stdlib").get())
  implementation(libs.findLibrary("gdx").get())
  testImplementation(libs.findLibrary("kotlin-stdlib").get())
  testImplementation(libs.findLibrary("junit").get())
  testImplementation(libs.findLibrary("kotlintest").get())
  testImplementation(libs.findLibrary("mockito-kotlin").get())
  testImplementation(libs.findLibrary("kotlin-reflect").get())

  add("linter", libs.findLibrary("ktlint-cli").get())
}

tasks.register<JavaExec>("lint") {
  mainClass.set("com.pinterest.ktlint.Main")
  description = "Check Kotlin code style."
  group = "verification"
  classpath = linter
  args = listOf("src/**/*.kt")

  tasks["check"].dependsOn(this)
}

tasks.register<JavaExec>("format") {
  mainClass.set("com.pinterest.ktlint.Main")
  description = "Fix Kotlin code style."
  group = "formatting"
  classpath = linter
  args = listOf("-F", "src/**/*.kt")
}

tasks.withType<Test> {
  testLogging {
    events = setOf(TestLogEvent.FAILED, TestLogEvent.SKIPPED, TestLogEvent.STANDARD_OUT)
    exceptionFormat = TestExceptionFormat.FULL
    showExceptions = true
    showCauses = true
    showStackTraces = true

    debug {
      events = setOf(
        TestLogEvent.STARTED,
        TestLogEvent.FAILED,
        TestLogEvent.PASSED,
        TestLogEvent.SKIPPED,
        TestLogEvent.STANDARD_ERROR,
        TestLogEvent.STANDARD_OUT
      )
      exceptionFormat = TestExceptionFormat.FULL
    }

    info.events = debug.events
    info.exceptionFormat = debug.exceptionFormat
  }
}

tasks.named<Jar>("jar") {
  from(project.the<SourceSetContainer>().named("main").get().output)
  archiveBaseName.set(projectName)
}
