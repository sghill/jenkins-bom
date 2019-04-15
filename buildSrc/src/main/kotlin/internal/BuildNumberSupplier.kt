package internal

import java.util.function.Supplier

object BuildNumberSupplier : Supplier<String> {
    override fun get(): String = System.getenv("CIRCLE_BUILD_NUM") ?: "LOCAL"
}
