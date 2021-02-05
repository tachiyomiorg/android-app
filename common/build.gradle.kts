plugins {
  kotlin("jvm")
  id("kotlin-kapt")
  `maven-publish`
  signing
}

dependencies {
  api(Deps.kotlin.stdlib)
  api(Deps.kotlin.serialization.json)
  api(Deps.okhttp)
  api(Deps.jsoup)
  api(Deps.coroutines.core)

  implementation(Deps.tinylog.api)
  implementation(Deps.tinylog.impl)
  implementation(Deps.toothpick.runtime)
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
      groupId = "org.tachiyomi"
      artifactId = "common"
      version = packageVersion
      pom {
        name.set("Tachiyomi Common")
        description.set("Common classes for Tachiyomi.")
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
  repositories {
    maven {
      val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2"
      val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots"
      url = uri(if (packageVersion.endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)

      credentials {
        username = System.getenv("SONATYPE_USER")
        password = System.getenv("SONATYPE_PASS")
      }
    }
  }
}

signing {
  sign(publishing.publications["publication"])
}
