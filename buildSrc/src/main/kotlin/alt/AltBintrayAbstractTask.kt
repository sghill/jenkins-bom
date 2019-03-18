package alt

import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.gradle.api.DefaultTask
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

open class AltBintrayAbstractTask : DefaultTask() {
    fun bintray(): BintrayService = Retrofit.Builder()
            .baseUrl("https://api.bintray.com")
            .client(OkHttpClient.Builder()
                    .addInterceptor(HttpLoggingInterceptor({ m -> project.logger.info(m) }).setLevel(HttpLoggingInterceptor.Level.BASIC))
                    .addInterceptor({ chain ->
                        chain.proceed(chain.request().newBuilder()
                                .header("Authorization", Credentials.basic(prop("bintray.user"), prop("bintray.apiKey")))
                                .build())
                    })
                    .build())
            .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().add(KotlinJsonAdapterFactory()).build()))
            .build()
            .create(BintrayService::class.java)

    private fun prop(s: String): String = project.property(s) as String
}
