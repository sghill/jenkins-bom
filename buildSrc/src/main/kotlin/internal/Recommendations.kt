package internal

import java.util.function.Supplier

data class Recommendations(val data: List<JenkinsPlugin>, val shortSha256: String, val jenkinsVersion: String) {
    fun toVersion(buildNumberSupplier: Supplier<String> = BuildNumberSupplier): String {
        return "$jenkinsVersion.${buildNumberSupplier.get()}"
    }
}
