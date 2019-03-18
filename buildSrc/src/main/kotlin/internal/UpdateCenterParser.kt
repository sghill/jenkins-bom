package internal

import com.google.common.hash.Hashing
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import java.io.File
import java.nio.charset.StandardCharsets
import kotlin.streams.toList

object UpdateCenterParser {
    fun parse(file: File): Recommendations {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val updateCenter: UpdateCenter = moshi.adapter(UpdateCenter::class.java).fromJson(file.readText())!!
        val recs = updateCenter.plugins.values.stream()
                .sorted()
                .distinct()
                .toList()
        val sha256 = Hashing.sha256().hashString(recs.toString(), StandardCharsets.UTF_8).toString()
        return Recommendations(recs, ShortShaFormatter.format(sha256), updateCenter.core.version)
    }
}
