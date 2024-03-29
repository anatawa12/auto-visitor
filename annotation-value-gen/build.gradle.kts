import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("kapt")
}

group = project(":").group
version = project(":").version

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.squareup:javapoet:1.13.0")
    compileOnly("com.google.auto.service:auto-service-annotations:1.0")
    kapt("com.google.auto.service:auto-service:1.0")
}


tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
    }
}

