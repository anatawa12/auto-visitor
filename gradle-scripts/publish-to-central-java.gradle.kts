val org.gradle.api.Project.`publishing`: org.gradle.api.publish.PublishingExtension
    get() =
        (this as org.gradle.api.plugins.ExtensionAware).extensions.getByName("publishing") as org.gradle.api.publish.PublishingExtension

fun org.gradle.api.Project.`publishing`(configure: org.gradle.api.publish.PublishingExtension.() -> Unit): Unit =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.configure("publishing", configure)

val org.gradle.api.Project.`java`: org.gradle.api.plugins.JavaPluginExtension
    get() =
        (this as org.gradle.api.plugins.ExtensionAware).extensions.getByName("java") as org.gradle.api.plugins.JavaPluginExtension

fun org.gradle.api.Project.`java`(configure: org.gradle.api.plugins.JavaPluginExtension.() -> Unit): Unit =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.configure("java", configure)

apply(plugin = "maven-publish")

publishing.publications.create<MavenPublication>("maven") {
    from(components["java"])
    configurePom()
}

java {
    withSourcesJar()
    withJavadocJar()
}
