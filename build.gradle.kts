import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.tasks.RecordingCopyTask
import internal.AddConstraintsTask
import internal.FetchVersionsFromUpdateCenterTask

plugins {
    `java-platform`
    `maven-publish`
    signing
    id("com.jfrog.bintray") version "1.8.4"
}

group = "com.github.sghill.jenkins"

val addConstraints = tasks.register("addConstraints", AddConstraintsTask::class)
val fetch = tasks.register("fetchVersionsFromUpdateCenter", FetchVersionsFromUpdateCenterTask::class)
val upload = tasks.named("bintrayUpload")
upload.configure { dependsOn(addConstraints) }
tasks.named("publish").configure { dependsOn(upload, addConstraints) }

publishing {
    publications {
        create<MavenPublication>("jenkinsBom") {
            from(components["javaPlatform"])
            pom {
                name.set("Jenkins BOM")
                description.set("Bill of Materials with versions from the Jenkins Update Center")
                url.set("https://github.com/sghill/jenkins-bom")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                scm {
                    url.set("https://github.com/sghill/jenkins-bom")
                }
            }
        }
    }
    repositories {
        maven {
            name = "Embedded"
            url = uri("$buildDir/embedded-mvn-repo")
        }
    }
}

signing {
    sign(publishing.publications["jenkinsBom"])
    setRequired { gradle.taskGraph.hasTask(":publish") }
}

fun prop(s: String): String? = findProperty(s) as String?

bintray {
    user = prop("bintray.user")
    key = prop("bintray.apiKey")
    setPublications("jenkinsBom")
    filesSpec(delegateClosureOf<RecordingCopyTask> {
        from("${project.buildDir}/publications/jenkinsBom") {
            include("*.xml.asc")
            rename { "${project.name}-${project.version}.pom.asc" }
        }
        into("com/github/sghill/jenkins/${project.name}/${project.version}")
    })
    pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
        repo = "maven"
        name = "jenkins-bom"
        setLicenses("MIT")
    })
}
