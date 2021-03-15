plugins {
  id("com.android.library")
  id("kotlin-android")
  id("kotlin-kapt")
}

val removeDomainClasses by tasks.registering(Delete::class) {
  doFirst {
    delete(fileTree("$buildDir/tmp/kotlin-classes") {
      include("*/tachiyomi/domain/**/*.class")
    })
  }
}
tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java) {
  finalizedBy(removeDomainClasses)
}

android {
  kapt {
    arguments {
      arg("room.schemaLocation", "$projectDir/schemas")
    }
  }
}

dependencies {
  implementationProject(Projects.core)
  implementationProject(Projects.domain)
  implementationProject(Projects.sourceApi)

  implementation(Deps.room.runtime)
  implementation(Deps.room.ktx)
  kapt(Deps.room.compiler)
  implementation(Deps.sqlite)
  implementation(Deps.coroutines.core)
  implementation(Deps.coroutines.android)
  implementation(Deps.workManager.runtime)
  implementation(Deps.kotson)

  implementation(Deps.toothpick.runtime)
  implementation(Deps.toothpick.smoothie)
  implementation(Deps.toothpick.ktp)
  kapt(Deps.toothpick.compiler)
}
