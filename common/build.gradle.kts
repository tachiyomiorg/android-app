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
  implementation(Deps.commonsCodec)
}

val packageVersion = "1.1"

publishing {
  publications {
    create<MavenPublication>("publication") {
      from(components["java"])
      groupId = "tachiyomi.sourceapi"
      artifactId = "common"
      version = packageVersion
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
