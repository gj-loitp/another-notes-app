package com.mckimquyen.notes.model

/**
 * Exception that should be thrown for any error expected to occur
 * during deserialization of data from a future version of the app.
 * This is used to catch forward compatibility fails.
 */
class BadDataException(
    message: String = "",
    cause: Throwable? = null,
) : IllegalStateException(message, cause)
