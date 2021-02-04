plugins {
    kotlin("jvm") version "1.4.21"
    kotlin("multiplatform") version "1.4.21" apply false
}

group = "com.anatawa12.autoVisitor"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
}
