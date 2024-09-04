package com.mckimquyen.notes.model

/**
 * Enum with a value, so that it can be stored in future-proof manner
 * without relying on the field name or ordinal.
 */
interface ValueEnum<T> {
    val value: T
}

/**
 * Get value enum constant from its value.
 * Throws an exception if value doesn't match any enum constant.
 */
inline fun <reified V : ValueEnum<out T>, T> findValueEnum(value: T) =
    V::class.java.enumConstants?.find { it.value == value }
        ?: throw BadDataException("Unknown value")
