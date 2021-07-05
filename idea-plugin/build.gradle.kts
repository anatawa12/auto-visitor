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

val release1 = "203.7148.57"
val release2 = "211.6693.65-EAP-SNAPSHOT"

intellij {
    version = release1
    pluginName = "auto-visitor"
    setPlugins("org.jetbrains.kotlin:203-1.4.30-release-IJ7148.5")
}
//*
intellij {
    version = release2
    pluginName = "auto-visitor"
    setPlugins("org.jetbrains.kotlin:211-1.4.32-release-IJ6693.72")
}
// */

val patchPluginXml by tasks.getting(PatchPluginXmlTask::class) {
    changeNotes("""
        <p>
          Current version of this plugin targets Kotlin ${kotlin.coreLibrariesVersion}.
        </p>
        <ul>
            <li>IllegalStateException for some valid when <a href="https://github.com/anatawa12/auto-visitor/pull/24">#24</a></li>
            <li>Report better error for invalid visitors <a href="https://github.com/anatawa12/auto-visitor/pull/25">#25</a></li>
            <li>Build both embeddable and not <a href="https://github.com/anatawa12/auto-visitor/pull/26">#26</a></li>
            <li>Support for no accept child class <a href="https://github.com/anatawa12/auto-visitor/pull/30">#30</a></li>
            <li>Fix: Auto-generated visitor class returns unit <a href="https://github.com/anatawa12/auto-visitor/pull/31">#31</a></li>
            <li>Rewrite transformers <a href="https://github.com/anatawa12/auto-visitor/pull/33">#33</a></li>
            <li>Upgrade org.jetbrains.intellij to 0.7.2 <a href="https://github.com/anatawa12/auto-visitor/pull/11">#11</a></li>
            <li>Upgrade com.gradle.plugin-publish to 0.14.0 <a href="https://github.com/anatawa12/auto-visitor/pull/32">#32</a></li>
            <li>Upgrade Kotlin to 1.4.32 <a href="https://github.com/anatawa12/auto-visitor/pull/28">#28</a> <a href="https://github.com/anatawa12/auto-visitor/pull/27">#27</a> <a href="https://github.com/anatawa12/auto-visitor/pull/29">#29</a></li>
            <li>Add Intellij Target Version: 2021.1 <a href="https://github.com/anatawa12/auto-visitor/pull/34">#34</a></li>
        </ul>
    """.trimIndent())
    setSinceBuild("203.7148")
    setUntilBuild("211.*")
}

val runPluginVerifier by tasks.getting(RunPluginVerifierTask::class) {
    setIdeVersions("$release1,${release2.substringBefore('-')}")
}

val publishPlugin by tasks.getting(PublishTask::class) {
    setToken(project.findProperty("com.anatawa12.jetbrains.token"))
}
