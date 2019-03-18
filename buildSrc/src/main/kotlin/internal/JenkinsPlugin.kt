package internal

data class JenkinsPlugin(val gav: String, val url: String): Comparable<JenkinsPlugin> {
    override fun compareTo(other: JenkinsPlugin): Int = gav.compareTo(other.gav)

    fun extension(): String = url.substringAfterLast(".")
}
