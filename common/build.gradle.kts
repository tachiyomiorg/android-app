plugins {
  kotlin("jvm")
  id("kotlin-kapt")
  id("com.jfrog.bintray")
  id("maven-publish")
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
      groupId = "tachiyomi.sourceapi"
      artifactId = "common"
      version = packageVersion
      pom {
        name.set("Tachiyomi Common")
        description.set("Common classes for Tachiyomi.")
        url.set("https://github.com/tachiyomiorg/android-app")
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
          connection.set("scm:git:git:github.com:tachiyomiorg/android-app.git")
          developerConnection.set("scm:git:github.com:tachiyomiorg/android-app.git")
          url.set("https://github.com/tachiyomiorg/android-app")
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
    name = "common"
    vcsUrl = "https://github.com/tachiyomiorg/android-app"
    setLicenses("MPL-2.0")
    version = VersionConfig().apply {
      name = packageVersion
    }
  }
  setPublications("publication")
}
