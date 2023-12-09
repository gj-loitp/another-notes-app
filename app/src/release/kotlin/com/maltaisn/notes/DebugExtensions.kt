


/**
 * For checks only performed in debug mode.
 * A failing check should be handled correctly in release mode.
 */
@Suppress("UNUSED_PARAMETER")
fun debugCheck(value: Boolean, message: () -> String = { "" }) = Unit

@Suppress("UNUSED_PARAMETER")
fun debugRequire(value: Boolean, message: () -> String = { "" }) = Unit
