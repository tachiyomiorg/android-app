plugins {
  id("java-library")
  id("kotlin")
  id("kotlin-kapt")
}

dependencies {
  api(Deps.kotlin.stdlib)
  api(Deps.kotlin.serialization)
  api(Deps.okhttp)
  api(Deps.jsoup)
  api(Deps.coroutines.core)

  implementation(Deps.tinylog.api)
  implementation(Deps.toothpick.runtime)
}
