package internal

import java.time.Clock
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

data class Recommendations(val data: List<String>, val shortSha256: String, val jenkinsVersion: String) {
    fun toVersion(clock: Clock = Clock.systemUTC()): String {
        val date = DateTimeFormatter.BASIC_ISO_DATE.format(clock.instant().atOffset(ZoneOffset.UTC))
        return "$jenkinsVersion-$date@$shortSha256"
    }
}
