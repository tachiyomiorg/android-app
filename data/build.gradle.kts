plugins {
  id("com.android.library")
  id("kotlin-android")
  id("kotlin-kapt")
}

android {
  compileSdkVersion(Config.compileSdk)
  defaultConfig {
    minSdkVersion(Config.minSdk)
    targetSdkVersion(Config.targetSdk)
  }
  sourceSets["main"].java.srcDirs("src/main/kotlin")
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
  implementation(Deps.kotson)
  implementation(Deps.workManager.runtime)

  implementation(Deps.toothpick.runtime)
  implementation(Deps.toothpick.smoothie)
  implementation(Deps.toothpick.ktp)
  kapt(Deps.toothpick.compiler)

}
