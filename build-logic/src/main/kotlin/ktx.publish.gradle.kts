import org.gradle.api.distribution.DistributionContainer
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.plugins.signing.Sign

plugins {
  `maven-publish`
  signing
  id("org.jetbrains.dokka")
}

val projectName = project.property("projectName") as String
val projectDesc = project.property("projectDesc") as String
val ossrhUsername = project.property("ossrhUsername") as String
val ossrhPassword = project.property("ossrhPassword") as String

dokka {
  dokkaPublications.html {
    outputDirectory.set(layout.buildDirectory.dir("dokka/html"))
  }
}

tasks.register<Zip>("dokkaZip") {
  description = "Create a ZIP archive of the generated Dokka HTML documentation."
  from(layout.buildDirectory.dir("dokka/html"))
  dependsOn(tasks.named("dokkaGeneratePublicationHtml"))
}

val javadocJar = tasks.register<Jar>("javadocJar") {
  description = "Create a JAR archive of the Dokka-generated Javadoc documentation."
  archiveClassifier.set("javadoc")
  from(layout.buildDirectory.dir("dokka/html"))
  dependsOn(tasks.named("dokkaGeneratePublicationHtml"))
}

val sourcesJar = tasks.register<Jar>("sourcesJar") {
  description = "Create a JAR archive of the source code."
  from(project.the<SourceSetContainer>().named("main").get().allSource)
  archiveClassifier.set("sources")
}

configure<PublishingExtension> {
  repositories {
    maven {
      name = "maven"
      val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
      val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots/")
      url = if (project.version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl

      credentials {
        username = ossrhUsername
        password = ossrhPassword
      }
    }
  }

  publications {
    create<MavenPublication>("mavenKtx") {
      artifactId = projectName
      from(components["kotlin"])
      artifact(sourcesJar)
      artifact(javadocJar)

      pom {
        name.set(projectName)
        description.set(projectDesc)
        url.set("https://libktx.github.io/")

        licenses {
          license {
            name.set("CC0-1.0")
            url.set("https://creativecommons.org/publicdomain/zero/1.0/")
          }
        }

        scm {
          connection.set("scm:git:git@github.com:libktx/ktx.git")
          developerConnection.set("scm:git:git@github.com:libktx/ktx.git")
          url.set("https://github.com/libktx/ktx/")
        }

        developers {
          developer {
            id.set("mj")
            name.set("MJ")
          }
        }
      }
    }
  }
}

val isReleaseVersion = !project.version.toString().endsWith("SNAPSHOT")

tasks.withType<Sign> {
  onlyIf { isReleaseVersion }
}

signing {
  setRequired { isReleaseVersion && gradle.taskGraph.hasTask("publish") }
  sign(extensions.getByType(PublishingExtension::class.java).publications["mavenKtx"])
}

tasks.register("uploadSnapshot") {
  description = "Upload a SNAPSHOT version of the library to the Maven repository."
  if (!isReleaseVersion) {
    finalizedBy(tasks.named("publishAllPublicationsToMavenRepository"))
  }
}

afterEvaluate {
  rootProject.extensions.getByType(DistributionContainer::class.java).named("main") {
    distributionBaseName.set(project.version.toString())
    contents {
      into("lib") {
        from(tasks.named("jar"))
      }
      into("doc") {
        from(tasks.named("dokkaZip"))
      }
      into("src") {
        from(tasks.named("sourcesJar"))
      }
    }
  }
}

