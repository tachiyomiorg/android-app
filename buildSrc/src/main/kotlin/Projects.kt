import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.project

@Suppress("EnumEntryName")
enum class Projects(val path: String) {
  // Core Java modules
  common(":common"),
  domain(":domain"),
  sourceApi(":source-api"),

  // Core Android modules
  core(":core"),

  // Tachiyomi specific Android modules
  `data`(":data"),
  presentation(":presentation"),
  app(":app")
}

fun DependencyHandler.apiProject(lib: Projects) {
  add("api", project(lib.path))
}

fun DependencyHandler.implementationProject(lib: Projects) {
  add("implementation", project(lib.path))
}
