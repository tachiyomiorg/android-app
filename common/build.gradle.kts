plugins {
  id("java-library")
  id("kotlin")
  id("kotlin-kapt")
  id("com.jfrog.bintray")
  id("maven-publish")
}

dependencies {
  api(Deps.kotlin.stdlib)
  api(Deps.kotlin.serialization.runtime)
  api(Deps.okhttp)
  api(Deps.jsoup)
  api(Deps.coroutines.core)

  implementation(Deps.tinylog.api)
  implementation(Deps.tinylog.impl)
  implementation(Deps.toothpick.runtime)
}

publishing {
  publications {
    create<MavenPublication>("publication") {
      from(components["java"])
      groupId = "tachiyomi.sourceapi"
      artifactId = "common"
      version = "1.1"
    }
  }
}

bintray {
  user = System.getenv("BINTRAY_USER")
  key = System.getenv("BINTRAY_KEY")
  pkg = PackageConfig().apply {
    repo = "maven"
    name = "common"
    vcsUrl = "https://github.com/tachiyomiorg/android-app"
    setLicenses("Apache-2.0")
    version = VersionConfig().apply {
      name = "1.1"
    }
  }
  setPublications("publication")
}
