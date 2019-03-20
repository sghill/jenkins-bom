package alt

import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property

open class AltBintrayPublishTask : AltBintrayAbstractTask() {
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
    @Input
    @Optional
    val autoPublishWaitForSeconds: Property<Int> = project.objects.property()

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
        val resolvedWait = autoPublishWaitForSeconds.getOrElse(0)
        val result = bintray().publishVersion(resolvedSubject, resolvedRepoName, resolvedPkgName, resolvedVersion, PublishRequest(resolvedWait)).execute()
        if (result.isSuccessful) {
            logger.info("$resolvedPkgName version $resolvedVersion has been published")
        } else {
            throw GradleException("Received ${result.code()} attempting to publish $resolvedPkgName version $resolvedVersion")
        }
    }

    private fun String.isNotSet() = this == UNSET
}
