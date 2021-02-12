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
    implementation(project(":lib"))
    implementation(project(":annotation-value-gen"))
    kapt(project(":annotation-value-gen"))
    compileOnly("com.google.auto.service:auto-service-annotations:1.0-rc7")
    kapt("com.google.auto.service:auto-service:1.0-rc7")
}

apply(from = "${rootProject.projectDir}/gradle-scripts/publish-to-central-java.gradle.kts")
