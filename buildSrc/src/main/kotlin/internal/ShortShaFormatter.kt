package internal

object ShortShaFormatter {
    fun format(sha: String) = sha.take(7)
}