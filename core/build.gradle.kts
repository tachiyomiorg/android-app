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
  apiProject(Projects.common)

  implementation(Deps.duktape)
  implementation(Deps.androidX.core)
  implementation(Deps.androidX.sqlite)
  implementation(Deps.androidX.preference)
  implementation(Deps.coroutines.core)
  implementation(Deps.coroutines.android)
  implementation(Deps.flomo)
  implementation(Deps.lifecycle.extensions)

  implementation(Deps.toothpick.runtime)
  implementation(Deps.toothpick.ktp)
  kapt(Deps.toothpick.compiler)
}
