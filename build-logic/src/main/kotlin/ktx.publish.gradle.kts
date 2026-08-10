import org.gradle.api.distribution.DistributionContainer

plugins {
  signing
  id("com.vanniktech.maven.publish")
  id("org.jetbrains.dokka")
}

val projectName = project.property("projectName") as String
val projectDesc = project.property("projectDesc") as String

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

mavenPublishing {
  publishToMavenCentral(automaticRelease = true)

  signAllPublications()

  coordinates(project.property("libGroup") as String, projectName, version.toString())

  pom {
    name.set(projectName)
    description.set(projectDesc)
    url.set("https://github.com/Quillraven/ktx/")

    licenses {
      license {
        name.set("CC0-1.0")
        url.set("https://creativecommons.org/publicdomain/zero/1.0/")
      }
    }

    scm {
      connection.set("scm:git:git@github.com:Quillraven/ktx.git")
      developerConnection.set("scm:git:git@github.com:Quillraven/ktx.git")
      url.set("https://github.com/Quillraven/ktx/")
    }

    developers {
      developer {
        id.set("Quillraven")
        name.set("Simon Klausner")
      }
    }
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
