package alt

import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property

open class AltBintrayCreateVersionTask : AltBintrayAbstractTask() {
    @Input
    val pkgName: Property<String> = project.objects.property()
    @Input
    val repo: Property<String> = project.objects.property()
    @Input
    @Optional
    val userOrg: Property<String> = project.objects.property()
    @Input
    @Optional
    val version: Property<String> = project.objects.property()

    private companion object {
        const val UNSET = "UNSET"
    }

    @TaskAction
    fun create() {
        val errors = mutableListOf()
        val resolvedSubject = userOrg.getOrElse(project.findProperty("bintray.user") as String? ?: UNSET)
        if (resolvedSubject.isNotSet()) {
            errors.add("userOrg or bintray.user must be set")
        }
        val resolvedVersion = version.getOrElse(UNSET)
        if (resolvedVersion == "unspecified" || resolvedVersion.isNotSet()) {
            errors.add("version or project.version must be set")
        }
        val resolvedRepoName = repo.get()
        val resolvedPkgName = pkgName.get()


        if (errors.isNotEmpty()) {
            throw GradleException("Missing required configuration for alternative bintray task: $errors")
        }
        val bintray = bintray()
        val exists = bintray.checkVersionExists(resolvedSubject, resolvedRepoName, resolvedPkgName, resolvedVersion).execute()
        if (!exists.isSuccessful) {
            val created = bintray.createVersion(resolvedSubject, resolvedRepoName, resolvedPkgName, CreateVersionRequest(resolvedVersion)).execute()
            if (created.isSuccessful) {
                logger.info("Version $resolvedVersion successfully created")
            } else {
                logger.warn("Received ${created.code()} attempting to create version $resolvedVersion")
            }
        } else {
            logger.warn("Version $resolvedVersion already existed")
        }
    }

    private fun String.isNotSet() = this == UNSET
}
