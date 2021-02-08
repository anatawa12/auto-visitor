import com.anatawa12.compileTimeConstant.CreateConstantsTask

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("com.anatawa12:compile-time-constant:1.0.1")
    }
}

plugins {
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
            id = "com.anatawa12.auto-visitor"
            implementationClass = "com.anatawa12.autoVisitor.gradle.AutoVisitorGradlePlugin"
        }
    }
}

val createCompileTimeConstant: CreateConstantsTask by tasks

createCompileTimeConstant.apply {
    constantsClass = "com.anatawa12.autoVisitor.gradle.Constants"
    values(mapOf(
        "version" to version.toString()
    ))
}

tasks.getByName("compileKotlin").dependsOn(createCompileTimeConstant)
