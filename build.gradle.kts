buildscript {
  repositories {
    mavenCentral()
    google()
  }
  dependencies {
    classpath("com.android.tools.build:gradle:7.1.0-alpha01")
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Deps.kotlin.version}")
    classpath("org.jetbrains.kotlin:kotlin-serialization:${Deps.kotlin.version}")
  }
}

plugins {
  id("com.github.ben-manes.versions") version "0.39.0"
}

allprojects {
  repositories {
    mavenCentral()
    google()
    maven { setUrl("https://jitpack.io") }
    maven { setUrl("https://oss.sonatype.org/content/repositories/snapshots/") }
  }
}

subprojects {
  tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile> {
    kotlinOptions {
      freeCompilerArgs = freeCompilerArgs + listOf(
        "-Xopt-in=kotlin.RequiresOptIn",
        "-Xuse-experimental=kotlin.ExperimentalStdlibApi",
        "-Xuse-experimental=kotlinx.coroutines.FlowPreview",
        "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi",
        "-Xuse-experimental=kotlinx.serialization.ExperimentalSerializationApi",
        "-Xuse-experimental=androidx.compose.foundation.ExperimentalFoundationApi"
      )
      jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
  }
  tasks.withType<Test> {
    useJUnitPlatform()
  }

  @Suppress("DEPRECATION")
  plugins.withType<com.android.build.gradle.BasePlugin> {
    configure<com.android.build.gradle.BaseExtension> {
      compileSdkVersion(Config.compileSdk)
      defaultConfig {
        minSdkVersion(Config.minSdk)
        targetSdkVersion(Config.targetSdk)
        versionCode(Config.versionCode)
        versionName(Config.versionName)
        ndk {
          version = Config.ndk
        }
      }
      compileOptions {
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
      }
      sourceSets["main"].java.srcDirs("src/main/kotlin")
    }
  }

  plugins.withType<JacocoPlugin> {
    configure<JacocoPluginExtension> {
      toolVersion = "0.8.7"
    }
  }

  afterEvaluate {
    tasks.withType<JacocoReport> {
      reports {
        xml.isEnabled = true
        html.isEnabled = false
      }
    }
  }
}

tasks.register("clean", Delete::class) {
  delete(rootProject.buildDir)
}
