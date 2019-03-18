import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.tasks.RecordingCopyTask
import internal.UpdateCenterParser
import internal.VersionShaParser

plugins {
    `java-platform`
    `maven-publish`
    signing
    id("com.jfrog.bintray") version "1.8.4"
}

group = "com.github.sghill.jenkins"

val upload = tasks.named("bintrayUpload")
upload.configure {
    onlyIf {
        previous.resolvedConfiguration // force resolution of the previous config
        previousSha != currentSha
    }
}
tasks.named("publish").configure { dependsOn(upload) }

val jenkins: Configuration by configurations.creating
val previous: Configuration by configurations.creating
var previousSha by extra<String>("")
var currentSha by extra<String>("")

previous.incoming.afterResolve {
    previousSha = VersionShaParser.parse(resolutionResult.allComponents.single().moduleVersion!!.version)
    println("Recognized previous recommendations sha as $previousSha")
}

jenkins.incoming.afterResolve {
    val recommendations = UpdateCenterParser.parse(jenkins.singleFile)
    val v = recommendations.toVersion()
    currentSha = recommendations.shortSha256
    project.version = v
    println("Recognized current recommendations sha as $currentSha")
}

repositories {
    ivy {
        url = uri("https://updates.jenkins-ci.org")
        patternLayout {
            artifact("[revision]/[module].actual.[ext]")
        }
        metadataSources {
            artifact()
        }
    }
    jcenter {
        content {
            includeModule(group.toString(), name)
        }
    }
}

dependencies {
    jenkins(":update-center:stable@json")
    previous("$group:$name:latest.release") {
        isChanging = true
    }
    constraints {
        UpdateCenterParser.parse(jenkins.singleFile).data.forEach {
            dependencies.constraints.add("runtime", it)
        }
    }
}

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
    if (prop("publishEmbedded") == "true") {
        repositories {
            maven {
                name = "Embedded"
                url = uri("$buildDir/embedded-mvn-repo")
            }
        }
    }
}

signing {
    sign(publishing.publications["jenkinsBom"])
    setRequired { gradle.taskGraph.hasTask(":publish") }
}

fun prop(s: String): String? = findProperty(s) as String?

project.afterEvaluate {
    bintray {
        user = prop("bintray.user")
        key = prop("bintray.apiKey")
        setPublications("jenkinsBom")
        filesSpec(delegateClosureOf<RecordingCopyTask> {
            from("${project.buildDir}/publications/jenkinsBom") {
                include("pom-default.xml.asc")
                rename { "${project.name}-${project.version}.pom.asc" }
            }
            into("com/github/sghill/jenkins/${project.name}/${project.version}")
        })
        pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
            repo = "maven"
            name = "jenkins-bom"
            publish = true
            setLicenses("MIT")
        })
    }
}
