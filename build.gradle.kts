plugins {
    kotlin("jvm") version "1.4.30"
    kotlin("multiplatform") version "1.4.30" apply false
    kotlin("plugin.allopen") version "1.4.30" apply false
    id("org.jetbrains.intellij") version "0.7.1" apply false
}

group = "com.anatawa12.autoVisitor"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
}
