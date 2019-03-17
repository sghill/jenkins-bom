package internal

import org.gradle.api.DefaultTask

open class AddConstraintsTask : DefaultTask() {
    init {
        project.file("${project.buildDir}/update-center/versions.txt").useLines {lines ->
            lines.filterNot { it.isBlank() }.forEach {
                project.dependencies.constraints.add("runtime", it)
            }
        }
    }
}
