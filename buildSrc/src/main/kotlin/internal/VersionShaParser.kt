package internal

object VersionShaParser {
    fun parse(version: String): String {
        val shortSha = version.substringAfterLast("@")
        return if (shortSha == version) "0".repeat(7) else shortSha
    }
}
