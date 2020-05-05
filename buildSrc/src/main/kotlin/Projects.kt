import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.project

enum class Projects(val path: String) {
  // Core Java modules
  common(":common"),
  domain(":domain"),
  sourceApi(":source-api"),

  // Core Android modules
  core(":core"),
  coreUi(":core-ui"),

  // Tachiyomi specific Android modules
  `data`(":data"),
  presentation(":presentation2"),
  app(":app")
}

fun DependencyHandler.apiProject(lib: Projects) {
  add("api", project(lib.path))
}

fun DependencyHandler.implementationProject(lib: Projects) {
  add("implementation", project(lib.path))
}
