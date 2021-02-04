plugins {
    kotlin("jvm")
    kotlin("kapt")
    `java-gradle-plugin`
}

group = "org.example"
version = "1.0-SNAPSHOT"

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
