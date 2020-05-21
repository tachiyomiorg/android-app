plugins {
  id("java-library")
  id("kotlin")
  id("kotlin-kapt")
  id("kotlinx-serialization")
}

dependencies {
  implementationProject(Projects.common)
  implementationProject(Projects.sourceApi)

  implementation(Deps.toothpick.runtime)
  kapt(Deps.toothpick.compiler)

  implementation(Deps.kotlin.serialization.protobuf)

  testImplementation(Deps.mockk)
  testImplementation(Deps.toothpick.testing)
  testImplementation(Deps.kotest.framework)
  testImplementation(Deps.kotest.assertions)
  kaptTest(Deps.toothpick.compiler)
}
