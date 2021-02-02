rootProject.name = "auto-visitor"
include("compiler")
include("annotation-value-gen")

val enableBenchmark = System.getenv("ENABLE_AUTO_VISITOR_BENCHMARKS") == "ENABLED"
//val enableBenchmark = true

if (enableBenchmark) {
    include("benchmarks")
    pluginManagement {
        repositories {
            maven(url = "https://dl.bintray.com/kotlin/kotlinx")
            gradlePluginPortal()
        }
    }
}
