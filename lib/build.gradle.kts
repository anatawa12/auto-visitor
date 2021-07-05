plugins {
    `maven-publish`
    kotlin("multiplatform")
}

group = project(":").group
version = project(":").version

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        testRuns["test"].executionTask.configure {
            useJUnit()
        }
    }
    js(IR) {
        nodejs()
    }
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")

    @Suppress("UNUSED_VARIABLE")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }


    sourceSets {
        @Suppress("UNUSED_VARIABLE")
        val commonMain by getting

        @Suppress("UNUSED_VARIABLE")
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        @Suppress("UNUSED_VARIABLE")
        val jvmMain by getting

        @Suppress("UNUSED_VARIABLE")
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }

        @Suppress("UNUSED_VARIABLE")
        val jsMain by getting

        @Suppress("UNUSED_VARIABLE")
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }

        @Suppress("UNUSED_VARIABLE")
        val nativeMain by getting

        @Suppress("UNUSED_VARIABLE")
        val nativeTest by getting
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>>() {
    kotlinOptions {
        freeCompilerArgs = listOf(
            "-Xopt-in=kotlin.RequiresOptIn"
        )
    }
}

val compileKotlinJs by tasks.getting(org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile::class) {
    if (this.kotlinOptions.outputFile.let { it != null && !it.endsWith(".js") }) {
        this.kotlinOptions.outputFile = "${this.kotlinOptions.outputFile}.js"
    }
}

val createEmptyJavadocJar by tasks.creating(Jar::class) {
    archiveBaseName.set("lib-jvm")
    archiveClassifier.set("javadoc")
}

val jvm by publishing.publications.getting(MavenPublication::class) {
    artifact(createEmptyJavadocJar)
}

publishing.publications.filterIsInstance<MavenPublication>().forEach {
    it.configurePom()
}
