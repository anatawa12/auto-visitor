import org.jetbrains.intellij.tasks.PatchPluginXmlTask
import org.jetbrains.intellij.tasks.PublishPluginTask
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

val release1 = "203.7148.57"
val release2 = "211.7628.21"

intellij {
    version.set(release1)
    pluginName.set("auto-visitor")
    plugins.set(listOf("org.jetbrains.kotlin:203-1.5.20-release-289-IJ7717.8"))
}
//*
intellij {
    version.set(release2)
    pluginName.set("auto-visitor")
    plugins.set(listOf("org.jetbrains.kotlin:211-1.5.20-release-284-IJ7442.40"))
}
// */

val patchPluginXml by tasks.getting(PatchPluginXmlTask::class) {
    changeNotes.set("""
        <p>
          Current version of this plugin targets Kotlin ${kotlin.coreLibrariesVersion}.
        </p>
    """.trimIndent() + (System.getenv("RELEASE_NOTE_HTML") ?: "<p>No Release Note</p>"))
    sinceBuild.set("203.7148")
    untilBuild.set(provider { null })
}

val runPluginVerifier by tasks.getting(RunPluginVerifierTask::class) {
    ideVersions.add(release1)
    ideVersions.add(release2)
}

val publishPlugin by tasks.getting(PublishPluginTask::class) {
    token.set(project.findProperty("com.anatawa12.jetbrains.token").toString())
}
