plugins {
  kotlin("jvm")
  id("com.jfrog.bintray")
  id("maven-publish")
}

dependencies {
  implementationProject(Projects.common)
}

val packageVersion = "1.1"

publishing {
  publications {
    create<MavenPublication>("publication") {
      from(components["java"])
      groupId = "tachiyomi.sourceapi"
      artifactId = "source-api"
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
    name = "source-api"
    vcsUrl = "https://github.com/tachiyomiorg/android-app"
    setLicenses("MPL-2.0")
    version = VersionConfig().apply {
      name = packageVersion
    }
  }
  setPublications("publication")
}
