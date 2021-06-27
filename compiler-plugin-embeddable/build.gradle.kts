plugins {
    id("com.github.johnrengelman.shadow")
    kotlin("jvm")
    base
    `maven-publish`
    signing
}

group = project(":").group
version = project(":").version

repositories {
    mavenCentral()
}

dependencies {
    shadow(kotlin("stdlib"))
    shadow(kotlin("compiler-embeddable"))
    shadow(project(":lib"))
    implementation(project(":compiler-plugin")) {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "com.anatawa12.autoVisitor", module = "lib")
    }
}

// simple jar will be always empty
tasks.jar.get().enabled = false

tasks.shadowJar {
    relocate("com.intellij", "org.jetbrains.kotlin.com.intellij")
    archiveClassifier.set("")
}

tasks.assemble.get().dependsOn(tasks.shadowJar.get())

@Suppress("UnstableApiUsage")
java {
    withSourcesJar()
    withJavadocJar()
}

publishing.publications.create<MavenPublication>("maven") {
    project.shadow.component(this)
    artifact(tasks.getByName("sourcesJar"))
    artifact(tasks.getByName("javadocJar"))
    configurePom()
}

apply(from = "${rootProject.projectDir}/gradle-scripts/publish-to-central.gradle.kts")
