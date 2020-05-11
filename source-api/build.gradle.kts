plugins {
  id("java-library")
  id("kotlin")
  id("com.jfrog.bintray")
  id("maven-publish")
}

dependencies {
  implementationProject(Projects.common)
}

publishing {
  publications {
    create<MavenPublication>("publication") {
      from(components["java"])
      groupId = "tachiyomi.sourceapi"
      artifactId = "source-api"
      version = "1.1"
    }
  }
}

bintray {
  user = System.getenv("BINTRAY_USER")
  key = System.getenv("BINTRAY_KEY")
  pkg = PackageConfig().apply {
    repo = "maven"
    name = "source-api"
    vcsUrl = "https://github.com/tachiyomiorg/android-app"
    setLicenses("Apache-2.0")
    version = VersionConfig().apply {
      name = "1.1"
    }
  }
  setPublications("publication")
}
