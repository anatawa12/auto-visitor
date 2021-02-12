import org.gradle.api.publish.maven.MavenPublication

fun MavenPublication.configurePom() {
    pom { pom ->
        pom.name.set("Auto Visitor")
        pom.description.set("A kotlin compiler plugin to make easy to write visitor pattern.")
        pom.url.set("https://github.com/anatawa12/auto-visitor#readme")
        pom.licenses { licenses ->
            licenses.license { license ->
                license.name.set("MIT License")
                license.url.set("https://opensource.org/licenses/mit-license.php")
            }
        }
        pom.developers { developers ->
            developers.developer { developer ->
                developer.id.set("anatawa12")
                developer.name.set("anatawa12")
                developer.email.set("anatawa12@icloud.com")
            }
        }
        pom.scm { scm ->
            scm.connection.set("scm:git:git://github.com/anatawa12/auto-visitor.git")
            scm.developerConnection.set("scm:git:ssh://github.com:anatawa12/auto-visitor.git")
            scm.url.set("https://github.com/anatawa12/auto-visitor/tree/master")
        }
    }
}
