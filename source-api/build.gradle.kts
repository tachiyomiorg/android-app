plugins {
  kotlin("jvm")
  id("com.jfrog.bintray")
  id("maven-publish")
}

dependencies {
  implementationProject(Projects.common)
}

val packageVersion = "1.1"

java {
  withJavadocJar()
  withSourcesJar()
}

publishing {
  publications {
    create<MavenPublication>("publication") {
      from(components["java"])
      groupId = "tachiyomi.sourceapi"
      artifactId = "source-api"
      version = packageVersion
      pom {
        name.set("Tachiyomi Source API")
        description.set("Core source API for Tachiyomi.")
        url.set("https://github.com/tachiyomiorg/tachiyomi-1.x")
        licenses {
          license {
            name.set("Mozilla Public License 2.0")
            url.set("https://www.mozilla.org/en-US/MPL/2.0/")
          }
        }
        developers {
          developer {
            id.set("inorichi")
            name.set("Javier Tomás")
            email.set("len@kanade.eu")
          }
        }
        scm {
          connection.set("scm:git:git:github.com:tachiyomiorg/tachiyomi-1.x.git")
          developerConnection.set("scm:git:github.com:tachiyomiorg/tachiyomi-1.x.git")
          url.set("https://github.com/tachiyomiorg/tachiyomi-1.x")
        }
      }
    }
  }
}

bintray {
  user = System.getenv("BINTRAY_USER")
  key = System.getenv("BINTRAY_KEY")
  pkg = PackageConfig().apply {
    userOrg = "tachiyomiorg"
    repo = "maven"
    name = "source-api"
    vcsUrl = "https://github.com/tachiyomiorg/tachiyomi-1.x"
    setLicenses("MPL-2.0")
    version = VersionConfig().apply {
      name = packageVersion
    }
  }
  setPublications("publication")
}
