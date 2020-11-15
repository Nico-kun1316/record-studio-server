package misc

import net.InvalidParameterException
import java.util.*

fun String?.toUUID(): UUID = try {
    UUID.fromString(this)
} catch (e: Throwable) {
    throw InvalidParameterException("Invalid id format")
}
