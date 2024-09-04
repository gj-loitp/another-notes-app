package com.mckimquyen.notes.ext

/**
 * For debugging, provides a log tag in every class.
 */
val Any.TAG: String
    get() = this::class.java.simpleName

