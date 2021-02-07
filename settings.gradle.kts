rootProject.name = "auto-visitor"
include("compiler-plugin")
include("annotation-value-gen")
include("lib")
include("gradle-plugin")
include("annotation-processor")
include("idea-plugin")

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
