plugins {
  id("com.android.library")
  id("kotlin-android")
  id("kotlin-kapt")
}

dependencies {
  apiProject(Projects.common)

  implementation(Deps.duktape)
  implementation(Deps.androidX.core)
  implementation(Deps.androidX.sqlite)
  implementation(Deps.androidX.dataStore)
  implementation(Deps.coroutines.core)
  implementation(Deps.coroutines.android)
  implementation(Deps.flomo)
  implementation(Deps.lifecycle.common)
  implementation(Deps.lifecycle.process)
  implementation(Deps.lifecycle.extensions)

  implementation(Deps.toothpick.runtime)
  implementation(Deps.toothpick.ktp)
  kapt(Deps.toothpick.compiler)
}
