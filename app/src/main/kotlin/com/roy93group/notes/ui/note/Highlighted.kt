package com.roy93group.notes.ui.note

import androidx.annotation.Keep

/**
 * Text with a list of highlighted ranges.
 */
@Keep
data class Highlighted(
    val content: String,
    val highlights: List<IntRange> = emptyList(),
)
