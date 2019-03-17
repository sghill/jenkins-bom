package internal

data class UpdateCenter(val plugins: Map<String, JenkinsPlugin>, val core: Core)
