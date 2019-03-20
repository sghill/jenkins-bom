package alt

import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

open class AltBintrayExtension(objects: ObjectFactory) {
    val pkgName: Property<String> = objects.property()
    val repo: Property<String> = objects.property()
    val userOrg: Property<String> = objects.property()
    val autoPublish: Property<Boolean> = objects.property()
    val autoPublishWaitForSeconds: Property<Int> = objects.property()

    fun hasSubject(p: Project): Boolean = userOrg.isPresent || p.hasProperty("bintray.user")

    fun subject(p: Project): String {
        return userOrg.getOrElse(p.property("bintray.user") as String)
    }
}