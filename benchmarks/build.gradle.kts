plugins {
    java
    kotlin("jvm")
    id("kotlinx.benchmark") version "0.2.0-dev-20"
    kotlin("plugin.allopen")
}

group = project(":").group
version = project(":").version

repositories {
    mavenCentral()
    maven(url = "https://dl.bintray.com/kotlin/kotlinx")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx.benchmark.runtime-jvm:0.2.0-dev-20")
}

allOpen {
    annotation("org.openjdk.jmh.annotations.State")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

benchmark {
    targets {
        register("main") {
            extensions.add("jmhVersion", "1.21")
        }
    }
}
