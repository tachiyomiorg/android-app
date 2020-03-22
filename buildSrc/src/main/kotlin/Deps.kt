object Deps {

  object kotlin {
    const val version = "1.3.70"
    const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$version"
    const val serialization = "org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0"
  }

  object coroutines {
    private const val version = "1.3.5"
    const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
    const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
  }

  object androidX {
    const val core = "androidx.core:core:1.0.0"
    const val design = "com.google.android.material:material:1.0.0"
    const val appCompat = "androidx.appcompat:appcompat:1.0.0"
    const val recyclerView = "androidx.recyclerview:recyclerview:1.0.0"
    const val preference = "androidx.preference:preference:1.0.0"
    const val card = "androidx.cardview:cardview:1.0.0"
    const val emoji = "androidx.emoji:emoji-bundled:1.0.0"
    const val sqlite = "androidx.sqlite:sqlite:2.0.1"
  }

  object lifecycle {
    private const val version = "2.0.0"
    const val extensions = "androidx.lifecycle:lifecycle-extensions:$version"
    const val runtime = "androidx.lifecycle:lifecycle-runtime:$version"
  }

  const val androidKTX = "androidx.core:core-ktx:1.0.1"

  object workManager {
    private const val version = "2.2.0-beta01"
    const val runtime = "androidx.work:work-runtime-ktx:$version"
  }

  object room {
    private const val version = "2.2.2"
    const val runtime = "androidx.room:room-runtime:$version"
    const val ktx = "androidx.room:room-ktx:$version"
    const val compiler = "androidx.room:room-compiler:$version"
  }

  const val sqlite = "io.requery:sqlite-android:3.30.1"

  object toothpick {
    private const val version = "3.1.0"
    const val runtime = "com.github.stephanenicolas.toothpick:toothpick-runtime:$version"
    const val smoothie = "com.github.stephanenicolas.toothpick:smoothie:$version"
    const val compiler = "com.github.stephanenicolas.toothpick:toothpick-compiler:$version"
    const val ktp = "com.github.stephanenicolas.toothpick:ktp:$version"
    const val testing = "com.github.stephanenicolas.toothpick:toothpick-testing-junit5:$version"
  }

  const val okhttp = "com.squareup.okhttp3:okhttp:4.2.2"
  const val duktape = "com.squareup.duktape:duktape-android:1.3.0"
  const val kotson = "com.github.salomonbrys.kotson:kotson:2.5.0"
  const val jsoup = "org.jsoup:jsoup:1.12.1"

  const val flomo = "io.github.erikhuizinga:flomo:0.0.0-coroutines-1.3.0-M2"

  object timber {
    private const val version = "5.0.0-SNAPSHOT"
    const val jdk = "com.jakewharton.timber:timber-jdk:$version"
    const val android = "com.jakewharton.timber:timber-android:$version"
  }

  const val coRedux = "com.freeletics.coredux:core:1.1.1"
  const val coReduxLog = "com.freeletics.coredux:log-common:1.1.1"
  const val conductor = "com.bluelinelabs:conductor:2.1.5"
  const val conductorPreference = "com.github.inorichi:conductor-support-preference:78e2344"
  const val materialDimens = "com.dmitrymalkovich.android:material-design-dimens:1.4"
  const val constraint = "com.android.support.constraint:constraint-layout:1.1.3"
  const val cyanea = "com.github.jaredrummler:Cyanea:4179330"

  object materialDialog {
    private const val version = "3.1.1"
    const val core = "com.afollestad.material-dialogs:core:$version"
    const val input = "com.afollestad.material-dialogs:input:$version"
  }

  object glide {
    private const val version = "4.10.0"
    const val core = "com.github.bumptech.glide:glide:$version"
    const val okhttp = "com.github.bumptech.glide:okhttp3-integration:$version"
    const val compiler = "com.github.bumptech.glide:compiler:$version"
  }

  const val flexbox = "com.google.android:flexbox:1.1.1"

  const val junit = "org.junit.jupiter:junit-jupiter-api:5.5.0"
  const val mockk = "io.mockk:mockk:1.9.3"
  const val kotlintest = "io.kotlintest:kotlintest-runner-junit5:3.3.2"

}
