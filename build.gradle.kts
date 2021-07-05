plugins {
    kotlin("jvm") version "1.4.32"
    kotlin("multiplatform") version "1.5.20" apply false
    kotlin("plugin.allopen") version "1.4.32" apply false
    id("org.jetbrains.intellij") version "1.1.2" apply false
    id("com.github.johnrengelman.shadow") version "6.1.0" apply false
}

group = "com.anatawa12.autoVisitor"
version = property("version")!!

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
}
