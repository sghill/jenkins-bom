import alt.AltBintrayExtension
import internal.UpdateCenterParser
import java.time.Instant
import java.time.format.DateTimeFormatter

plugins {
    `java-platform`
    `maven-publish`
    signing
    id("alt.bintray")
}

group = "com.github.sghill.jenkins"

val jenkins: Configuration by configurations.creating
val previous: Configuration by configurations.creating

previous.incoming.afterResolve {
    val resolved = resolutionResult.allComponents.filter { it.id is ModuleComponentIdentifier }
    if (resolved.count() != 1) {
        logger.warn("Found these components: {}", resolved)
        throw GradleException("Expected 1 component to be resolved, but found ${resolved.count()}")
    }
}

jenkins.incoming.afterResolve {
    val recommendations = UpdateCenterParser.parse(jenkins.singleFile)
    val v = recommendations.toVersion()
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
    jenkins(":update-center:stable@json") {
        setChanging(true)
    }
    previous("$group:$name:latest.release") {
        setChanging(true)
    }
    constraints {
        val updateCenter = UpdateCenterParser.parse(jenkins.singleFile)
        val rationale = "Recommended at ${DateTimeFormatter.ISO_INSTANT.format(Instant.now())} by Jenkins Update Center for v${updateCenter.jenkinsVersion}"
        api("org.jenkins-ci.main:jenkins-war:${updateCenter.jenkinsVersion}") {
            because(rationale)
        }
        api("org.jenkins-ci.main:jenkins-core:${updateCenter.jenkinsVersion}") {
            because(rationale)
        }
        updateCenter.data.forEach {
            dependencies.constraints.add("api", it.gav) {
                because(rationale)
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
