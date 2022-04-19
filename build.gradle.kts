plugins {
    kotlin("jvm") version "1.5.30"
    kotlin("multiplatform") version "1.5.30" apply false
    kotlin("plugin.allopen") version "1.6.21" apply false
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

allprojects {
    afterEvaluate {
        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
        if ((this as ExtensionAware).extensions.findByName("java") is JavaPluginExtension) {
            this.java.targetCompatibility = JavaVersion.VERSION_1_8
            this.java.sourceCompatibility = JavaVersion.VERSION_1_8
        }
    }
}
