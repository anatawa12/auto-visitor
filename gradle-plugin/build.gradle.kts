import com.anatawa12.compileTimeConstant.CreateConstantsTask

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("com.anatawa12:compile-time-constant:1.0.2")
    }
}

plugins {
    id("com.gradle.plugin-publish") version "0.12.0"
    kotlin("jvm")
    kotlin("kapt")
    `java-gradle-plugin`
}

apply(plugin = "com.anatawa12.compile-time-constant")

group = project(":").group
version = project(":").version

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("gradle-plugin-api"))
    implementation(gradleApi())
    compileOnly("com.google.auto.service:auto-service-annotations:1.0-rc7")
    kapt("com.google.auto.service:auto-service:1.0-rc7")
}

gradlePlugin {
    plugins {
        register("autovisitor") {
            displayName = "Auto Visitor"
            description = "A kotlin compiler plugin to make easy to write visitor pattern."
            id = "com.anatawa12.auto-visitor"
            implementationClass = "com.anatawa12.autoVisitor.gradle.AutoVisitorGradlePlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/anatawa12/auto-visitor"
    vcsUrl = "https://github.com/anatawa12/auto-visitor"
    tags = listOf("kotlin", "visitor", "kotlin-compiler", "compiler-plugin")
}

fun Project.compileTimeConstant(configure: com.anatawa12.compileTimeConstant.CompileTimeConstantExtension.() -> Unit): Unit =
    (this as ExtensionAware).extensions.configure("compileTimeConstant", configure)

compileTimeConstant {
    alwaysGenerateJarFile = true
}

val createCompileTimeConstant: CreateConstantsTask by tasks

createCompileTimeConstant.apply {
    constantsClass = "com.anatawa12.autoVisitor.gradle.Constants"
    values(mapOf(
        "version" to version.toString()
    ))
}

tasks.getByName("compileKotlin").dependsOn(createCompileTimeConstant)

apply(from = "${rootProject.projectDir}/gradle-scripts/publish-to-central-java.gradle.kts")

tasks.withType<PublishToMavenRepository>().configureEach {
    onlyIf {
        if (repository.name == "mavenCentral") {
            publication.name != "autovisitorPluginMarkerMaven"
                    && publication.name != "pluginMaven"
        } else {
            true
        }
    }
}
