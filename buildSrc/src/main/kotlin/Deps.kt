@Suppress("ClassName", "MemberVisibilityCanBePrivate")
object Deps {

  object kotlin {
    const val version = "1.3.72"
    const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$version"
    const val serialization = "org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0"
  }

  object coroutines {
    private const val version = "1.3.5"
    const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
    const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
  }

  object androidX {
    const val core = "androidx.core:core-ktx:1.3.0-rc01"
    const val appCompat = "androidx.appcompat:appcompat:1.2.0-beta01"
    const val preference = "androidx.preference:preference:1.1.1"
    const val sqlite = "androidx.sqlite:sqlite:2.1.0"
  }

  object compose {
    const val version = "0.1.0-dev10"
    const val runtime = "androidx.compose:compose-runtime:$version"
    const val framework = "androidx.ui:ui-framework:$version"
    const val layout = "androidx.ui:ui-layout:$version"
    const val material = "androidx.ui:ui-material:$version"
    const val icons = "androidx.ui:ui-material-icons-extended:$version"
    const val tooling = "androidx.ui:ui-tooling:$version"
    const val graphics = "androidx.ui:ui-graphics:$version"
  }

  object lifecycle {
    private const val version = "2.2.0"
    const val extensions = "androidx.lifecycle:lifecycle-extensions:$version"
    const val runtime = "androidx.lifecycle:lifecycle-runtime:$version"
  }

  object workManager {
    private const val version = "2.3.4"
    const val runtime = "androidx.work:work-runtime-ktx:$version"
  }

  object room {
    private const val version = "2.2.5"
    const val runtime = "androidx.room:room-runtime:$version"
    const val ktx = "androidx.room:room-ktx:$version"
    const val compiler = "androidx.room:room-compiler:$version"
  }

  const val sqlite = "io.requery:sqlite-android:3.31.0"

  object toothpick {
    private const val version = "3.1.0"
    const val runtime = "com.github.stephanenicolas.toothpick:toothpick-runtime:$version"
    const val smoothie = "com.github.stephanenicolas.toothpick:smoothie:$version"
    const val compiler = "com.github.stephanenicolas.toothpick:toothpick-compiler:$version"
    const val ktp = "com.github.stephanenicolas.toothpick:ktp:$version"
    const val testing = "com.github.stephanenicolas.toothpick:toothpick-testing-junit5:$version"
  }

  const val okhttp = "com.squareup.okhttp3:okhttp:4.6.0"
  const val duktape = "com.squareup.duktape:duktape-android:1.3.0"
  const val kotson = "com.github.salomonbrys.kotson:kotson:2.5.0"
  const val jsoup = "org.jsoup:jsoup:1.13.1"

  const val flomo = "io.github.erikhuizinga:flomo:0.1.0-beta"

  object tinylog {
    private const val version = "2.1.2"
    const val impl = "org.tinylog:tinylog-impl:$version"
    const val api = "org.tinylog:tinylog-api:$version"
  }

  const val coRedux = "com.freeletics.coredux:core:1.1.1"
  const val coReduxLog = "com.freeletics.coredux:log-common:1.1.1"

  const val coil = "io.coil-kt:coil:0.10.1"

  const val mockk = "io.mockk:mockk:1.10.0"

  object kotest {
    private const val version = "4.0.5"
    const val framework = "io.kotest:kotest-runner-junit5-jvm:$version"
    const val assertions = "io.kotest:kotest-assertions-core-jvm:$version"
  }

}
