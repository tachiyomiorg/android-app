buildscript {
  repositories {
    google()
    jcenter()
  }
  dependencies {
    classpath("com.android.tools.build:gradle:4.2.0-alpha01")
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Deps.kotlin.version}")
    classpath("org.jetbrains.kotlin:kotlin-serialization:${Deps.kotlin.version}")
    classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.5")
  }
}

plugins {
  id("com.github.ben-manes.versions") version "0.28.0"
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
        "-Xuse-experimental=kotlinx.coroutines.FlowPreview",
        "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi",
        "-Xuse-experimental=kotlinx.serialization.ImplicitReflectionSerializer"
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
      }
      compileOptions {
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
      }
      sourceSets["main"].java.srcDirs("src/main/kotlin")
    }
  }
}

tasks.register("clean", Delete::class) {
  delete(rootProject.buildDir)
}
