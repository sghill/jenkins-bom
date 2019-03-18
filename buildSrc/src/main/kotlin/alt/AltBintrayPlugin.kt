package alt

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

open class AltBintrayPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.apply<MavenPublishPlugin>()
        val altBintray = project.extensions.create("altBintray", alt.AltBintrayExtension::class)
        val createVersion = project.tasks.register<alt.AltBintrayCreateVersionTask>("createBintrayVersion") {
            pkgName.set(altBintray.pkgName)
            repo.set(altBintray.repo)
            userOrg.set(altBintray.userOrg)
            version.set(project.version as String)
        }
        val publishVersion = project.tasks.register<alt.AltBintrayPublishTask>("autoPublishBintrayVersion") {
            pkgName.set(altBintray.pkgName)
            repo.set(altBintray.repo)
            userOrg.set(altBintray.userOrg)
            version.set(project.version as String)
            onlyIf { altBintray.autoPublish.getOrElse(false) }
        }

        project.afterEvaluate {
            project.extensions.configure<PublishingExtension> {
                repositories {
                    maven {
                        if (!altBintray.hasSubject(project)) {
                            project.logger.warn("Skipping adding Bintray repository - Neither bintray.user or altBintray.userOrg defined")
                        } else {
                            val subject = altBintray.subject(project)
                            name = "Bintray"
                            url = project.uri("https://api.bintray.com/content/$subject/${altBintray.repo.get()}/${altBintray.pkgName.get()}/${project.version}")
                            credentials {
                                username = project.prop("bintray.user")
                                password = project.prop("bintray.apiKey")
                            }
                        }
                    }
                }
            }

            project.tasks.withType(PublishToMavenRepository::class.java).configureEach {
                if (!altBintray.hasSubject(project)) {
                    project.logger.warn("Skipping task dependencies setup - Neither bintray.user or altBintray.userOrg defined")
                } else {
                    val subject = altBintray.subject(project)
                    val repoUrl = "https://api.bintray.com/content/$subject/${altBintray.repo.get()}/${altBintray.pkgName.get()}/${project.version}"
                    if (repository.url == project.uri(repoUrl)) {
                        dependsOn(createVersion)
                        finalizedBy(publishVersion)
                    }
                }
            }
        }
    }

    private fun Project.prop(s: String): String? = project.findProperty(s) as String?
}
