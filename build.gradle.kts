import alt.AltBintrayAbstractTask
import alt.AltBintrayExtension
import internal.UpdateCenterParser
import internal.VersionShaParser
import java.time.Instant
import java.time.format.DateTimeFormatter

plugins {
    `java-platform`
    `maven-publish`
    signing
    id("alt.bintray")
}

group = "com.github.sghill.jenkins"

// disable publishing if nothing is different
tasks.withType(AltBintrayAbstractTask::class.java).configureEach {
    onlyIf {
        previous.resolvedConfiguration // force resolution of the previous config
        logger.warn("Recommendations comparison: (previous) {} vs {} (current)", previousSha, currentSha)
        previousSha != currentSha
    }
}
tasks.withType(PublishToMavenRepository::class.java).configureEach {
    onlyIf {
        previous.resolvedConfiguration // force resolution of the previous config
        logger.warn("Recommendations comparison: (previous) {} vs {} (current)", previousSha, currentSha)
        previousSha != currentSha
    }
}

val jenkins: Configuration by configurations.creating
val previous: Configuration by configurations.creating
var previousSha by extra<String>("")
var currentSha by extra<String>("")

previous.incoming.afterResolve {
    val resolved = resolutionResult.allComponents.single { it.id is ModuleComponentIdentifier }
    previousSha = VersionShaParser.parse(resolved.moduleVersion!!.version)
}

jenkins.incoming.afterResolve {
    val recommendations = UpdateCenterParser.parse(jenkins.singleFile)
    val v = recommendations.toVersion()
    currentSha = recommendations.shortSha256
    project.version = v
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
            includeModule("com.github.sghill.jenkins", "jenkins-bom")
        }
    }
}

val jenkinsPluginExtension = Attribute.of("io.jenkins.plugin.extension", String::class.java)

dependencies {
    jenkins(":update-center:stable@json")
    previous("$group:$name:latest.release")
    constraints {
        val updateCenter = UpdateCenterParser.parse(jenkins.singleFile)
        val rationale = "Recommended at ${DateTimeFormatter.ISO_INSTANT.format(Instant.now())} by Jenkins Update Center for v${updateCenter.jenkinsVersion}"
        runtime("org.jenkins-ci.main:jenkins-war:${updateCenter.jenkinsVersion}") {
            because(rationale)
        }
        updateCenter.data.forEach {
            dependencies.constraints.add("runtime", it.gav) {
                attributes {
                    attribute(jenkinsPluginExtension, it.extension())
                }
            }
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
    repositories {
        if (prop("publishEmbedded") == "true") {
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

configure<AltBintrayExtension> {
    repo.set("maven")
    pkgName.set("jenkins-bom")
    autoPublish.set(true)
}
