buildscript {
  repositories {
    google()
    jcenter()
  }
  dependencies {
    classpath("com.android.tools.build:gradle:7.0.0-alpha08")
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Deps.kotlin.version}")
    classpath("org.jetbrains.kotlin:kotlin-serialization:${Deps.kotlin.version}")
  }
}

plugins {
  id("com.github.ben-manes.versions") version "0.36.0"
}

allprojects {
  repositories {
    google()
    maven { setUrl("https://kotlin.bintray.com/kotlinx") }
    maven { setUrl("https://jitpack.io") }
    maven { setUrl("https://google.bintray.com/flexbox-layout") }
    jcenter()
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

        // For Jetpack Compose
        "-Xallow-jvm-ir-dependencies",
        "-Xskip-prerelease-check"
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
      toolVersion = "0.8.6"
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
