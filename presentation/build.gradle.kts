plugins {
  id("com.android.library")
  id("kotlin-android")
  id("kotlin-kapt")
}

android {
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
  implementationProject(Projects.sourceApi)
  implementationProject(Projects.domain)

  implementation(Deps.androidX.appCompat)

  implementation(Deps.toothpick.runtime)
  implementation(Deps.toothpick.ktp)
  kapt(Deps.toothpick.compiler)

  implementation(Deps.coRedux)
  implementation(Deps.coReduxLog)

  implementation(Deps.compose.runtime)
  implementation(Deps.compose.layout)
  implementation(Deps.compose.material)
  implementation(Deps.compose.icons)
  implementation(Deps.compose.tooling)
  implementation(Deps.compose.graphics)

  implementation(Deps.coil)
}
