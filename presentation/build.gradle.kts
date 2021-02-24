plugins {
  id("com.android.library")
  id("kotlin-android")
  id("kotlin-kapt")
}

android {
  kotlinOptions {
    jvmTarget = JavaVersion.VERSION_1_8.toString()
    useIR = true
  }
  buildFeatures {
    compose = true
  }
  composeOptions {
    kotlinCompilerVersion = "1.4.21"
    kotlinCompilerExtensionVersion = Deps.compose.version
  }
}

dependencies {
  implementationProject(Projects.core)
  implementationProject(Projects.sourceApi)
  implementationProject(Projects.domain)

  implementation(Deps.androidX.appCompat)
  implementation(Deps.androidX.browser)

  implementation(Deps.toothpick.runtime)
  implementation(Deps.toothpick.ktp)
  kapt(Deps.toothpick.compiler)

  implementation(Deps.compose.compiler)
  implementation(Deps.compose.ui)
  implementation(Deps.compose.tooling)
  implementation(Deps.compose.material)
  implementation(Deps.compose.icons)
  implementation(Deps.compose.navigation)
  implementation(Deps.compose.constraintLayout)

  implementation(Deps.coil)
}
