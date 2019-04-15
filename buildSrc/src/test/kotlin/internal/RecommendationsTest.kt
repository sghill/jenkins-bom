package internal

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import java.util.function.Supplier

internal class RecommendationsTest {
    @Test
    fun `should create version with current jenkins version + build number`() {
        val recs = Recommendations(listOf("a", "b", "c").map { JenkinsPlugin(it, "http://localhost/file.jpi") }, "ab12de3", "2.150.2")
        val utc = ZoneId.of("Z")
        val clock = Clock.fixed(LocalDate.parse("2005-10-31").atStartOfDay(utc).toInstant(), utc)
        val buildNumberSupplier = Supplier { "600" }

        val actual = recs.toVersion(buildNumberSupplier)

        assertThat(actual).isEqualTo("2.150.2.600")
    }
}
