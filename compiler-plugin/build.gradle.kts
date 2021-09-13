import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("kapt")
}

group = project(":").group
version = project(":").version

tasks.withType<KotlinCompile>() {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf(
            "-Xjvm-default=compatibility",
            "-Xopt-in=kotlin.RequiresOptIn"
        )
    }
}

kapt.includeCompileClasspath = false

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("compiler"))
    implementation(project(":lib"))

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.0")

    compileOnly("com.google.auto.service:auto-service-annotations:1.0")
    kapt("com.google.auto.service:auto-service:1.0")
    compileOnly(project(":annotation-value-gen"))
    kapt(project(":annotation-value-gen"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}

apply(from = "${rootProject.projectDir}/gradle-scripts/publish-to-central-java.gradle.kts")
