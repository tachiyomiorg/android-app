@Suppress("ClassName", "MemberVisibilityCanBePrivate")
object Deps {

  object kotlin {
    const val version = "1.4.30"
    const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$version"
    const val reflect = "org.jetbrains.kotlin:kotlin-reflect:$version"

    object serialization {
      private const val version = "1.1.0"
      const val json = "org.jetbrains.kotlinx:kotlinx-serialization-json:$version"
      const val protobuf = "org.jetbrains.kotlinx:kotlinx-serialization-protobuf:$version"
    }
  }

  object coroutines {
    private const val version = "1.4.3"
    const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
    const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
  }

  object androidX {
    const val core = "androidx.core:core-ktx:1.5.0-beta02"
    const val appCompat = "androidx.appcompat:appcompat:1.3.0-beta01"
    const val browser = "androidx.browser:browser:1.3.0"
    const val preference = "androidx.preference:preference-ktx:1.1.1"
    const val sqlite = "androidx.sqlite:sqlite-ktx:2.1.0"
  }

  object compose {
    const val version = "1.0.0-beta01"
    const val compiler = "androidx.compose.compiler:compiler:$version"
    const val ui = "androidx.compose.ui:ui:$version"
    const val material = "androidx.compose.material:material:$version"
    const val icons = "androidx.compose.material:material-icons-extended:$version"
    const val tooling = "androidx.compose.ui:ui-tooling:$version"
    const val navigation = "androidx.navigation:navigation-compose:1.0.0-alpha08"
    const val constraintLayout = "androidx.constraintlayout:constraintlayout-compose:1.0.0-alpha03"
  }

  object lifecycle {
    private const val version = "2.3.0"
    const val common = "androidx.lifecycle:lifecycle-common-java8:$version"
    const val process = "androidx.lifecycle:lifecycle-process:$version"
    const val extensions = "androidx.lifecycle:lifecycle-runtime-ktx:$version"
  }

  object workManager {
    private const val version = "2.7.0-alpha01"
    const val runtime = "androidx.work:work-runtime-ktx:$version"
  }

  object room {
    private const val version = "2.3.0-beta01"
    const val runtime = "androidx.room:room-runtime:$version"
    const val ktx = "androidx.room:room-ktx:$version"
    const val compiler = "androidx.room:room-compiler:$version"
  }

  const val sqlite = "io.requery:sqlite-android:3.33.0"

  object toothpick {
    private const val version = "3.1.0"
    const val runtime = "com.github.stephanenicolas.toothpick:toothpick-runtime:$version"
    const val smoothie = "com.github.stephanenicolas.toothpick:smoothie:$version"
    const val compiler = "com.github.stephanenicolas.toothpick:toothpick-compiler:$version"
    const val ktp = "com.github.stephanenicolas.toothpick:ktp:$version"
    const val testing = "com.github.stephanenicolas.toothpick:toothpick-testing-junit5:$version"
  }

  const val okhttp = "com.squareup.okhttp3:okhttp:4.10.0-RC1"
  const val duktape = "com.squareup.duktape:duktape-android:1.3.0"
  const val jsoup = "org.jsoup:jsoup:1.13.1"

  const val flomo = "io.github.erikhuizinga:flomo:0.1.0-beta"

  object tinylog {
    private const val version = "2.2.0"
    const val impl = "org.tinylog:tinylog-impl:$version"
    const val api = "org.tinylog:tinylog-api:$version"
  }

  const val coil = "io.coil-kt:coil:1.0.0"

  const val mockk = "io.mockk:mockk:1.10.0"

  object kotest {
    private const val version = "4.3.1"
    const val framework = "io.kotest:kotest-runner-junit5-jvm:$version"
    const val assertions = "io.kotest:kotest-assertions-core-jvm:$version"
  }

}
