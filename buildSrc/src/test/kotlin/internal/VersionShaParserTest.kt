package internal

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class VersionShaParserTest {
    @Test
    fun `should extract short sha from version`() {
        val actual = VersionShaParser.parse("2.164.1-20190101@1357f27")
        assertThat(actual).isEqualTo("1357f27")
    }

    @Test
    fun `should return all 0 on nonsense version`() {
        val actual = VersionShaParser.parse("nonsense")
        assertThat(actual).isEqualTo("0000000")
    }
}
