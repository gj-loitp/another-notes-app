


/**
 * For checks only performed in debug mode.
 * A failing check should be handled correctly in release mode.
 */
inline fun debugCheck(value: Boolean, message: () -> String = { "Check failed" }) {
    check(value, message)
}

inline fun debugRequire(value: Boolean, message: () -> String = { "Failed requirement" }) {
    require(value, message)
}
