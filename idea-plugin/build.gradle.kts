import org.jetbrains.intellij.tasks.PatchPluginXmlTask
import org.jetbrains.intellij.tasks.PublishTask
import org.jetbrains.intellij.tasks.RunPluginVerifierTask

plugins {
    id("org.jetbrains.intellij")
    kotlin("jvm")
}

group = project(":").group
version = project(":").version

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    //implementation(kotlin("compiler-embeddable"))
    implementation(project(":compiler-plugin")) {
        exclude(group = "org.jetbrains.kotlin")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf(
            "-Xjvm-default=compatibility",
            "-Xopt-in=kotlin.RequiresOptIn"
        )
    }
}

intellij {
    version = "203.7148.57"
    pluginName = "auto-visitor"
    setPlugins("org.jetbrains.kotlin:203-1.4.30-release-IJ7148.5")
}

val patchPluginXml by tasks.getting(PatchPluginXmlTask::class) {
    changeNotes("""
        The first release!
    """.trimIndent())
}

val runPluginVerifier by tasks.getting(RunPluginVerifierTask::class) {
    setIdeVersions("203.7148.57")
}

val publishPlugin by tasks.getting(PublishTask::class) {
    setToken(project.property("com.anatawa12.jetbrains.token"))
}
