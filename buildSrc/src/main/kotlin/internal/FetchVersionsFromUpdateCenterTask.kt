package internal

import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import kotlin.streams.toList

open class FetchVersionsFromUpdateCenterTask : DefaultTask() {
    init {
        outputs.files("${project.buildDir}/update-center/versions.txt")
    }

    @TaskAction
    open fun writeVersions() {
        val jenkins = Retrofit.Builder()
                .baseUrl("https://updates.jenkins-ci.org/")
                .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().add(KotlinJsonAdapterFactory()).build()))
                .build()
                .create(JenkinsService::class.java)
        logger.info("Attempting to fetch jenkins update center json...")
        val response = jenkins.ltsUpdateCenter().execute()
        if (!response.isSuccessful) {
            throw GradleException("Received ${response.code()} with body ${response.errorBody()?.string() ?: "<none>"}")
        }
        val body = response.body() ?: throw GradleException("Received successful response with empty body")
        logger.info("Successfully fetched jenkins update center json")

        val additionalRecommendations = listOf(
                "org.jenkins-ci.main:jenkins-war:${body.core.version}"
        )

        val versions = project.file("${project.buildDir}/update-center/versions.txt")
        if (versions.exists()) {
            versions.delete()
        }
        val values = body.plugins.values + additionalRecommendations.map { JenkinsPlugin(it) }
        val recommendedVersions = values.stream()
                .map { p -> p.gav }
                .sorted()
                .distinct()
                .toList()
        recommendedVersions.forEach { g -> versions.appendText("$g%n".format()) }
    }
}
