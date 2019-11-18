buildscript {
  repositories {
    google()
    jcenter()
  }
  dependencies {
    classpath("com.android.tools.build:gradle:4.0.0-alpha03")
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Deps.kotlin.version}")
    classpath("org.jetbrains.kotlin:kotlin-serialization:${Deps.kotlin.version}")
  }
}

plugins {
  id("com.github.ben-manes.versions") version "0.22.0"
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

tasks.register("clean", Delete::class) {
  delete(rootProject.buildDir)
}

subprojects {
  tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile> {
    kotlinOptions {
      freeCompilerArgs += "-Xuse-experimental=kotlinx.coroutines.FlowPreview"
      freeCompilerArgs += "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi"
      freeCompilerArgs += "-Xuse-experimental=kotlinx.coroutines.InternalCoroutinesApi"
      freeCompilerArgs += "-Xuse-experimental=kotlinx.serialization.ImplicitReflectionSerializer"
    }
  }
}
