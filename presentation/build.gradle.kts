plugins {
  id("com.android.library")
  id("kotlin-android")
  id("kotlin-android-extensions")
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
  implementationProject(Projects.coreUi)
  implementationProject(Projects.sourceApi)
  implementationProject(Projects.domain)
  implementationProject(Projects.data) // TODO remove data dependency

  implementation(Deps.androidX.design)
  implementation(Deps.androidX.appCompat)
  implementation(Deps.androidX.recyclerView)
  implementation(Deps.androidX.preference)
  implementation(Deps.androidX.card)
  implementation(Deps.androidX.emoji)
  implementation(Deps.constraint)

  implementation(Deps.androidKTX)

  implementation(Deps.conductor)
  implementation(Deps.conductorPreference)

  implementation(Deps.toothpick.runtime)
  implementation(Deps.toothpick.ktp)
  kapt(Deps.toothpick.compiler)

  implementation(Deps.coRedux)
  implementation(Deps.coReduxLog)
  implementation(Deps.materialDimens)
  implementation(Deps.materialDialog.core)
  implementation(Deps.materialDialog.input)
  implementation(Deps.cyanea)

  implementation(Deps.glide.core)
  implementation(Deps.glide.okhttp)

  implementation(Deps.flexbox)
}

androidExtensions {
  isExperimental = true
}
