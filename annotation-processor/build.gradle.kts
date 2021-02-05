plugins {
    kotlin("jvm")
    kotlin("kapt")
}

group = "com.anatawa12.autoVisitor"
version = "1.0-SNAPSHOT"

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
