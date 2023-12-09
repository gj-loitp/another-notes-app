package com.roy93group.notes

/**
 * For debugging, provides a log tag in every class.
 */
val Any.TAG: String
    get() = this::class.java.simpleName

