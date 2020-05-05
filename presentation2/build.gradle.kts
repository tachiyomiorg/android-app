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

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
  kotlinOptions {
    jvmTarget = "1.8"
  }
  buildFeatures {
    compose = true
  }
  composeOptions {
    kotlinCompilerVersion = "1.3.70-dev-withExperimentalGoogleExtensions-20200424"
    kotlinCompilerExtensionVersion = "0.1.0-dev10"
  }
}

dependencies {
  implementationProject(Projects.core)
  implementationProject(Projects.coreUi)
  implementationProject(Projects.sourceApi)
  implementationProject(Projects.domain)

  implementation(Deps.androidX.design)
//  implementation(Deps.androidX.appCompat)
//  implementation(Deps.androidX.recyclerView)
//  implementation(Deps.androidX.preference)
//  implementation(Deps.androidX.card)
  implementation(Deps.androidX.emoji)
//  implementation(Deps.constraint)

  implementation(Deps.androidKTX)

  implementation(Deps.toothpick.runtime)
  implementation(Deps.toothpick.ktp)
  kapt(Deps.toothpick.compiler)

  implementation(Deps.coRedux)
  implementation(Deps.coReduxLog)
  implementation(Deps.materialDimens)
  implementation(Deps.materialDialog.core)
  implementation(Deps.materialDialog.input)

  implementation(Deps.glide.core)
  implementation(Deps.glide.okhttp)

  implementation(Deps.flexbox)

  implementation(Deps.compose.runtime)
  implementation(Deps.compose.framework)
  implementation(Deps.compose.layout)
  implementation(Deps.compose.material)
  implementation(Deps.compose.icons)
  implementation(Deps.compose.tooling)
  implementation(Deps.compose.graphics)
}
